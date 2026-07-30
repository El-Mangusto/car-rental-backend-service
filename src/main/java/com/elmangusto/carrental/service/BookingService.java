package com.elmangusto.carrental.service;

import com.elmangusto.carrental.dto.request.CreateBookingRequest;
import com.elmangusto.carrental.dto.response.BookingResponse;
import com.elmangusto.carrental.entity.Booking;
import com.elmangusto.carrental.entity.Car;
import com.elmangusto.carrental.entity.User;
import com.elmangusto.carrental.entity.enums.CarStatus;
import com.elmangusto.carrental.entity.enums.Role;
import com.elmangusto.carrental.entity.enums.UserStatus;
import com.elmangusto.carrental.exception.BookingConflictException;
import com.elmangusto.carrental.exception.CarUnavailableException;
import com.elmangusto.carrental.exception.ResourceNotFoundException;
import com.elmangusto.carrental.exception.UserBannedException;
import com.elmangusto.carrental.mapper.BookingMapper;
import com.elmangusto.carrental.repository.BookingRepository;
import com.elmangusto.carrental.repository.CarRepository;
import com.elmangusto.carrental.repository.UserRepository;
import com.elmangusto.carrental.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
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
    public BookingResponse getById(Long id, CustomUserDetails principal) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", id));

        requireOwnerOrAdmin(booking, principal);

        return bookingMapper.toResponse(booking);
    }

    @Transactional(readOnly = true)
    public Page<BookingResponse> getAll(Long userId, CustomUserDetails principal, Pageable pageable) {
        Long effectiveUserId = resolveEffectiveUserId(userId, principal);

        Page<Booking> bookings = (effectiveUserId == null)
                ? bookingRepository.findAll(pageable)
                : bookingRepository.findAllByUser_Id(effectiveUserId, pageable);

        return bookings.map(bookingMapper::toResponse);
    }

    public BookingResponse create(CreateBookingRequest request, CustomUserDetails principal) {

        log.info("Creating booking for userId={}, carId={}, startTime={}, billingType={}, duration={}",
                principal.getId(), request.carId(), request.startTime(), request.billingType(), request.duration());

        Long userId = principal.getId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (user.getStatus() == UserStatus.BANNED) {
            throw new UserBannedException(
                    "User with id %d is banned and cannot create bookings".formatted(user.getId()));
        }

        Car car = carRepository.findByIdForUpdate(request.carId())
                .orElseThrow(() -> new ResourceNotFoundException("Car", request.carId()));

        if (car.getStatus() != CarStatus.AVAILABLE) {
            throw new CarUnavailableException(
                    "Car with id %d is %s and cannot create bookings".formatted(car.getId(), car.getStatus()));
        }

        LocalDateTime endTime = request.billingType()
                .calculateEndTime(request.startTime(), request.duration());

        List<Booking> overlapping = bookingRepository.findOverlappingBookings(
                car.getId(), request.startTime(), endTime);

        if (!overlapping.isEmpty()) {
            throw new BookingConflictException(
                    "Car with id %d is already booked for the period %s - %s"
                            .formatted(car.getId(), request.startTime(), endTime));
        }

        BigDecimal price = switch (request.billingType()) {
            case HOURLY -> car.getPricePerHour().multiply(BigDecimal.valueOf(request.duration()));
            case DAILY -> car.getPricePerDay().multiply(BigDecimal.valueOf(request.duration()));
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

    public BookingResponse cancel(Long id, CustomUserDetails principal) {

        log.info("Cancelling booking id={}", id);

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", id));
        requireOwnerOrAdmin(booking, principal);

        if (booking.isCancelled()) {
            throw new BookingConflictException(
                    "Booking with id %d is already cancelled".formatted(id));
        }

        if (booking.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BookingConflictException(
                    "Cannot cancel booking with id %d: rental period already started at %s"
                            .formatted(id, booking.getStartTime()));
        }

        booking.setCancelled(true);

        Booking saved = bookingRepository.save(booking);

        log.info("Booking id={} cancelled successfully", saved.getId());

        return bookingMapper.toResponse(saved);
    }

    private void requireOwnerOrAdmin(Booking booking, CustomUserDetails principal) {
        boolean isOwner = booking.getUser().getId().equals(principal.getId());
        boolean isAdmin = principal.user().getRole() == Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException(
                    "You are not allowed to access booking with id %d".formatted(booking.getId()));
        }
    }

    private Long resolveEffectiveUserId(Long requestedUserId, CustomUserDetails principal) {
        boolean isAdmin = principal.user().getRole() == Role.ADMIN;
        if (isAdmin) {
            return requestedUserId;
        }
        if (requestedUserId != null && !requestedUserId.equals(principal.getId())) {
            throw new AccessDeniedException("You can only view your own bookings");
        }
        return principal.getId();
    }
}