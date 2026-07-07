package com.elmangusto.carrental.dto.response;

import com.elmangusto.carrental.entity.enums.Role;
import com.elmangusto.carrental.entity.enums.UserStatus;

import java.math.BigDecimal;

public record UserResponse(

        Long id,
        String email,
        String firstName,
        String lastName,
        String phoneNumber,
        String login,
        BigDecimal balance,
        Role role,
        UserStatus status
) {}
