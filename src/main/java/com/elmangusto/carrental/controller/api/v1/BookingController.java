package com.elmangusto.carrental.controller.api.v1;

import com.elmangusto.carrental.dto.request.CreateBookingRequest;
import com.elmangusto.carrental.dto.response.BookingResponse;
import com.elmangusto.carrental.security.CustomUserDetails;
import com.elmangusto.carrental.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/bookings")
public class BookingController {

    private final BookingService bookingService;

    @GetMapping("/{id}")
    public BookingResponse getById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return bookingService.getById(id, principal);
    }

    @GetMapping
    public Page<BookingResponse> getAll(
            @RequestParam(required = false) Long userId,
            @ParameterObject
            @AuthenticationPrincipal CustomUserDetails principal,
            @PageableDefault(size = 10, sort = "startTime")
            Pageable pageable) {
        return bookingService.getAll(userId, principal, pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse create(@RequestBody @Valid CreateBookingRequest request,
                                  @AuthenticationPrincipal CustomUserDetails principal) {
        return bookingService.create(request, principal);
    }

    @PatchMapping("/{id}/cancel")
    public BookingResponse cancel(@PathVariable Long id,
                                  @AuthenticationPrincipal CustomUserDetails principal) {
        return bookingService.cancel(id, principal);
    }
}
