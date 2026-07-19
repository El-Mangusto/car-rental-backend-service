package com.elmangusto.carrental.service;

import com.elmangusto.carrental.dto.request.UserAuthRequest;
import com.elmangusto.carrental.dto.response.UserResponse;
import com.elmangusto.carrental.entity.User;
import com.elmangusto.carrental.exception.ResourceAlreadyExistsException;
import com.elmangusto.carrental.exception.ResourceNotFoundException;
import com.elmangusto.carrental.mapper.UserMapper;
import com.elmangusto.carrental.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Page<UserResponse> getAll(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(userMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        return userMapper.toResponse(user);
    }

    public UserResponse create(UserAuthRequest request) {

        log.info("Creating user with login={}", request.login());

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

        log.info("User created successfully. id={}, login={}", saved.getId(), saved.getLogin());

        return userMapper.toResponse(saved);
    }
}