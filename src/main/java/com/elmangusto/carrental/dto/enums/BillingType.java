package com.elmangusto.carrental.dto.enums;

import java.time.LocalDateTime;

public enum BillingType {

    HOURLY {
        @Override
        public LocalDateTime calculateEndTime(LocalDateTime startTime, int duration) {
            return startTime.plusHours(duration);
        }
    },

    DAILY {
        @Override
        public LocalDateTime calculateEndTime(LocalDateTime startTime, int duration) {
            return startTime.plusDays(duration);
        }
    };

    public abstract LocalDateTime calculateEndTime(
            LocalDateTime startTime,
            int duration
    );
}
