package com.elmangusto.carrental.dto.request;

import com.elmangusto.carrental.entity.enums.Role;
import jakarta.validation.constraints.NotNull;

public record UserRoleRequest(
        @NotNull
        Role role
) {}
