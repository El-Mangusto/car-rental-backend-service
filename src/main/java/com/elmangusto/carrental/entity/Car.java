package com.elmangusto.carrental.entity;


import com.elmangusto.carrental.entity.enums.Condition;
import com.elmangusto.carrental.entity.enums.CarStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "cars")
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String brand;

    @Column(nullable = false)
    private String model;

    @Column(name = "registration_number", nullable = false, unique = true)
    private String registrationNumber;

    @Column(name = "date_registration", nullable = false)
    private LocalDate dateRegistration;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CarStatus status = CarStatus.AVAILABLE;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Condition condition = Condition.OPERATIONAL;

    @Column(name = "price_per_hour", precision = 10, scale = 2)
    private BigDecimal pricePerHour;

    @Column(name = "price_per_day", precision = 10, scale = 2)
    private BigDecimal pricePerDay;

}
