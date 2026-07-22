package com.elmangusto.carrental.service;

import com.elmangusto.carrental.dto.request.RegisterUserRequest;
import com.elmangusto.carrental.dto.response.UserResponse;
import com.elmangusto.carrental.entity.User;
import com.elmangusto.carrental.entity.enums.Role;
import com.elmangusto.carrental.exception.ResourceAlreadyExistsException;
import com.elmangusto.carrental.exception.ResourceNotFoundException;
import com.elmangusto.carrental.mapper.UserMapper;
import com.elmangusto.carrental.repository.UserRepository;
import com.elmangusto.carrental.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
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
    public UserResponse getById(Long id, CustomUserDetails principal) {
        boolean isSelf = principal.getId().equals(id);
        boolean isAdmin = principal.user().getRole() == Role.ADMIN;

        if (!isSelf && !isAdmin) {
            throw new AccessDeniedException("You are not allowed to view this user");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        return userMapper.toResponse(user);
    }
}