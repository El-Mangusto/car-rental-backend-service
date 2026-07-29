package com.elmangusto.carrental.dto.filter;

import com.elmangusto.carrental.entity.enums.CarStatus;

import java.math.BigDecimal;

public record CarAdminFilter(
        String brand,
        String model,
        BigDecimal minPricePerHour,
        BigDecimal maxPricePerHour,
        BigDecimal minPricePerDay,
        BigDecimal maxPricePerDay,
        CarStatus status
) implements CarFilter {}
