package com.elmangusto.carrental.controller.api.v1;

import com.elmangusto.carrental.dto.filter.CarSearchFilter;
import com.elmangusto.carrental.dto.response.CarPublicResponse;
import com.elmangusto.carrental.service.CarService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/cars")
public class CarController {

    private final CarService carService;

    @GetMapping
    public Page<CarPublicResponse> search(
            @ParameterObject CarSearchFilter filter,
            @ParameterObject @PageableDefault(size = 10, sort = "brand") Pageable pageable) {
        return carService.searchAvailable(filter, pageable);
    }

    @GetMapping("/{id}")
    public CarPublicResponse getById(@PathVariable Long id) {
        return carService.getPublicById(id);
    }

}
