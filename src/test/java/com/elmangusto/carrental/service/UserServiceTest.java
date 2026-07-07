package com.elmangusto.carrental.service;

import com.elmangusto.carrental.exception.ResourceNotFoundException;
import com.elmangusto.carrental.mapper.UserMapper;
import com.elmangusto.carrental.repository.UserRepository;
import com.elmangusto.carrental.dto.request.UserAuthRequest;
import com.elmangusto.carrental.dto.response.UserResponse;
import com.elmangusto.carrental.entity.User;
import com.elmangusto.carrental.entity.enums.Role;
import com.elmangusto.carrental.entity.enums.UserStatus;
import com.elmangusto.carrental.exception.ResourceAlreadyExistsException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    @Test
    void create_shouldSaveUser_whenLoginIsUnique() {

        UserAuthRequest request = getUserAuthRequest();

        User user = new User();
        User userSaved = new User();
        UserResponse userResponse = getUserResponse();

        when(userRepository.existsByLogin("BobNikson_21"))
                .thenReturn(false);

        when(userMapper.toEntity(request))
                .thenReturn(user);

        when(userRepository.save(user))
                .thenReturn(userSaved);

        when(userMapper.toResponse(userSaved))
                .thenReturn(userResponse);

        UserResponse result = userService.create(request);

        assertThat(result).isNotNull();
        assertThat(result.login())
                .isEqualTo("BobNikson_21");

        verify(userRepository).existsByLogin("BobNikson_21");
        verify(userRepository).save(user);
        verify(userMapper).toEntity(request);
        verify(userMapper).toResponse(userSaved);

    }

    @Test
    void create_shouldThrowResourceAlreadyExistsException_whenLognAlreadyExists() {

        UserAuthRequest request = getUserAuthRequest();

        when(userRepository.existsByLogin("BobNikson_21"))
                .thenReturn(true);

        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessageContaining("BobNikson_21");

        verify(userRepository).existsByLogin("BobNikson_21");

        verify(userRepository, never()).save(any());
        verify(userMapper, never()).toEntity(any());
        verify(userMapper, never()).toResponse(any());
    }

    @Test
    void getById_shouldReturnUser_whenUserExists() {

        User user = getUser();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        UserResponse userResponse = getUserResponse();

        when(userMapper.toResponse(user))
                .thenReturn(userResponse);

        UserResponse result = userService.getById(1L);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(userResponse);

        verify(userRepository).findById(1L);
        verify(userMapper).toResponse(user);
    }

    @Test
    void getById_shouldThrowResourceNotFoundException_whenUserDoesNotExist() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getById(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("1");

        verify(userRepository).findById(1L);
    }

    @Test
    void getAll_shouldReturnPageOfCars_whenCarsExist() {

        User user = getUser();
        UserResponse userResponse = getUserResponse();

        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(user));

        when(userRepository.findAll(pageable))
                .thenReturn(userPage);

        when(userMapper.toResponse(user))
                .thenReturn(userResponse);

        Page<UserResponse> result = userService.getAll(pageable);

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
        Page<User> emptyPage = Page.empty(pageable);

        when(userRepository.findAll(pageable))
                .thenReturn(emptyPage);

        Page<UserResponse> result = userService.getAll(pageable);

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(userRepository).findAll(pageable);
        verifyNoInteractions(userMapper);
    }


    private static User getUser() {
        return User.builder()
                .id(1L)
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

    private static UserAuthRequest getUserAuthRequest() {
        return new UserAuthRequest(
                "test@gmail.com",
                "Bob",
                "Nikson",
                "+380671111111",
                "BobNikson_21",
                "12345678",
                Role.USER
        );
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