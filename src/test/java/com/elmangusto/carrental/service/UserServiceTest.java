package com.elmangusto.carrental.service;

import com.elmangusto.carrental.dto.request.UserProfilePatchRequest;
import com.elmangusto.carrental.dto.request.UserRoleRequest;
import com.elmangusto.carrental.dto.request.UserStatusRequest;
import com.elmangusto.carrental.dto.response.UserResponse;
import com.elmangusto.carrental.exception.ResourceAlreadyExistsException;
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

        UserResponse userResponse = getUserResponse();

        when(userMapper.toResponse(user))
                .thenReturn(userResponse);

        UserResponse result = userService.getById(OWNER_ID, principal);

        assertThat(result).isEqualTo(userResponse);
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
        UserResponse userResponse = getUserResponse();

        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(user));

        when(userRepository.findAll(pageable))
                .thenReturn(userPage);

        when(userMapper.toResponse(user))
                .thenReturn(userResponse);

        Page<UserResponse> result = userService.getAll(pageable, principal);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst()).isEqualTo(userResponse);
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

        Page<UserResponse> result = userService.getAll(pageable, principal);

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

        UserResponse response = new UserResponse(
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

        UserStatusRequest request = new UserStatusRequest(UserStatus.BANNED);

        UserResponse result = userService.setBanStatus(OWNER_ID, request, principal);

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
        UserResponse userResponse = getUserResponse();

        when(userRepository.findById(OWNER_ID))
                .thenReturn(Optional.of(user));

        when(userMapper.toResponse(user))
                .thenReturn(userResponse);

        UserStatusRequest request = new UserStatusRequest(UserStatus.ACTIVE);

        UserResponse result = userService.setBanStatus(OWNER_ID, request, principal);

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

        UserStatusRequest request = new UserStatusRequest(UserStatus.BANNED);

        assertThatThrownBy(() -> userService.setBanStatus(OWNER_ID, request, principal))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("1");

        verify(userRepository).findById(OWNER_ID);
        verify(userRepository, never()).save(any(User.class));
        verify(userMapper, never()).toResponse(any(User.class));
    }

    @Test
    void setBanStatus_shouldThrowAccessDeniedException_whenActingOnSelf() {

        CustomUserDetails principal = getPrincipal(OWNER_ID, Role.ADMIN);

        UserStatusRequest request = new UserStatusRequest(UserStatus.BANNED);

        assertThatThrownBy(() -> userService.setBanStatus(OWNER_ID, request, principal))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("yourself");

        verifyNoInteractions(userRepository, userMapper);
    }

    @Test
    void setRole_shouldUpdateRole_whenUserExists() {

        User user = getUser();
        User saved = getUser();
        saved.setRole(Role.ADMIN);
        CustomUserDetails principal = getPrincipal(OTHER_USER_ID, Role.SUPER_ADMIN);

        UserResponse response = new UserResponse(
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

        UserRoleRequest request = new UserRoleRequest(Role.ADMIN);

        UserResponse result = userService.setRole(OWNER_ID, request, principal);

        assertThat(result).isNotNull();
        assertThat(result.role()).isEqualTo(Role.ADMIN);

        verify(userRepository).findById(OWNER_ID);
        verify(userRepository).save(user);
        verify(userMapper).toResponse(saved);
    }

    @Test
    void setRole_shouldThrowResourceNotFoundException_whenUserDoesNotExist() {

        CustomUserDetails principal = getPrincipal(OTHER_USER_ID, Role.SUPER_ADMIN);

        when(userRepository.findById(OWNER_ID))
                .thenReturn(Optional.empty());

        UserRoleRequest request = new UserRoleRequest(Role.ADMIN);

        assertThatThrownBy(() -> userService.setRole(OWNER_ID, request, principal))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("1");

        verify(userRepository).findById(OWNER_ID);
        verify(userRepository, never()).save(any(User.class));
        verify(userMapper, never()).toResponse(any(User.class));
    }

    @Test
    void setRole_shouldThrowAccessDeniedException_whenActingOnSelf() {

        CustomUserDetails principal = getPrincipal(OWNER_ID, Role.SUPER_ADMIN);

        UserRoleRequest request = new UserRoleRequest(Role.ADMIN);

        assertThatThrownBy(() -> userService.setRole(OWNER_ID, request, principal))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("own role");

        verifyNoInteractions(userRepository, userMapper);
    }

    @Test
    void updateProfile_shouldUpdateAllFields_whenUserUpdatesOwnProfile() {

        CustomUserDetails principal = getPrincipal(OWNER_ID, Role.USER);

        User user = getUser();
        User saved = getUser();
        saved.setFirstName("John");
        saved.setLastName("Doe");
        saved.setPhoneNumber("380672222222");

        UserResponse response = new UserResponse(
                1L,
                "gmail",
                "John",
                "Doe",
                "+380672222222",
                "BobNikson_21",
                BigDecimal.ZERO,
                Role.USER,
                UserStatus.ACTIVE
        );

        when(userRepository.findById(OWNER_ID))
                .thenReturn(Optional.of(user));

        when(userRepository.findByPhoneNumber("380672222222"))
                .thenReturn(Optional.empty());

        when(userRepository.save(user))
                .thenReturn(saved);

        when(userMapper.toResponse(saved))
                .thenReturn(response);

        UserProfilePatchRequest request = new UserProfilePatchRequest("John", "Doe", "380672222222");

        UserResponse result = userService.updateProfile(OWNER_ID, request, principal);

        assertThat(result).isNotNull();
        assertThat(result.firstName()).isEqualTo("John");
        assertThat(result.lastName()).isEqualTo("Doe");
        assertThat(result.phoneNumber()).isEqualTo("+380672222222");

        verify(userRepository).findById(OWNER_ID);
        verify(userRepository).findByPhoneNumber("380672222222");
        verify(userRepository).save(user);
        verify(userMapper).toResponse(saved);
    }

    @Test
    void updateProfile_shouldUpdateOnlyProvidedFields_whenSomeFieldsAreNull() {

        CustomUserDetails principal = getPrincipal(OWNER_ID, Role.USER);

        User user = getUser();
        User saved = getUser();
        saved.setFirstName("John");

        UserResponse response = new UserResponse(
                1L,
                "gmail",
                "John",
                "Nikson",
                "+380671111111",
                "BobNikson_21",
                BigDecimal.ZERO,
                Role.USER,
                UserStatus.ACTIVE
        );

        when(userRepository.findById(OWNER_ID))
                .thenReturn(Optional.of(user));

        when(userRepository.save(user))
                .thenReturn(saved);

        when(userMapper.toResponse(saved))
                .thenReturn(response);

        UserProfilePatchRequest request = new UserProfilePatchRequest("John", null, null);

        UserResponse result = userService.updateProfile(OWNER_ID, request, principal);

        assertThat(result).isNotNull();
        assertThat(result.firstName()).isEqualTo("John");

        verify(userRepository).findById(OWNER_ID);
        verify(userRepository, never()).findByPhoneNumber(any());
        verify(userRepository).save(user);
        verify(userMapper).toResponse(saved);
    }

    @Test
    void updateProfile_shouldThrowAccessDeniedException_whenActingOnAnotherUser() {

        CustomUserDetails principal = getPrincipal(OTHER_USER_ID, Role.USER);

        UserProfilePatchRequest request = new UserProfilePatchRequest("John", "Doe", "380672222222");

        assertThatThrownBy(() -> userService.updateProfile(OWNER_ID, request, principal))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("own profile");

        verifyNoInteractions(userRepository, userMapper);
    }

    @Test
    void updateProfile_shouldThrowResourceNotFoundException_whenUserDoesNotExist() {

        CustomUserDetails principal = getPrincipal(OWNER_ID, Role.USER);

        when(userRepository.findById(OWNER_ID))
                .thenReturn(Optional.empty());

        UserProfilePatchRequest request = new UserProfilePatchRequest("John", "Doe", "380672222222");

        assertThatThrownBy(() -> userService.updateProfile(OWNER_ID, request, principal))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("1");

        verify(userRepository).findById(OWNER_ID);
        verify(userRepository, never()).save(any(User.class));
        verify(userMapper, never()).toResponse(any(User.class));
    }

    @Test
    void updateProfile_shouldThrowResourceAlreadyExistsException_whenPhoneNumberTakenByAnotherUser() {

        CustomUserDetails principal = getPrincipal(OWNER_ID, Role.USER);

        User user = getUser();
        User otherUser = User.builder()
                .id(OTHER_USER_ID)
                .phoneNumber("380672222222")
                .build();

        when(userRepository.findById(OWNER_ID))
                .thenReturn(Optional.of(user));

        when(userRepository.findByPhoneNumber("380672222222"))
                .thenReturn(Optional.of(otherUser));

        UserProfilePatchRequest request = new UserProfilePatchRequest(null, null, "380672222222");

        assertThatThrownBy(() -> userService.updateProfile(OWNER_ID, request, principal))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessageContaining("380672222222");

        verify(userRepository).findById(OWNER_ID);
        verify(userRepository).findByPhoneNumber("380672222222");
        verify(userRepository, never()).save(any(User.class));
        verify(userMapper, never()).toResponse(any(User.class));
    }

    @Test
    void updateProfile_shouldUpdatePhoneNumber_whenPhoneNumberBelongsToSameUser() {

        CustomUserDetails principal = getPrincipal(OWNER_ID, Role.USER);

        User user = getUser();
        User saved = getUser();
        saved.setPhoneNumber("380671111111");

        UserResponse response = getUserResponse();

        when(userRepository.findById(OWNER_ID))
                .thenReturn(Optional.of(user));

        when(userRepository.findByPhoneNumber("380671111111"))
                .thenReturn(Optional.of(user));

        when(userRepository.save(user))
                .thenReturn(saved);

        when(userMapper.toResponse(saved))
                .thenReturn(response);

        UserProfilePatchRequest request = new UserProfilePatchRequest(null, null, "380671111111");

        UserResponse result = userService.updateProfile(OWNER_ID, request, principal);

        assertThat(result).isNotNull();

        verify(userRepository).findById(OWNER_ID);
        verify(userRepository).findByPhoneNumber("380671111111");
        verify(userRepository).save(user);
        verify(userMapper).toResponse(saved);
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

    private static UserResponse getUserResponse() {
        return new UserResponse(
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