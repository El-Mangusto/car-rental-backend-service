package com.elmangusto.carrental.dto.response;

public record CarSummaryResponse(

        Long id,
        String brand,
        String model,
        String registrationNumber
) {
}
