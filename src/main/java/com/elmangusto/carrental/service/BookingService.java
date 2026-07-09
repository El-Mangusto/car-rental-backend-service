package com.elmangusto.carrental.service;

import com.elmangusto.carrental.dto.request.CreateBookingRequest;
import com.elmangusto.carrental.dto.response.BookingResponse;
import com.elmangusto.carrental.entity.Booking;
import com.elmangusto.carrental.entity.Car;
import com.elmangusto.carrental.entity.User;
import com.elmangusto.carrental.entity.enums.CarStatus;
import com.elmangusto.carrental.entity.enums.UserStatus;
import com.elmangusto.carrental.exception.BookingConflictException;
import com.elmangusto.carrental.exception.CarUnavailableException;
import com.elmangusto.carrental.exception.ResourceNotFoundException;
import com.elmangusto.carrental.exception.UserBannedException;
import com.elmangusto.carrental.mapper.BookingMapper;
import com.elmangusto.carrental.repository.BookingRepository;
import com.elmangusto.carrental.repository.CarRepository;
import com.elmangusto.carrental.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final UserRepository userRepository;
    private final CarRepository carRepository;

    @Transactional(readOnly = true)
    public BookingResponse getById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", id));

        return bookingMapper.toResponse(booking);
    }

    @Transactional(readOnly = true)
    public Page<BookingResponse> getAll(Long userId, Pageable pageable) {

        Page<Booking> bookings = (userId == null)
                ? bookingRepository.findAll(pageable)
                : bookingRepository.findAllByUser_Id(userId, pageable);

        return bookings.map(bookingMapper::toResponse);
    }

    public BookingResponse create(CreateBookingRequest request) {

        log.info("Creating booking for userId={}, carId={}, startTime={}, billingType={}, duration={}",
                request.userId(), request.carId(), request.startTime(), request.billingType(), request.duration());

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> {
                    log.warn("Booking creation failed: user not found, userId={}", request.userId());
                    return new ResourceNotFoundException("User", request.userId());
                });

        if (user.getStatus() == UserStatus.BANNED) {
            log.warn("Booking creation failed: userId={} is banned", user.getId());
            throw new UserBannedException(
                    "User with id %d is banned and cannot create bookings".formatted(user.getId()));
        }

        Car car = carRepository.findById(request.carId())
                .orElseThrow(() -> {
                    log.warn("Booking creation failed: car not found, carId={}", request.carId());
                    return new ResourceNotFoundException("Car", request.carId());
                });

        if (car.getStatus() != CarStatus.AVAILABLE) {
            log.warn("Booking creation failed: carId={} is MAINTENANCE", car.getId());
            throw new CarUnavailableException(
                    "Car with id %d is %s and cannot create bookings".formatted(car.getId(), car.getStatus()));
        }

        LocalDateTime endTime = request.billingType()
                .calculateEndTime(request.startTime(), request.duration());

        List<Booking> overlapping = bookingRepository.findOverlappingBookings(
                car.getId(), request.startTime(), endTime);

        if (!overlapping.isEmpty()) {
            log.warn("Booking creation failed: carId={} already booked for period {} - {}",
                    car.getId(), request.startTime(), endTime);
            throw new BookingConflictException(
                    "Car with id %d is already booked for the period %s - %s"
                            .formatted(car.getId(), request.startTime(), endTime));
        }

        BigDecimal price = switch (request.billingType()) {
            case HOURLY -> car.getPricePerHour()
                    .multiply(BigDecimal.valueOf(request.duration()));

            case DAILY -> car.getPricePerDay()
                    .multiply(BigDecimal.valueOf(request.duration()));
        };

        Booking booking = Booking.builder()
                .user(user)
                .car(car)
                .startTime(request.startTime())
                .endTime(endTime)
                .price(price)
                .build();

        Booking saved = bookingRepository.save(booking);

        log.info("Booking created successfully. id={}, userId={}, carId={}, price={}",
                saved.getId(), user.getId(), car.getId(), price);

        return bookingMapper.toResponse(saved);
    }

    public BookingResponse cancel(Long id) {

        log.info("Cancelling booking id={}", id);

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Booking cancellation failed: booking not found, id={}", id);
                    return new ResourceNotFoundException("Booking", id);
                });

        if (booking.isCancelled()) {
            log.warn("Booking cancellation failed: booking id={} already cancelled", id);
            throw new BookingConflictException(
                    "Booking with id %d is already cancelled".formatted(id));
        }

        if (booking.getStartTime().isBefore(LocalDateTime.now())) {
            log.warn("Booking cancellation failed: booking id={} rental period already started (startTime={})",
                    id, booking.getStartTime());
            throw new BookingConflictException(
                    "Cannot cancel booking with id %d: rental period already started".formatted(id));
        }

        booking.setCancelled(true);

        Booking saved = bookingRepository.save(booking);

        log.info("Booking id={} cancelled successfully", saved.getId());

        return bookingMapper.toResponse(saved);
    }
}
