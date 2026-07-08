package com.elmangusto.carrental.dto.request;

import com.elmangusto.carrental.dto.enums.BillingType;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public record CreateBookingRequest(

        @NotNull(message = "User id is required")
        @Positive(message = "User id must be greater than 0")
        Long userId,

        @NotNull(message = "carId is required")
        @Positive(message = "Car id must be greater than 0")
        Long carId,

        @NotNull(message = "Start time is required")
        @FutureOrPresent(message = "Start time must be in the present or future")
        LocalDateTime startTime,

        @NotNull(message = "Duration is required")
        @Positive(message = "Duration must be greater than 0")
        Integer duration,

        @NotNull(message = "Billing type is required")
        BillingType billingType

) {}
