package com.elmangusto.carrental.repository.specification;

import com.elmangusto.carrental.entity.Car;
import com.elmangusto.carrental.entity.enums.CarStatus;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class CarSpecifications {

    public static Specification<Car> hasBrand(String brand) {
        return ((root, query, cb)
                -> brand == null ? null
                : cb.equal(cb.lower(root.get("brand")), brand.toLowerCase()));
    }

    public static Specification<Car> hasModel(String model) {
        return ((root, query, cb)
                -> model == null ? null
                : cb.equal(cb.lower(root.get("model")), model.toLowerCase()));
    }

    public static Specification<Car> minPricePerHour(BigDecimal min) {
        return (root, query, cb)
                -> min == null ? null
                : cb.greaterThanOrEqualTo(root.get("pricePerHour"), min);
    }

    public static Specification<Car> maxPricePerHour(BigDecimal max) {
        return (root, query, cb)
                -> max == null ? null
                : cb.lessThanOrEqualTo(root.get("pricePerHour"), max);
    }

    public static Specification<Car> minPricePerDay(BigDecimal min) {
        return (root, query, cb)
                -> min == null ? null
                : cb.greaterThanOrEqualTo(root.get("pricePerDay"), min);
    }

    public static Specification<Car> maxPricePerDay(BigDecimal max) {
        return (root, query, cb)
                -> max == null ? null
                : cb.lessThanOrEqualTo(root.get("pricePerDay"), max);
    }

    public static Specification<Car> hasStatus(CarStatus status) {
        return ((root, query, cb)
                -> status == null ? null
                : cb.equal(root.get("status"), status));
    }
}
