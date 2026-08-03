package com.elmangusto.carrental.dto.request;

import com.elmangusto.carrental.entity.enums.UserStatus;
import jakarta.validation.constraints.NotNull;

public record UserStatusRequest(
        @NotNull
        UserStatus status
) {}
