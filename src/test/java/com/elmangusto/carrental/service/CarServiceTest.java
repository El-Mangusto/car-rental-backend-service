package com.elmangusto.carrental.service;

import com.elmangusto.carrental.dto.request.CreateCarRequest;
import com.elmangusto.carrental.dto.response.CarResponse;
import com.elmangusto.carrental.entity.Car;
import com.elmangusto.carrental.entity.enums.CarStatus;
import com.elmangusto.carrental.entity.enums.Condition;
import com.elmangusto.carrental.exception.ResourceAlreadyExistsException;
import com.elmangusto.carrental.exception.ResourceNotFoundException;
import com.elmangusto.carrental.mapper.CarMapper;
import com.elmangusto.carrental.repository.CarRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class CarServiceTest {

    @Mock
    private CarRepository carRepository;

    @Mock
    private CarMapper carMapper;

    @InjectMocks
    private CarService carService;

    @Test
    void create_shouldSaveCar_whenRegistrationNumberIsUnique() {

        CreateCarRequest request = getCarRequest();

        Car car = new Car();
        Car carSaved = new Car();
        CarResponse carResponse = getCarResponse();

        when(carRepository.existsByRegistrationNumber("AA23376BC"))
                .thenReturn(false);

        when(carMapper.toEntity(request))
                .thenReturn(car);

        when(carRepository.save(car))
                .thenReturn(carSaved);

        when(carMapper.toResponse(carSaved))
                .thenReturn(carResponse);

        CarResponse result = carService.create(request);

        assertThat(result).isNotNull();
        assertThat(result.registrationNumber())
                .isEqualTo("AA23376BC");

        verify(carRepository).existsByRegistrationNumber("AA23376BC");
        verify(carRepository).save(car);
        verify(carMapper).toEntity(request);
        verify(carMapper).toResponse(carSaved);

    }

    @Test
    void create_shouldThrowResourceAlreadyExistsException_whenRegistrationNumberAlreadyExists() {

        CreateCarRequest request = getCarRequest();

        when(carRepository.existsByRegistrationNumber("AA23376BC"))
                .thenReturn(true);

        assertThatThrownBy(() -> carService.create(request))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessageContaining("AA23376BC");

        verify(carRepository).existsByRegistrationNumber("AA23376BC");

        verify(carRepository, never()).save(any());
        verify(carMapper, never()).toEntity(any());
        verify(carMapper, never()).toResponse(any());
    }

    @Test
    void getById_shouldReturnCar_whenCarExists() {

        Car car = getCar();

        when(carRepository.findById(1L))
                .thenReturn(Optional.of(car));

        CarResponse carResponse = getCarResponse();

        when(carMapper.toResponse(car))
                .thenReturn(carResponse);

        CarResponse result = carService.getById(1L);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(carResponse);

        verify(carRepository).findById(1L);
        verify(carMapper).toResponse(car);
    }

    @Test
    void getById_shouldThrowResourceNotFoundException_whenCarDoesNotExist() {

        when(carRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> carService.getById(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("1");

        verify(carRepository).findById(1L);
    }

    @Test
    void getAll_shouldReturnPageOfCars_whenCarsExist() {

        Car car = getCar();
        CarResponse carResponse = getCarResponse();

        Pageable pageable = PageRequest.of(0, 10);
        Page<Car> carPage = new PageImpl<>(List.of(car));

        when(carRepository.findAll(pageable))
                .thenReturn(carPage);

        when(carMapper.toResponse(car))
                .thenReturn(carResponse);

        Page<CarResponse> result = carService.getAll(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst()).isEqualTo(carResponse);
        assertThat(result.getTotalElements()).isEqualTo(1);

        verify(carRepository).findAll(pageable);
        verify(carMapper).toResponse(car);
        verifyNoMoreInteractions(carRepository, carMapper);
    }

    @Test
    void getAll_shouldReturnEmptyPage_whenNoCarsExist() {

        Pageable pageable = PageRequest.of(0, 10);
        Page<Car> emptyPage = Page.empty(pageable);

        when(carRepository.findAll(pageable))
                .thenReturn(emptyPage);

        Page<CarResponse> result = carService.getAll(pageable);

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(carRepository).findAll(pageable);
        verifyNoInteractions(carMapper);
    }




    private static Car getCar() {
        return Car.builder()
                .id(1L)
                .brand("BMW")
                .model("M5")
                .dateRegistration(LocalDate.now())
                .registrationNumber("AA23376BC")
                .status(CarStatus.AVAILABLE)
                .condition(Condition.OPERATIONAL)
                .pricePerHour(BigDecimal.valueOf(34))
                .pricePerDay(BigDecimal.valueOf(120))
                .build();
    }

    private static CarResponse getCarResponse() {
        return new CarResponse(
                1L,
                "BMW",
                "M5",
                "AA23376BC",
                LocalDate.now(),
                CarStatus.AVAILABLE,
                Condition.OPERATIONAL,
                BigDecimal.valueOf(34),
                BigDecimal.valueOf(120)
        );
    }

    private static CreateCarRequest getCarRequest() {
        return new CreateCarRequest(
                "BMW",
                "M5",
                "AA23376BC",
                LocalDate.now(),
                CarStatus.AVAILABLE,
                Condition.OPERATIONAL,
                BigDecimal.valueOf(34),
                BigDecimal.valueOf(120)
        );
    }
}