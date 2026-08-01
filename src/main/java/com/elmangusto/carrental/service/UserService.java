package com.elmangusto.carrental.service;

import com.elmangusto.carrental.dto.response.UserAdminResponse;
import com.elmangusto.carrental.entity.User;
import com.elmangusto.carrental.entity.enums.Role;
import com.elmangusto.carrental.entity.enums.UserStatus;
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
    public Page<UserAdminResponse> getAll(Pageable pageable, CustomUserDetails principal) {
        boolean isAdmin = principal.user().getRole() == Role.ADMIN;

        if (!isAdmin) {
            throw new AccessDeniedException("Access denied");
        }

        return userRepository.findAll(pageable)
                .map(userMapper::toResponse);
    }


    @Transactional(readOnly = true)
    public UserAdminResponse getById(Long id, CustomUserDetails principal) {
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
    public UserAdminResponse setBanStatus(Long id, UserStatus newStatus, CustomUserDetails principal) {

        log.info("Changing status for userId={} to newStatus={}", id, newStatus);

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

        if (user.getStatus() == newStatus) {
            log.info("user id={} already has status={}, no changes applied", id, newStatus);
            return userMapper.toResponse(user);
        }

        user.setStatus(newStatus);

        User saved = userRepository.save(user);

        log.info("User id={} status changed successfully to {}", saved.getId(), saved.getStatus());

        return userMapper.toResponse(saved);
    }

    @Transactional
    public UserAdminResponse setRole(Long id, Role newRole, CustomUserDetails principal) {

        log.info("Changing role for userId={} to newRole={}", id, newRole);

        boolean isSelf = principal.getId().equals(id);

        if (isSelf && newRole.ordinal() < principal.user().getRole().ordinal()) {
            throw new AccessDeniedException("You cannot downgrade your own role");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        user.setRole(newRole);

        User saved = userRepository.save(user);

        log.info("User id={} role changed successfully to {}", saved.getId(), saved.getRole());

        return userMapper.toResponse(saved);
    }
}