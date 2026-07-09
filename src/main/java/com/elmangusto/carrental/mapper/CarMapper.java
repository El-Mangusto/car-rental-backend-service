package com.elmangusto.carrental.mapper;

import com.elmangusto.carrental.dto.request.CreateCarRequest;
import com.elmangusto.carrental.dto.response.CarResponse;
import com.elmangusto.carrental.dto.response.CarSummaryResponse;
import com.elmangusto.carrental.entity.Car;
import com.elmangusto.carrental.entity.enums.CarStatus;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CarMapper {

    Car toEntity(CreateCarRequest request);

    CarResponse toResponse(Car car);

    CarSummaryResponse toSummary(Car car);

    @AfterMapping
    default void setDefaultValues(@MappingTarget Car car) {

        if (car.getStatus() == null) {
            car.setStatus(CarStatus.AVAILABLE);
        }

    }
}
