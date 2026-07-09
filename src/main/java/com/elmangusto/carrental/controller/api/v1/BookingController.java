package com.elmangusto.carrental.controller.api.v1;

import com.elmangusto.carrental.dto.request.CreateBookingRequest;
import com.elmangusto.carrental.dto.response.BookingResponse;
import com.elmangusto.carrental.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/bookings")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse create(@RequestBody @Valid CreateBookingRequest request) {
        return bookingService.create(request);
    }

    @PatchMapping("/{id}/cancel")
    public BookingResponse cancel(@PathVariable Long id) {
        return bookingService.cancel(id);
    }
}
