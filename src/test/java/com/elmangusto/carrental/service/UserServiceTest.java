package com.elmangusto.carrental.service;

import com.elmangusto.carrental.dto.response.UserAdminResponse;
import com.elmangusto.carrental.exception.ResourceNotFoundException;
import com.elmangusto.carrental.mapper.UserMapper;
import com.elmangusto.carrental.repository.UserRepository;
import com.elmangusto.carrental.entity.User;
import com.elmangusto.carrental.entity.enums.Role;
import com.elmangusto.carrental.entity.enums.UserStatus;
import com.elmangusto.carrental.security.CustomUserDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final Long OWNER_ID = 1L;
    private static final Long OTHER_USER_ID = 2L;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    @Test
    void getById_shouldReturnUser_whenRequestedByAdmin() {

        User user = getUser();
        CustomUserDetails principal = getPrincipal(OTHER_USER_ID, Role.ADMIN);

        when(userRepository.findById(OWNER_ID))
                .thenReturn(Optional.of(user));

        UserAdminResponse userAdminResponse = getUserAdminResponse();

        when(userMapper.toResponse(user))
                .thenReturn(userAdminResponse);

        UserAdminResponse result = userService.getById(OWNER_ID, principal);

        assertThat(result).isEqualTo(userAdminResponse);
    }

    @Test
    void getById_shouldThrowAccessDeniedException_whenRequestedByAnotherUser() {

        CustomUserDetails principal = getPrincipal(OTHER_USER_ID, Role.USER);

        assertThatThrownBy(() -> userService.getById(OWNER_ID, principal))
                .isInstanceOf(AccessDeniedException.class);

        verifyNoInteractions(userRepository, userMapper);
    }

    @Test
    void getById_shouldThrowResourceNotFoundException_whenUserDoesNotExist() {

        CustomUserDetails principal = getPrincipal(OTHER_USER_ID, Role.ADMIN);

        when(userRepository.findById(OWNER_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getById(OWNER_ID, principal))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("1");

        verify(userRepository).findById(OWNER_ID);
    }

    @Test
    void getAll_shouldReturnPageOfCars_whenCarsExist() {

        User user = getUser();
        CustomUserDetails principal = getPrincipal(OTHER_USER_ID, Role.ADMIN);
        UserAdminResponse userAdminResponse = getUserAdminResponse();

        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(user));

        when(userRepository.findAll(pageable))
                .thenReturn(userPage);

        when(userMapper.toResponse(user))
                .thenReturn(userAdminResponse);

        Page<UserAdminResponse> result = userService.getAll(pageable, principal);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst()).isEqualTo(userAdminResponse);
        assertThat(result.getTotalElements()).isEqualTo(1);

        verify(userRepository).findAll(pageable);
        verify(userMapper).toResponse(user);
        verifyNoMoreInteractions(userRepository, userMapper);
    }

    @Test
    void getAll_shouldReturnEmptyPage_whenNoCarsExist() {

        Pageable pageable = PageRequest.of(0, 10);
        CustomUserDetails principal = getPrincipal(OTHER_USER_ID, Role.ADMIN);
        Page<User> emptyPage = Page.empty(pageable);

        when(userRepository.findAll(pageable))
                .thenReturn(emptyPage);

        Page<UserAdminResponse> result = userService.getAll(pageable, principal);

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(userRepository).findAll(pageable);
        verifyNoInteractions(userMapper);
    }

    @Test
    void setBanStatus_shouldUpdateStatus_whenUserExists() {
        CustomUserDetails principal = getPrincipal(OTHER_USER_ID, Role.ADMIN);

        User user = getUser();
        User saved = getUser();
        saved.setStatus(UserStatus.BANNED);

        UserAdminResponse response = new UserAdminResponse(
                1L,
                "gmail",
                "Bob",
                "Nikson",
                "+380671111111",
                "BobNikson_21",
                BigDecimal.ZERO,
                Role.USER,
                UserStatus.BANNED
        );

        when(userRepository.findById(OWNER_ID))
                .thenReturn(Optional.of(user));

        when(userRepository.save(user))
                .thenReturn(saved);

        when(userMapper.toResponse(saved))
                .thenReturn(response);

        UserAdminResponse result = userService.setBanStatus(OWNER_ID, UserStatus.BANNED, principal);

        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo(UserStatus.BANNED);

        verify(userRepository).findById(OWNER_ID);
        verify(userRepository).save(user);
        verify(userMapper).toResponse(saved);
    }

    @Test
    void setBanStatus_shouldReturnUnchanged_whenStatusIsSame() {
        CustomUserDetails principal = getPrincipal(OTHER_USER_ID, Role.ADMIN);

        User user = getUser();
        UserAdminResponse userAdminResponse = getUserAdminResponse();

        when(userRepository.findById(OWNER_ID))
                .thenReturn(Optional.of(user));

        when(userMapper.toResponse(user))
                .thenReturn(userAdminResponse);

        UserAdminResponse result = userService.setBanStatus(OWNER_ID, UserStatus.ACTIVE, principal);

        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo(UserStatus.ACTIVE);

        verify(userRepository).findById(OWNER_ID);
        verify(userRepository, never()).save(any(User.class));
        verify(userMapper).toResponse(user);
    }

    @Test
    void setBanStatus_shouldThrowResourceNotFoundException_whenUserDoesNotExist() {
        CustomUserDetails principal = getPrincipal(OTHER_USER_ID, Role.ADMIN);

        when(userRepository.findById(OWNER_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.setBanStatus(OWNER_ID, UserStatus.BANNED, principal))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("1");

        verify(userRepository).findById(OWNER_ID);
        verify(userRepository, never()).save(any(User.class));
        verify(userMapper, never()).toResponse(any(User.class));
    }

    @Test
    void setRole_shouldUpdateRole_whenUserExists() {

        User user = getUser();
        User saved = getUser();
        saved.setRole(Role.ADMIN);

        UserAdminResponse response = new UserAdminResponse(
                1L,
                "gmail",
                "Bob",
                "Nikson",
                "+380671111111",
                "BobNikson_21",
                BigDecimal.ZERO,
                Role.ADMIN,
                UserStatus.ACTIVE
        );

        when(userRepository.findById(OWNER_ID))
                .thenReturn(Optional.of(user));

        when(userRepository.save(user))
                .thenReturn(saved);

        when(userMapper.toResponse(saved))
                .thenReturn(response);

        UserAdminResponse result = userService.setRole(OWNER_ID, Role.ADMIN);

        assertThat(result).isNotNull();
        assertThat(result.role()).isEqualTo(Role.ADMIN);

        verify(userRepository).findById(OWNER_ID);
        verify(userRepository).save(user);
        verify(userMapper).toResponse(saved);
    }

    @Test
    void setRole_shouldThrowResourceNotFoundException_whenUserDoesNotExist() {

        when(userRepository.findById(OWNER_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.setRole(OWNER_ID, Role.ADMIN))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("1");

        verify(userRepository).findById(OWNER_ID);
        verify(userRepository, never()).save(any(User.class));
        verify(userMapper, never()).toResponse(any(User.class));
    }


    private static User getUser() {
        return User.builder()
                .id(OWNER_ID)
                .email("test@gmail.com")
                .firstName("Bob")
                .lastName("Nikson")
                .phoneNumber("380671111111")
                .login("BobNikson_21")
                .password("12345678")
                .balance(BigDecimal.ZERO)
                .role(Role.USER)
                .status(UserStatus.ACTIVE)
                .build();
    }

    private static CustomUserDetails getPrincipal(Long userId, Role role) {
        User user = User.builder()
                .id(userId)
                .role(role)
                .build();
        return new CustomUserDetails(user);
    }

    private static UserAdminResponse getUserAdminResponse() {
        return new UserAdminResponse(
                1L,
                "gmail",
                "Bob",
                "Nikson",
                "+380671111111",
                "BobNikson_21",
                BigDecimal.ZERO,
                Role.USER,
                UserStatus.ACTIVE
        );
    }
}