package com.elmangusto.carrental.dto.response;

import com.elmangusto.carrental.entity.enums.CarStatus;
import com.elmangusto.carrental.entity.enums.Condition;


import java.math.BigDecimal;
import java.time.LocalDate;

public record CarResponse (

    Long id,
    String brand,
    String model,
    String registrationNumber,
    LocalDate dateRegistration,
    CarStatus status,
    Condition condition,
    BigDecimal pricePerHour,
    BigDecimal pricePerDay
) {}
