package com.elmangusto.carrental.dto.request;

import org.jspecify.annotations.Nullable;

public record UserProfilePatchRequest(
        @Nullable String firstName,
        @Nullable String lastName,
        @Nullable String phoneNumber
) {}
