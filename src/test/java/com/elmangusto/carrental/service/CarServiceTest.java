package com.elmangusto.carrental.service;

import com.elmangusto.carrental.dto.request.CarRequest;
import com.elmangusto.carrental.dto.response.CarResponse;
import com.elmangusto.carrental.entity.Car;
import com.elmangusto.carrental.entity.enums.CarStatus;
import com.elmangusto.carrental.entity.enums.Condition;
import com.elmangusto.carrental.exception.ResourceAlreadyExistsException;
import com.elmangusto.carrental.mapper.CarMapper;
import com.elmangusto.carrental.repository.CarRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@ExtendWith(MockitoExtension.class)
class CarServiceTest {

    @Mock
    private CarRepository carRepository;

    @Mock
    private CarMapper carMapper;

    @InjectMocks
    private CarService carService;

    @Test
    void addCar_shouldSaveCar_whenRegistrationNumberIsUnique() {

        CarRequest request = new CarRequest(
                "BMW",
                "M5",
                "AA23376BC",
                LocalDate.now(),
                CarStatus.AVAILABLE,
                Condition.OPERATIONAL,
                BigDecimal.valueOf(34),
                BigDecimal.valueOf(120)
        );

        Car car = new Car();
        Car carSaved = new Car();
        CarResponse carResponse = new CarResponse(
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

        when(carRepository.existsByRegistrationNumber("AA23376BC"))
                .thenReturn(false);

        when(carMapper.toEntity(request))
                .thenReturn(car);

        when(carRepository.save(car))
                .thenReturn(carSaved);

        when(carMapper.toResponse(carSaved))
                .thenReturn(carResponse);

        CarResponse result = carService.createCar(request);

        assertThat(result).isNotNull();
        assertThat(result.registrationNumber())
                .isEqualTo("AA23376BC");

        verify(carRepository).existsByRegistrationNumber("AA23376BC");
        verify(carRepository).save(car);
        verify(carMapper).toEntity(request);
        verify(carMapper).toResponse(carSaved);

    }

    @Test
    void addCar_shouldThrowDuplicateRegistrationNumberException_whenRegistrationNumberAlreadyExists() {

        CarRequest request = new CarRequest(
                "BMW",
                "M5",
                "AA23376BC",
                LocalDate.now(),
                CarStatus.AVAILABLE,
                Condition.OPERATIONAL,
                BigDecimal.valueOf(34),
                BigDecimal.valueOf(120)
        );

        when(carRepository.existsByRegistrationNumber("AA23376BC"))
                .thenReturn(true);

        assertThatThrownBy(() -> carService.createCar(request))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessageContaining("AA23376BC");

        verify(carRepository).existsByRegistrationNumber("AA23376BC");

        verify(carRepository, never()).save(any());
        verify(carMapper, never()).toEntity(any());
        verify(carMapper, never()).toResponse(any());
    }
}