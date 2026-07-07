package com.elmangusto.carrental.service;

import com.elmangusto.carrental.dto.request.CreateBookingRequest;
import com.elmangusto.carrental.dto.response.BookingResponse;
import com.elmangusto.carrental.entity.Booking;
import com.elmangusto.carrental.entity.Car;
import com.elmangusto.carrental.entity.User;
import com.elmangusto.carrental.exception.BookingConflictException;
import com.elmangusto.carrental.exception.ResourceNotFoundException;
import com.elmangusto.carrental.mapper.BookingMapper;
import com.elmangusto.carrental.repository.BookingRepository;
import com.elmangusto.carrental.repository.CarRepository;
import com.elmangusto.carrental.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    public BookingResponse create(CreateBookingRequest request) {

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.userId()));

        Car car = carRepository.findById(request.carId())
                .orElseThrow(() -> new ResourceNotFoundException("Car", request.carId()));;

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

        return bookingMapper.toResponse(saved);
    }
}
