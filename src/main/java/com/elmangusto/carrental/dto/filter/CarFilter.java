package com.elmangusto.carrental.dto.filter;

import java.math.BigDecimal;

public interface CarFilter {
    String brand();
    String model();
    BigDecimal minPricePerHour();
    BigDecimal maxPricePerHour();
    BigDecimal minPricePerDay();
    BigDecimal maxPricePerDay();
}
