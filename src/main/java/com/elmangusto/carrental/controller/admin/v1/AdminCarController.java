package com.elmangusto.carrental.controller.admin.v1;

import com.elmangusto.carrental.dto.filter.CarAdminFilter;
import com.elmangusto.carrental.dto.request.ChangeCarStatusRequest;
import com.elmangusto.carrental.dto.request.CreateCarRequest;
import com.elmangusto.carrental.dto.response.CarAdminResponse;
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
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/cars")
public class AdminCarController {

    private final CarService carService;

    @GetMapping
    public Page<CarAdminResponse> search(
            @ParameterObject CarAdminFilter filter,
            @ParameterObject @PageableDefault(size = 10, sort = "brand") Pageable pageable) {
        return carService.searchAll(filter, pageable);
    }

    @GetMapping("/{id}")
    public CarAdminResponse getById(@PathVariable Long id) {
        return carService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CarAdminResponse create(@RequestBody @Valid CreateCarRequest request) {
        return carService.create(request);
    }

    @PatchMapping("/{id}/status")
    public CarAdminResponse changeStatus(
            @PathVariable Long id,
            @RequestBody @Valid ChangeCarStatusRequest request) {
        return carService.changeStatus(id, request.newStatus());
    }
}
