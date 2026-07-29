package com.elmangusto.carrental.dto.filter;

import java.math.BigDecimal;

public record CarSearchFilter(
        String brand,
        String model,
        BigDecimal minPricePerHour,
        BigDecimal maxPricePerHour,
        BigDecimal minPricePerDay,
        BigDecimal maxPricePerDay
) implements CarFilter {}
