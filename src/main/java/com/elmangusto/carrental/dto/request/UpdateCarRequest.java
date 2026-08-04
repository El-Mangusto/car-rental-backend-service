package com.elmangusto.carrental.dto.request;


import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateCarRequest(

        @Nullable
        @Size(max = 50, message = "Brand must not exceed 50 characters")
        String brand,

        @Nullable
        @Size(max = 50, message = "Model must not exceed 50 characters")
        String model,

        @Nullable
        @Size(max = 20, message = "Registration number must not exceed 20 characters")
        String registrationNumber,

        @Nullable
        @PastOrPresent(message = "Registration date cannot be in the future")
        LocalDate dateRegistration,

        @Nullable
        @Digits(integer = 10, fraction = 2, message = "Invalid price format")
        BigDecimal pricePerHour,

        @Nullable
        @Digits(integer = 10, fraction = 2, message = "Invalid price format")
        BigDecimal pricePerDay
) {}
