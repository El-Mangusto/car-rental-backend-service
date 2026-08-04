package com.elmangusto.carrental.controller.api.v1;

import com.elmangusto.carrental.dto.request.LoginRequest;
import com.elmangusto.carrental.dto.request.RegisterUserRequest;
import com.elmangusto.carrental.dto.response.AuthResponse;
import com.elmangusto.carrental.dto.response.UserResponse;
import com.elmangusto.carrental.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@RequestBody @Valid RegisterUserRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody @Valid LoginRequest request) {
        return authService.login(request);
    }
}