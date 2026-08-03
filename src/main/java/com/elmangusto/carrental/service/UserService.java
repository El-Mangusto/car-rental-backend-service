package com.elmangusto.carrental.service;

import com.elmangusto.carrental.dto.request.UserProfilePatchRequest;
import com.elmangusto.carrental.dto.request.UserRoleRequest;
import com.elmangusto.carrental.dto.request.UserStatusRequest;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public Page<UserResponse> getAll(Pageable pageable, CustomUserDetails principal) {
        boolean isAdmin = principal.user().getRole() == Role.ADMIN;

        if (!isAdmin) {
            throw new AccessDeniedException("Access denied");
        }

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


    @Transactional
    public UserResponse setBanStatus(Long id, UserStatusRequest request, CustomUserDetails principal) {

        log.info("Changing status for userId={} to newStatus={}", id, request.status());

        boolean isSelf = principal.getId().equals(id);

        if (isSelf) {
            throw new AccessDeniedException("You cannot ban/unban yourself");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        boolean actorIsSuperAdmin = principal.user().getRole() == Role.SUPER_ADMIN;
        boolean targetIsPrivileged = user.getRole() != Role.USER;

        if (targetIsPrivileged && !actorIsSuperAdmin) {
            throw new AccessDeniedException("Only a super admin can change status of an admin account");
        }

        if (user.getStatus() == request.status()) {
            log.info("user id={} already has status={}, no changes applied", id, request.status());
            return userMapper.toResponse(user);
        }

        user.setStatus(request.status());

        User saved = userRepository.save(user);

        log.info("User id={} status changed successfully to {}", saved.getId(), saved.getStatus());

        return userMapper.toResponse(saved);
    }

    @Transactional
    public UserResponse setRole(Long id, UserRoleRequest request, CustomUserDetails principal) {

        log.info("Changing role for userId={} to newRole={}", id, request.role());

        if (principal.getId().equals(id)) {
            throw new AccessDeniedException("You cannot change your own role");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        user.setRole(request.role());

        User saved = userRepository.save(user);

        log.info("User id={} role changed successfully to {}", saved.getId(), saved.getRole());

        return userMapper.toResponse(saved);
    }

    public UserResponse updateProfile(Long id, UserProfilePatchRequest request, CustomUserDetails principal) {

        log.info("Updating profile for userId={} to newFirstName={}, newLastName={}, newPhoneNumber={}",
                id, request.firstName(), request.lastName(), request.phoneNumber());

        if (!principal.getId().equals(id)) {
            throw new AccessDeniedException("You can only update your own profile");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        if (request.firstName() != null) {
            user.setFirstName(request.firstName());
        }

        if (request.lastName() != null) {
            user.setLastName(request.lastName());
        }

        if (request.phoneNumber() != null) {
            validatePhoneNumberAvailable(request.phoneNumber(), id);
            user.setPhoneNumber(request.phoneNumber());
        }

        User saved = userRepository.save(user);

        log.info("User id={} profile changed successfully to newFirstName={}, newLastName={}, newPhoneNumber={}",
                id, request.firstName(), request.lastName(), request.phoneNumber());

        return userMapper.toResponse(saved);
    }

    private void validatePhoneNumberAvailable(String phoneNumber, Long currentUserId) {
        userRepository.findByPhoneNumber(phoneNumber)
                .filter(existing -> !existing.getId().equals(currentUserId))
                .ifPresent(existing -> {
                    throw new ResourceAlreadyExistsException(
                            "User with phone '%s' already exists".formatted(phoneNumber));
                });
    }
}