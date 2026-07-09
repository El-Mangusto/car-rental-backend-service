package com.elmangusto.carrental.service;

import com.elmangusto.carrental.dto.enums.BillingType;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BookingMapper bookingMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CarRepository carRepository;

    @InjectMocks
    private BookingService bookingService;

    @Test
    void create_shouldCreateBooking_whenNoConflictingBookingsExist() {

        CreateBookingRequest request = getBookingRequest();
        User user = getUser();
        Car car = getCar();
        Booking savedBooking = getBooking();
        BookingResponse bookingResponse = getBookingResponse();

        when(userRepository.findById(request.userId()))
                .thenReturn(Optional.of(user));

        when(carRepository.findById(request.carId()))
                .thenReturn(Optional.of(car));

        when(bookingRepository.findOverlappingBookings(eq(car.getId()), any(), any()))
                .thenReturn(List.of());

        when(bookingRepository.save(any(Booking.class)))
                .thenReturn(savedBooking);

        when(bookingMapper.toResponse(savedBooking))
                .thenReturn(bookingResponse);

        BookingResponse result = bookingService.create(request);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(bookingResponse);

        verify(userRepository).findById(request.userId());
        verify(carRepository).findById(request.carId());
        verify(bookingRepository).findOverlappingBookings(eq(car.getId()), any(), any());
        verify(bookingRepository).save(any(Booking.class));
        verify(bookingMapper).toResponse(savedBooking);
    }

    @Test
    void create_shouldThrowResourceNotFoundException_whenUserDoesNotExist() {

        CreateBookingRequest request = getBookingRequest();

        when(userRepository.findById(request.userId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(request.userId().toString());

        verify(userRepository).findById(request.userId());
        verify(carRepository, never()).findById(any());
        verify(bookingRepository, never()).findOverlappingBookings(any(), any(), any());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowResourceNotFoundException_whenCarDoesNotExist() {

        CreateBookingRequest request = getBookingRequest();
        User user = getUser();

        when(userRepository.findById(request.userId()))
                .thenReturn(Optional.of(user));

        when(carRepository.findById(request.carId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(request.carId().toString());

        verify(carRepository).findById(request.carId());
        verify(bookingRepository, never()).findOverlappingBookings(any(), any(), any());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowBookingConflictException_whenOverlappingBookingsExist() {

        CreateBookingRequest request = getBookingRequest();
        User user = getUser();
        Car car = getCar();
        Booking existingBooking = getBooking();

        when(userRepository.findById(request.userId()))
                .thenReturn(Optional.of(user));

        when(carRepository.findById(request.carId()))
                .thenReturn(Optional.of(car));

        when(bookingRepository.findOverlappingBookings(eq(car.getId()), any(), any()))
                .thenReturn(List.of(existingBooking));

        assertThatThrownBy(() -> bookingService.create(request))
                .isInstanceOf(BookingConflictException.class)
                .hasMessageContaining(car.getId().toString());

        verify(bookingRepository).findOverlappingBookings(eq(car.getId()), any(), any());
        verify(bookingRepository, never()).save(any());
        verify(bookingMapper, never()).toResponse(any());
    }

    @Test
    void create_shouldThrowUserBannedException_whenUserStatusIsBanned() {

        CreateBookingRequest request = getBookingRequest();

        User user = getUser();
        user.setStatus(UserStatus.BANNED);

        when(userRepository.findById(request.userId()))
                .thenReturn(Optional.of(user));

        assertThatThrownBy(() -> bookingService.create(request))
                .isInstanceOf(UserBannedException.class)
                .hasMessageContaining(user.getId().toString());

        verify(userRepository).findById(user.getId());
        verify(carRepository, never()).findById(any());
        verify(bookingRepository, never()).findOverlappingBookings(any(), any(), any());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowCarUnavailableException_whenCarStatusIsMaintenance() {

        CreateBookingRequest request = getBookingRequest();

        User user = getUser();
        Car car = getCar();
        car.setStatus(CarStatus.MAINTENANCE);

        when(userRepository.findById(request.userId()))
                .thenReturn(Optional.of(user));

        when(carRepository.findById(request.carId()))
                .thenReturn(Optional.of(car));

        assertThatThrownBy(() -> bookingService.create(request))
                .isInstanceOf(CarUnavailableException.class)
                .hasMessageContaining(car.getId().toString());

        verify(userRepository).findById(user.getId());
        verify(carRepository).findById(car.getId());
        verify(bookingRepository, never()).findOverlappingBookings(any(), any(), any());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void cancel_shouldCancelBooking_whenBookingExists() {

        Booking booking = getBooking();
        BookingResponse response = getBookingResponse();

        when(bookingRepository.findById(1L))
                .thenReturn(Optional.of(booking));

        when (bookingRepository.save((any(Booking.class))))
                .thenReturn(booking);

        when (bookingMapper.toResponse(booking))
                .thenReturn(response);

        BookingResponse result = bookingService.cancel(1L);

        assertThat(result).isEqualTo(response);
        assertThat(booking.isCancelled()).isTrue();

        verify(bookingRepository).findById(1L);
        verify(bookingRepository).save(booking);
        verify(bookingMapper).toResponse(booking);
        verifyNoMoreInteractions(bookingRepository, bookingMapper);
    }

    @Test
    void cancel_shouldThrowResourceNotFoundException_whenBookingDoesNotExist() {

        when(bookingRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.cancel(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Booking")
                .hasMessageContaining("1");

        verify(bookingRepository).findById(1L);
        verifyNoMoreInteractions(bookingRepository, bookingMapper);
    }

    @Test
    void cancel_shouldThrowBookingConflictException_whenBookingIsAlreadyCancelled() {

        Booking booking = Booking.builder()
                .id(1L)
                .cancelled(true)
                .startTime(LocalDateTime.now().plusDays(1))
                .build();

        when(bookingRepository.findById(1L))
                .thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.cancel(1L))
                .isInstanceOf(BookingConflictException.class)
                .hasMessageContaining("already cancelled");

        verify(bookingRepository).findById(1L);
        verifyNoMoreInteractions(bookingRepository, bookingMapper);
    }

    @Test
    void cancel_shouldThrowBookingConflictException_whenRentalPeriodHasAlreadyStarted() {

        Booking booking = Booking.builder()
                .id(1L)
                .cancelled(false)
                .startTime(LocalDateTime.now().minusMinutes(1))
                .build();

        when(bookingRepository.findById(1L))
                .thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.cancel(1L))
                .isInstanceOf(BookingConflictException.class)
                .hasMessageContaining("rental period already started");

        verify(bookingRepository).findById(1L);
        verifyNoMoreInteractions(bookingRepository, bookingMapper);
    }

    @Test
    void getById_shouldReturnBooking_whenBookingExists() {

        Booking booking = getBooking();

        when(bookingRepository.findById(1L))
                .thenReturn(Optional.of(booking));

        BookingResponse bookingResponse = getBookingResponse();

        when(bookingMapper.toResponse(booking))
                .thenReturn(bookingResponse);

        BookingResponse result = bookingService.getById(1L);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(bookingResponse);

        verify(bookingRepository).findById(1L);
        verify(bookingMapper).toResponse(booking);
    }

    @Test
    void getById_shouldThrowResourceNotFoundException_whenBookingDoesNotExist() {

        when(bookingRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getById(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("1");

        verify(bookingRepository).findById(1L);
    }

    @Test
    void getAll_shouldReturnUserBookings_whenUserIdIsProvided() {

        Booking booking = getBooking();

        BookingResponse response = getBookingResponse();

        Pageable pageable = PageRequest.of(0, 10);

        Page<Booking> bookings = new PageImpl<>(List.of(booking));

        when(bookingRepository.findAllByUser_Id(1L, pageable))
                .thenReturn(bookings);

        when(bookingMapper.toResponse(booking))
                .thenReturn(response);

        Page<BookingResponse> result = bookingService.getAll(1L, pageable);

        assertThat(result).hasSize(1);
        assertThat(result.getContent()).containsExactly(response);

        verify(bookingRepository).findAllByUser_Id(1L, pageable);
        verify(bookingMapper).toResponse(booking);
        verifyNoMoreInteractions(bookingRepository, bookingMapper);
    }

    @Test
    void getAll_shouldReturnAllBookings_whenUserIdIsNull() {

        Booking booking = getBooking();

        BookingResponse response = getBookingResponse();

        Pageable pageable = PageRequest.of(0, 10);

        Page<Booking> bookings = new PageImpl<>(List.of(booking));

        when(bookingRepository.findAll(pageable))
                .thenReturn(bookings);

        when(bookingMapper.toResponse(booking))
                .thenReturn(response);

        Page<BookingResponse> result = bookingService.getAll(null, pageable);

        assertThat(result).hasSize(1);
        assertThat(result.getContent()).containsExactly(response);

        verify(bookingRepository).findAll(pageable);
        verify(bookingMapper).toResponse(booking);
        verifyNoMoreInteractions(bookingRepository, bookingMapper);
    }

    @Test
    void getAll_shouldReturnEmptyPage_whenNoBookingsExist() {

        Pageable pageable = PageRequest.of(0, 10);

        when(bookingRepository.findAll(pageable))
                .thenReturn(Page.empty(pageable));

        Page<BookingResponse> result = bookingService.getAll(null, pageable);

        assertThat(result).isEmpty();

        verify(bookingRepository).findAll(pageable);
        verifyNoMoreInteractions(bookingRepository, bookingMapper);
    }

    private static User getUser() {
        return User.builder()
                .id(1L)
                .build();
    }

    private static Car getCar() {
        return Car.builder()
                .id(1L)
                .brand("BMW")
                .model("M5")
                .pricePerHour(BigDecimal.valueOf(34))
                .pricePerDay(BigDecimal.valueOf(120))
                .build();
    }

    private static Booking getBooking() {
        return Booking.builder()
                .id(1L)
                .user(getUser())
                .car(getCar())
                .startTime(LocalDateTime.of(2026, 7, 10, 10, 0))
                .endTime(LocalDateTime.of(2026, 7, 10, 14, 0))
                .price(BigDecimal.valueOf(136))
                .cancelled(false)
                .build();
    }

    private static CreateBookingRequest getBookingRequest() {
        return new CreateBookingRequest(
                1L,
                1L,
                LocalDateTime.of(2026, 7, 10, 10, 0),
                4,
                BillingType.HOURLY

        );
    }

    private static BookingResponse getBookingResponse() {
        return new BookingResponse(
                1L,
                null,
                null,
                LocalDateTime.of(2026, 7, 10, 10, 0),
                LocalDateTime.of(2026, 7, 10, 14, 0),
                false
        );
    }
}