package com.elmangusto.carrental.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RegisterUserRequest(

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @NotBlank(message = "First name is required")
        String firstName,

        @NotBlank(message = "Last name is required")
        String lastName,

        @NotBlank(message = "Phone number is required")
        @Pattern(
                regexp = "^\\+[1-9]\\d{1,14}$",
                message = "Phone number must be in international format (e.g. +1234567890)"
        )
        String phoneNumber,

        @NotBlank(message = "Login number is required")
        String login,

        @NotBlank(message = "Password number is required")
        String password

) {}
