package com.elmangusto.carrental.mapper;

import com.elmangusto.carrental.dto.request.CarRequest;
import com.elmangusto.carrental.dto.response.CarResponse;
import com.elmangusto.carrental.entity.Car;
import com.elmangusto.carrental.entity.enums.CarStatus;
import com.elmangusto.carrental.entity.enums.Condition;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CarMapper {

    Car toEntity(CarRequest request);

    CarResponse toResponse(Car car);

    @AfterMapping
    default void setDefaultValues(@MappingTarget Car car) {

        if (car.getStatus() == null) {
            car.setStatus(CarStatus.AVAILABLE);
        }

        if (car.getCondition() == null) {
            car.setCondition(Condition.OPERATIONAL);
        }
    }
}
