package com.elmangusto.carrental.repository;

import com.elmangusto.carrental.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("""
        SELECT b FROM Booking b
        WHERE b.car.id = :carId
        AND b.startTime < :endTime
        AND b.endTime > :startTime
        AND b.cancelled = false
        """)
    List<Booking> findOverlappingBookings(
            @Param("carId") Long carId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    Page<Booking> findAllByUser_Id(Long userId, Pageable pageable);

}
