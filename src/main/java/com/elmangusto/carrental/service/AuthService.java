package com.elmangusto.carrental.service;

import com.elmangusto.carrental.dto.request.LoginRequest;
import com.elmangusto.carrental.dto.request.RegisterUserRequest;
import com.elmangusto.carrental.dto.response.AuthResponse;
import com.elmangusto.carrental.dto.response.UserResponse;
import com.elmangusto.carrental.entity.User;
import com.elmangusto.carrental.exception.ResourceAlreadyExistsException;
import com.elmangusto.carrental.mapper.UserMapper;
import com.elmangusto.carrental.repository.UserRepository;
import com.elmangusto.carrental.security.CustomUserDetails;
import com.elmangusto.carrental.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public UserResponse register(RegisterUserRequest request) {

        log.info("Registering user with login={}", request.login());

        if (userRepository.existsByLogin(request.login())) {
            throw new ResourceAlreadyExistsException(
                    "User with login '%s' already exists".formatted(request.login()));
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new ResourceAlreadyExistsException(
                    "User with email '%s' already exists".formatted(request.email()));
        }
        if (userRepository.existsByPhoneNumber(request.phoneNumber())) {
            throw new ResourceAlreadyExistsException(
                    "User with phone number '%s' already exists".formatted(request.phoneNumber()));
        }

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.password()));

        User saved = userRepository.save(user);

        log.info("User registered successfully. id={}, login={}", saved.getId(), saved.getLogin());

        return userMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {

        log.info("Login attempt for login={}", request.login());

        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.login(), request.password()));

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        log.info("Login successful. userId={}, login={}", userDetails.getId(), userDetails.getUsername());

        return new AuthResponse(jwtService.generateToken(userDetails));
    }
}
