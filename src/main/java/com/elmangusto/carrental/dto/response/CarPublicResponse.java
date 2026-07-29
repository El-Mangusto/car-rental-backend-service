package com.elmangusto.carrental.dto.response;

import java.math.BigDecimal;

public record CarPublicResponse(
        Long id,
        String brand,
        String model,
        BigDecimal pricePerHour,
        BigDecimal pricePerDay
) { }
