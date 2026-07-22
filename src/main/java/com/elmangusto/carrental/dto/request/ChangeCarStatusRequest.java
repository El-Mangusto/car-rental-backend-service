package com.elmangusto.carrental.dto.request;

import com.elmangusto.carrental.entity.enums.CarStatus;
import jakarta.validation.constraints.NotNull;

public record ChangeCarStatusRequest(
        @NotNull
        CarStatus newStatus
) {}
