package com.elmangusto.carrental.dto.response;

import com.elmangusto.carrental.entity.enums.CarStatus;


import java.math.BigDecimal;
import java.time.LocalDate;

public record CarResponse (

    Long id,
    String brand,
    String model,
    String registrationNumber,
    LocalDate dateRegistration,
    CarStatus status,
    BigDecimal pricePerHour,
    BigDecimal pricePerDay
) {}
