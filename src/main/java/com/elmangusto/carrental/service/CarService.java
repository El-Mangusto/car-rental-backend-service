package com.elmangusto.carrental.service;

import com.elmangusto.carrental.dto.request.CreateCarRequest;
import com.elmangusto.carrental.dto.response.CarResponse;
import com.elmangusto.carrental.entity.Car;
import com.elmangusto.carrental.entity.enums.CarStatus;
import com.elmangusto.carrental.exception.CarUnavailableException;
import com.elmangusto.carrental.exception.ResourceAlreadyExistsException;
import com.elmangusto.carrental.exception.ResourceNotFoundException;
import com.elmangusto.carrental.mapper.CarMapper;
import com.elmangusto.carrental.repository.CarRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CarService {

    private final CarRepository carRepository;
    private final CarMapper carMapper;

    @Transactional(readOnly = true)
    public Page<CarResponse> getAll(Pageable pageable) {
        return carRepository.findAll(pageable)
                .map(carMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public CarResponse getById(Long id) {

        Car car = carRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Car", id));

        return carMapper.toResponse(car);
    }

    public CarResponse create(CreateCarRequest request) {

        log.info("Creating car with registrationNumber={}", request.registrationNumber());

        if (carRepository.existsByRegistrationNumber(request.registrationNumber())) {
            throw new ResourceAlreadyExistsException(
                    "Car with registration number '%s' already exists".formatted(request.registrationNumber()));
        }

        Car car = carMapper.toEntity(request);
        Car saved = carRepository.save(car);

        log.info("Car created successfully. id={}, registrationNumber={}",
                saved.getId(), saved.getRegistrationNumber());

        return carMapper.toResponse(saved);
    }

    public CarResponse changeStatus(Long id, CarStatus newStatus) {

        log.info("Changing status for carId={} to newStatus={}", id, newStatus);

        Car car = carRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Car", id));

        if (car.getStatus() == CarStatus.SCRAPPED) {
            throw new CarUnavailableException(
                    "Car with id %d is scrapped and its status cannot be changed".formatted(id));
        }

        if (car.getStatus() == newStatus) {
            log.info("Car id={} already has status={}, no changes applied", id, newStatus);
            return carMapper.toResponse(car);
        }

        car.setStatus(newStatus);

        Car saved = carRepository.save(car);

        log.info("Car id={} status changed successfully to {}", saved.getId(), saved.getStatus());

        return carMapper.toResponse(saved);
    }
}