package com.elmangusto.carrental.dto.request;

import jakarta.validation.constraints.Pattern;
import org.jspecify.annotations.Nullable;

public record UserProfilePatchRequest(

        @Nullable
        String firstName,

        @Nullable
        String lastName,

        @Nullable
        @Pattern(
                regexp = "^\\+[1-9]\\d{1,14}$",
                message = "Phone number must be in international format (e.g. +1234567890)"
        )
        String phoneNumber
) {}
