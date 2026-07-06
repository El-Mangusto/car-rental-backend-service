package com.elmangusto.carrental.repository;

import com.elmangusto.carrental.entity.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CarRepository extends JpaRepository<Car, Long> {

    boolean existsByRegistrationNumber(String registrationNumber);
}
