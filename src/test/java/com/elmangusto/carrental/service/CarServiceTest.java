package com.elmangusto.carrental.service;

import com.elmangusto.carrental.dto.filter.CarAdminFilter;
import com.elmangusto.carrental.dto.filter.CarSearchFilter;
import com.elmangusto.carrental.dto.request.CreateCarRequest;
import com.elmangusto.carrental.dto.request.UpdateCarRequest;
import com.elmangusto.carrental.dto.response.CarAdminResponse;
import com.elmangusto.carrental.dto.response.CarPublicResponse;
import com.elmangusto.carrental.entity.Car;
import com.elmangusto.carrental.entity.enums.CarStatus;
import com.elmangusto.carrental.exception.CarUnavailableException;
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
import org.springframework.data.jpa.domain.Specification;

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
        CarAdminResponse carAdminResponse = getCarAdminResponse();

        when(carRepository.existsByRegistrationNumber("AA23376BC"))
                .thenReturn(false);

        when(carMapper.toEntity(request))
                .thenReturn(car);

        when(carRepository.save(car))
                .thenReturn(carSaved);

        when(carMapper.toAdminResponse(carSaved))
                .thenReturn(carAdminResponse);

        CarAdminResponse result = carService.create(request);

        assertThat(result).isNotNull();
        assertThat(result.registrationNumber())
                .isEqualTo("AA23376BC");

        verify(carRepository).existsByRegistrationNumber("AA23376BC");
        verify(carRepository).save(car);
        verify(carMapper).toEntity(request);
        verify(carMapper).toAdminResponse(carSaved);

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
        verify(carMapper, never()).toAdminResponse(any());
    }

    @Test
    void getById_shouldReturnCar_whenCarExists() {

        Car car = getCar();

        when(carRepository.findById(1L))
                .thenReturn(Optional.of(car));

        CarAdminResponse carAdminResponse = getCarAdminResponse();

        when(carMapper.toAdminResponse(car))
                .thenReturn(carAdminResponse);

        CarAdminResponse result = carService.getById(1L);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(carAdminResponse);

        verify(carRepository).findById(1L);
        verify(carMapper).toAdminResponse(car);
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
    void searchAll_shouldReturnPageOfCars_whenCarsExist() {

        Car car = getCar();
        CarAdminResponse carAdminResponse = getCarAdminResponse();
        CarAdminFilter filter = new CarAdminFilter(null, null, null, null, null, null, null);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Car> carPage = new PageImpl<>(List.of(car));

        when(carRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(carPage);

        when(carMapper.toAdminResponse(car))
                .thenReturn(carAdminResponse);

        Page<CarAdminResponse> result = carService.searchAll(filter, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst()).isEqualTo(carAdminResponse);
        assertThat(result.getTotalElements()).isEqualTo(1);

        verify(carRepository).findAll(any(Specification.class), eq(pageable));
        verify(carMapper).toAdminResponse(car);
        verifyNoMoreInteractions(carRepository, carMapper);
    }

    @Test
    void searchAll_shouldReturnEmptyPage_whenNoCarsExist() {

        CarAdminFilter filter = new CarAdminFilter(null, null, null, null, null, null, null);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Car> emptyPage = Page.empty(pageable);

        when(carRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(emptyPage);

        Page<CarAdminResponse> result = carService.searchAll(filter, pageable);

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(carRepository).findAll(any(Specification.class), eq(pageable));
        verifyNoInteractions(carMapper);
    }

    @Test
    void searchAvailable_shouldReturnPageOfCars_whenCarsExist() {

        Car car = getCar();
        CarPublicResponse carPublicResponse = getCarPublicResponse();
        CarSearchFilter filter = new CarSearchFilter(null, null, null, null, null, null);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Car> carPage = new PageImpl<>(List.of(car));

        when(carRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(carPage);

        when(carMapper.toPublicResponse(car))
                .thenReturn(carPublicResponse);

        Page<CarPublicResponse> result = carService.searchAvailable(filter, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst()).isEqualTo(carPublicResponse);

        verify(carRepository).findAll(any(Specification.class), eq(pageable));
        verify(carMapper).toPublicResponse(car);
        verifyNoMoreInteractions(carRepository, carMapper);
    }

    @Test
    void changeStatus_shouldUpdateStatus_whenCarExist() {

        Car car = getCar();
        Car saved = getCar();
        saved.setStatus(CarStatus.MAINTENANCE);
        CarAdminResponse response = new CarAdminResponse(
                1L,
                "BMW",
                "M5",
                "AA23376BC",
                LocalDate.now(),
                CarStatus.MAINTENANCE,
                BigDecimal.valueOf(34),
                BigDecimal.valueOf(120)
        );

        when(carRepository.findById(1L))
                .thenReturn(Optional.of(car));

        when(carRepository.save(car))
                .thenReturn(saved);

        when(carMapper.toAdminResponse(saved))
                .thenReturn(response);

        CarAdminResponse result = carService.changeStatus(1L, CarStatus.MAINTENANCE);

        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo(CarStatus.MAINTENANCE);

        verify(carRepository).findById(1L);
        verify(carRepository).save(car);
        verify(carMapper).toAdminResponse(saved);
    }

    @Test
    void changeStatus_shouldResourceNotFoundException_whenCarDoesNotExist() {

        when(carRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> carService.changeStatus(1L, CarStatus.AVAILABLE))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("1");

        verify(carRepository).findById(any(Long.class));
        verify(carRepository, never()).save(any(Car.class));
        verify(carMapper, never()).toAdminResponse(any(Car.class));
    }

    @Test
    void changeStatus_shouldCarUnavailableException_whenCarStatusIsScrapped() {

        Car car = getCar();
        car.setStatus(CarStatus.SCRAPPED);

        when(carRepository.findById(1L))
                .thenReturn(Optional.of(car));

        assertThatThrownBy(() -> carService.changeStatus(1L, CarStatus.AVAILABLE))
                .isInstanceOf(CarUnavailableException.class)
                .hasMessageContaining("1");

        verify(carRepository).findById(any(Long.class));
        verify(carRepository, never()).save(any(Car.class));
        verify(carMapper, never()).toAdminResponse(any(Car.class));
    }

    @Test
    void updateCar_shouldUpdateAllFields_whenRequestHasAllFields() {

        Car car = getCar();
        Car saved = getCar();
        saved.setBrand("Audi");
        saved.setModel("A6");
        saved.setPricePerDay(BigDecimal.valueOf(150));

        UpdateCarRequest request = new UpdateCarRequest(
                "Audi", "A6", null, null, null, BigDecimal.valueOf(150)
        );

        CarAdminResponse response = new CarAdminResponse(
                1L, "Audi", "A6", "AA23376BC", LocalDate.now(),
                CarStatus.AVAILABLE, BigDecimal.valueOf(34), BigDecimal.valueOf(150)
        );

        when(carRepository.findById(1L))
                .thenReturn(Optional.of(car));

        when(carRepository.save(car))
                .thenReturn(saved);

        when(carMapper.toAdminResponse(saved))
                .thenReturn(response);

        CarAdminResponse result = carService.updateCar(1L, request);

        assertThat(result).isNotNull();
        assertThat(result.brand()).isEqualTo("Audi");
        assertThat(result.model()).isEqualTo("A6");
        assertThat(result.pricePerDay()).isEqualTo(BigDecimal.valueOf(150));

        verify(carRepository).findById(1L);
        verify(carRepository).save(car);
        verify(carMapper).toAdminResponse(saved);
    }

    @Test
    void updateCar_shouldNotChangeAnything_whenAllFieldsAreNull() {

        Car car = getCar();
        CarAdminResponse response = getCarAdminResponse();

        UpdateCarRequest request = new UpdateCarRequest(
                null, null, null, null, null, null
        );

        when(carRepository.findById(1L))
                .thenReturn(Optional.of(car));

        when(carRepository.save(car))
                .thenReturn(car);

        when(carMapper.toAdminResponse(car))
                .thenReturn(response);

        CarAdminResponse result = carService.updateCar(1L, request);

        assertThat(result).isNotNull();

        verify(carRepository).findById(1L);
        verify(carRepository).save(car);
        verify(carMapper).toAdminResponse(car);
    }

    @Test
    void updateCar_shouldThrowResourceNotFoundException_whenCarDoesNotExist() {

        UpdateCarRequest request = new UpdateCarRequest(
                "Audi", null, null, null, null, null
        );

        when(carRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> carService.updateCar(1L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("1");

        verify(carRepository).findById(1L);
        verify(carRepository, never()).save(any(Car.class));
        verify(carMapper, never()).toAdminResponse(any(Car.class));
    }

    @Test
    void updateCar_shouldUpdateRegistrationNumber_whenNewNumberIsUnique() {

        Car car = getCar();
        Car saved = getCar();
        saved.setRegistrationNumber("BB99887CC");

        UpdateCarRequest request = new UpdateCarRequest(
                null, null, "BB99887CC", null, null, null
        );

        CarAdminResponse response = new CarAdminResponse(
                1L, "BMW", "M5", "BB99887CC", LocalDate.now(),
                CarStatus.AVAILABLE, BigDecimal.valueOf(34), BigDecimal.valueOf(120)
        );

        when(carRepository.findById(1L))
                .thenReturn(Optional.of(car));

        when(carRepository.findByRegistrationNumber("BB99887CC"))
                .thenReturn(Optional.empty());

        when(carRepository.save(car))
                .thenReturn(saved);

        when(carMapper.toAdminResponse(saved))
                .thenReturn(response);

        CarAdminResponse result = carService.updateCar(1L, request);

        assertThat(result.registrationNumber()).isEqualTo("BB99887CC");

        verify(carRepository).findByRegistrationNumber("BB99887CC");
        verify(carRepository).save(car);
    }

    @Test
    void updateCar_shouldThrowResourceAlreadyExistsException_whenRegistrationNumberTakenByAnotherCar() {

        Car car = getCar();
        Car anotherCar = getCar();
        anotherCar.setId(2L);
        anotherCar.setRegistrationNumber("BB99887CC");

        UpdateCarRequest request = new UpdateCarRequest(
                null, null, "BB99887CC", null, null, null
        );

        when(carRepository.findById(1L))
                .thenReturn(Optional.of(car));

        when(carRepository.findByRegistrationNumber("BB99887CC"))
                .thenReturn(Optional.of(anotherCar));

        assertThatThrownBy(() -> carService.updateCar(1L, request))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessageContaining("BB99887CC");

        verify(carRepository, never()).save(any(Car.class));
        verify(carMapper, never()).toAdminResponse(any(Car.class));
    }

    @Test
    void updateCar_shouldNotThrow_whenRegistrationNumberUnchanged() {

        Car car = getCar();
        CarAdminResponse response = getCarAdminResponse();

        UpdateCarRequest request = new UpdateCarRequest(
                null, null, "AA23376BC", null, null, null
        );

        when(carRepository.findById(1L))
                .thenReturn(Optional.of(car));

        when(carRepository.findByRegistrationNumber("AA23376BC"))
                .thenReturn(Optional.of(car));

        when(carRepository.save(car))
                .thenReturn(car);

        when(carMapper.toAdminResponse(car))
                .thenReturn(response);

        CarAdminResponse result = carService.updateCar(1L, request);

        assertThat(result).isNotNull();

        verify(carRepository).save(car);
    }


    private static Car getCar() {
        return Car.builder()
                .id(1L)
                .brand("BMW")
                .model("M5")
                .dateRegistration(LocalDate.now())
                .registrationNumber("AA23376BC")
                .status(CarStatus.AVAILABLE)
                .pricePerHour(BigDecimal.valueOf(34))
                .pricePerDay(BigDecimal.valueOf(120))
                .build();
    }

    private static CarAdminResponse getCarAdminResponse() {
        return new CarAdminResponse(
                1L,
                "BMW",
                "M5",
                "AA23376BC",
                LocalDate.now(),
                CarStatus.AVAILABLE,
                BigDecimal.valueOf(34),
                BigDecimal.valueOf(120)
        );
    }

    private static CarPublicResponse getCarPublicResponse() {
        return new CarPublicResponse(
                1L,
                "BMW",
                "M5",
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
                BigDecimal.valueOf(34),
                BigDecimal.valueOf(120)
        );
    }
}