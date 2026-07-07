package com.elmangusto.carrental.dto.response;

import java.time.LocalDateTime;

public record BookingResponse(

        Long id,
        UserSummaryResponse user,
        CarSummaryResponse car,
        LocalDateTime startTime,
        LocalDateTime endTime
) {}
