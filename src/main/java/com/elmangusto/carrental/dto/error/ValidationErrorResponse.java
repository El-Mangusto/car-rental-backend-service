package com.elmangusto.carrental.dto.error;

import java.time.LocalDateTime;
import java.util.Map;

public record ValidationErrorResponse(

        int status,
        String error,
        Map<String, String> validationError,
        LocalDateTime timestamp
) {
}
