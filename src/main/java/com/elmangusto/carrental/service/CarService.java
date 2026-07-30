package com.elmangusto.carrental.service;

import com.elmangusto.carrental.dto.filter.CarAdminFilter;
import com.elmangusto.carrental.dto.filter.CarFilter;
import com.elmangusto.carrental.dto.filter.CarSearchFilter;
import com.elmangusto.carrental.dto.request.CreateCarRequest;
import com.elmangusto.carrental.dto.response.CarAdminResponse;
import com.elmangusto.carrental.dto.response.CarPublicResponse;
import com.elmangusto.carrental.entity.Car;
import com.elmangusto.carrental.entity.enums.CarStatus;
import com.elmangusto.carrental.exception.CarUnavailableException;
import com.elmangusto.carrental.exception.ResourceAlreadyExistsException;
import com.elmangusto.carrental.exception.ResourceNotFoundException;
import com.elmangusto.carrental.mapper.CarMapper;
import com.elmangusto.carrental.repository.CarRepository;
import com.elmangusto.carrental.repository.specification.CarSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CarService {

    private final CarRepository carRepository;
    private final CarMapper carMapper;

    @Transactional(readOnly = true)
    public Page<CarPublicResponse> searchAvailable(CarSearchFilter filter, Pageable pageable) {
        Specification<Car> spec = buildCommonSpec(filter)
                .and(CarSpecifications.hasStatus(CarStatus.AVAILABLE));

        return carRepository.findAll(spec, pageable).map(carMapper::toPublicResponse);
    }

    @Transactional(readOnly = true)
    public Page<CarAdminResponse> searchAll(CarAdminFilter filter, Pageable pageable) {
        Specification<Car> spec = buildCommonSpec(filter)
                .and(CarSpecifications.hasStatus(filter.status()));

        return carRepository.findAll(spec, pageable).map(carMapper::toAdminResponse);
    }

    public CarPublicResponse getPublicById(Long id) {
        Car car = carRepository.findById(id)
                .filter(c -> c.getStatus() == CarStatus.AVAILABLE)
                .orElseThrow(() -> new ResourceNotFoundException("Car", id));
        return carMapper.toPublicResponse(car);
    }

    @Transactional(readOnly = true)
    public CarAdminResponse getById(Long id) {
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Car", id));
        return carMapper.toAdminResponse(car);
    }

    public CarAdminResponse create(CreateCarRequest request) {

        log.info("Creating car with registrationNumber={}", request.registrationNumber());

        if (carRepository.existsByRegistrationNumber(request.registrationNumber())) {
            throw new ResourceAlreadyExistsException(
                    "Car with registration number '%s' already exists".formatted(request.registrationNumber()));
        }

        Car car = carMapper.toEntity(request);
        Car saved = carRepository.save(car);

        log.info("Car created successfully. id={}, registrationNumber={}",
                saved.getId(), saved.getRegistrationNumber());

        return carMapper.toAdminResponse(saved);
    }

    public CarAdminResponse changeStatus(Long id, CarStatus newStatus) {

        log.info("Changing status for carId={} to newStatus={}", id, newStatus);

        Car car = carRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Car", id));

        if (car.getStatus() == CarStatus.SCRAPPED) {
            throw new CarUnavailableException(
                    "Car with id %d is scrapped and its status cannot be changed".formatted(id));
        }

        if (car.getStatus() == newStatus) {
            log.info("Car id={} already has status={}, no changes applied", id, newStatus);
            return carMapper.toAdminResponse(car);
        }

        car.setStatus(newStatus);

        Car saved = carRepository.save(car);

        log.info("Car id={} status changed successfully to {}", saved.getId(), saved.getStatus());

        return carMapper.toAdminResponse(saved);
    }

    private Specification<Car> buildCommonSpec(CarFilter filter) {
        return Specification.allOf(List.of(
                CarSpecifications.hasBrand(filter.brand()),
                CarSpecifications.hasModel(filter.model()),
                CarSpecifications.minPricePerHour(filter.minPricePerHour()),
                CarSpecifications.maxPricePerHour(filter.maxPricePerHour()),
                CarSpecifications.minPricePerDay(filter.minPricePerDay()),
                CarSpecifications.maxPricePerDay(filter.maxPricePerDay())
        ));
    }
}