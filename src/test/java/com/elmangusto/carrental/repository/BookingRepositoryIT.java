package com.elmangusto.carrental.repository;

import com.elmangusto.carrental.AbstractPostgresContainerTest;
import com.elmangusto.carrental.entity.Booking;
import com.elmangusto.carrental.entity.Car;
import com.elmangusto.carrental.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BookingRepositoryIT extends AbstractPostgresContainerTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User user;
    private Car car;

    @BeforeEach
    void setUp() {

        user = User.builder()
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("+380501234567")
                .login("testuser")
                .password("password")
                .build();
        entityManager.persist(user);

        car = Car.builder()
                .brand("BMW")
                .model("M5")
                .registrationNumber("AA1234BB")
                .dateRegistration(LocalDate.of(2020, 1, 1))
                .pricePerHour(BigDecimal.valueOf(30))
                .pricePerDay(BigDecimal.valueOf(100))
                .build();
        entityManager.persist(car);
    }

    @Test
    void findOverlappingBookings_shouldReturnBooking_whenNewBookingStartsInsideExisting() {

        persistBooking(
                LocalDateTime.of(2026, 7, 10, 10, 0),
                LocalDateTime.of(2026, 7, 10, 14, 0),
                false);

        List<Booking> result = bookingRepository.findOverlappingBookings(
                car.getId(),
                LocalDateTime.of(2026, 7, 10, 12, 0),
                LocalDateTime.of(2026, 7, 10, 16, 0));

        assertThat(result).hasSize(1);
    }

    @Test
    void findOverlappingBookings_shouldReturnBooking_whenNewBookingEndsInsideExisting() {

        persistBooking(
                LocalDateTime.of(2026, 7, 10, 10, 0),
                LocalDateTime.of(2026, 7, 10, 14, 0),
                false);

        List<Booking> result = bookingRepository.findOverlappingBookings(
                car.getId(),
                LocalDateTime.of(2026, 7, 10, 8, 0),
                LocalDateTime.of(2026, 7, 10, 12, 0));

        assertThat(result).hasSize(1);
    }

    @Test
    void findOverlappingBookings_shouldReturnBooking_whenNewBookingFullyContainsExisting() {

        persistBooking(
                LocalDateTime.of(2026, 7, 10, 11, 0),
                LocalDateTime.of(2026, 7, 10, 13, 0),
                false);

        List<Booking> result = bookingRepository.findOverlappingBookings(
                car.getId(),
                LocalDateTime.of(2026, 7, 10, 10, 0),
                LocalDateTime.of(2026, 7, 10, 14, 0));

        assertThat(result).hasSize(1);
    }

    @Test
    void findOverlappingBookings_shouldReturnEmpty_whenNewBookingStartsExactlyWhenExistingEnds() {

        persistBooking(
                LocalDateTime.of(2026, 7, 10, 10, 0),
                LocalDateTime.of(2026, 7, 10, 12, 0),
                false);

        List<Booking> result = bookingRepository.findOverlappingBookings(
                car.getId(),
                LocalDateTime.of(2026, 7, 10, 12, 0),
                LocalDateTime.of(2026, 7, 10, 14, 0));

        assertThat(result).isEmpty();
    }

    @Test
    void findOverlappingBookings_shouldReturnEmpty_whenNewBookingEndsExactlyWhenExistingStarts() {

        persistBooking(
                LocalDateTime.of(2026, 7, 10, 12, 0),
                LocalDateTime.of(2026, 7, 10, 14, 0),
                false);

        List<Booking> result = bookingRepository.findOverlappingBookings(
                car.getId(),
                LocalDateTime.of(2026, 7, 10, 10, 0),
                LocalDateTime.of(2026, 7, 10, 12, 0));

        assertThat(result).isEmpty();
    }

    @Test
    void findOverlappingBookings_shouldReturnEmpty_whenExistingBookingIsCancelled() {

        persistBooking(
                LocalDateTime.of(2026, 7, 10, 10, 0),
                LocalDateTime.of(2026, 7, 10, 14, 0),
                true);

        List<Booking> result = bookingRepository.findOverlappingBookings(
                car.getId(),
                LocalDateTime.of(2026, 7, 10, 12, 0),
                LocalDateTime.of(2026, 7, 10, 16, 0));

        assertThat(result).isEmpty();
    }

    @Test
    void findOverlappingBookings_shouldReturnEmpty_whenNoBookingsForThisCar() {

        List<Booking> result = bookingRepository.findOverlappingBookings(
                car.getId(),
                LocalDateTime.of(2026, 7, 10, 10, 0),
                LocalDateTime.of(2026, 7, 10, 14, 0));

        assertThat(result).isEmpty();
    }

    private void persistBooking(LocalDateTime start, LocalDateTime end, boolean cancelled) {

        Booking booking = Booking.builder()
                .user(user)
                .car(car)
                .startTime(start)
                .endTime(end)
                .price(BigDecimal.valueOf(100))
                .cancelled(cancelled)
                .build();

        entityManager.persist(booking);
        entityManager.flush();
    }
}