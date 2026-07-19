package com.elmangusto.carrental.controller.api.v1;

import com.elmangusto.carrental.dto.request.LoginRequest;
import com.elmangusto.carrental.dto.response.AuthResponse;
import com.elmangusto.carrental.security.CustomUserDetails;
import com.elmangusto.carrental.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @PostMapping("/login")
    public AuthResponse login(@RequestBody @Valid LoginRequest request) {

        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.login(), request.password()));

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        return new AuthResponse(jwtService.generateToken(userDetails));
    }
}