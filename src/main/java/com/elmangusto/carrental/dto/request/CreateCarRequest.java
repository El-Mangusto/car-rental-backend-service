package com.elmangusto.carrental.dto.request;

import com.elmangusto.carrental.entity.enums.CarStatus;
import com.elmangusto.carrental.entity.enums.Condition;
import jakarta.validation.constraints.*;


import java.math.BigDecimal;
import java.time.LocalDate;


public record CreateCarRequest(

    @NotBlank(message = "Brand is required")
    @Size(max = 50, message = "Brand must not exceed 50 characters")
    String brand,

    @NotBlank(message = "Model is required")
    @Size(max = 50, message = "Model must not exceed 50 characters")
    String model,

    @NotBlank(message = "Registration number is required")
    @Size(max = 20, message = "Registration number must not exceed 20 characters")
    String registrationNumber,

    @NotNull(message = "Registration date is required")
    @PastOrPresent(message = "Registration date cannot be in the future")
    LocalDate dateRegistration,

    CarStatus status,

    Condition condition,

    @PositiveOrZero(message = "Price per hour must be greater than or equal to 0")
    @Digits(integer = 10, fraction = 2, message = "Invalid price format")
    BigDecimal pricePerHour,

    @PositiveOrZero(message = "Price per day must be greater than or equal to 0")
    @Digits(integer = 10, fraction = 2, message = "Invalid price format")
    BigDecimal pricePerDay

) {}
