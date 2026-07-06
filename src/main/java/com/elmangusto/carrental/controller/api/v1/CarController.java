package com.elmangusto.carrental.controller.api.v1;

import com.elmangusto.carrental.dto.request.CarRequest;
import com.elmangusto.carrental.dto.response.CarResponse;
import com.elmangusto.carrental.service.CarService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/cars")
@RequiredArgsConstructor
public class CarController {

    private final CarService carService;

    @GetMapping
    public Page<CarResponse> getAll(
            @ParameterObject
            @PageableDefault(size = 10, sort = "brand") Pageable pageable) {
        return carService.getAll(pageable);
    }

    @GetMapping("/{id}")
    public CarResponse getById(@PathVariable Long id) {
        return carService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CarResponse createCar(@RequestBody @Valid CarRequest request) {
        return carService.createCar(request);
    }
}
