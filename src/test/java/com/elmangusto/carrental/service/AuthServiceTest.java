package com.elmangusto.carrental.service;

import com.elmangusto.carrental.dto.request.LoginRequest;
import com.elmangusto.carrental.dto.request.RegisterUserRequest;
import com.elmangusto.carrental.dto.response.AuthResponse;
import com.elmangusto.carrental.dto.response.UserResponse;
import com.elmangusto.carrental.entity.User;
import com.elmangusto.carrental.entity.enums.Role;
import com.elmangusto.carrental.entity.enums.UserStatus;
import com.elmangusto.carrental.exception.ResourceAlreadyExistsException;
import com.elmangusto.carrental.mapper.UserMapper;
import com.elmangusto.carrental.repository.UserRepository;
import com.elmangusto.carrental.security.CustomUserDetails;
import com.elmangusto.carrental.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String LOGIN = "BobNikson_21";
    private static final String PASSWORD = "12345678";
    private static final String TOKEN = "generated.jwt.token";

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    @Test
    void registration_shouldSaveUser_whenLoginIsUnique() {

        RegisterUserRequest request = getRegisterUserRequest();

        User user = new User();
        User userSaved = new User();
        UserResponse userResponse = getUserResponse();

        when(userRepository.existsByLogin("BobNikson_21"))
                .thenReturn(false);

        when(userMapper.toEntity(request))
                .thenReturn(user);

        when(passwordEncoder.encode(request.password()))
                .thenReturn("encodedPassword");

        when(userRepository.save(user))
                .thenReturn(userSaved);

        when(userMapper.toResponse(userSaved))
                .thenReturn(userResponse);

        UserResponse result = authService.register(request);

        assertThat(result).isNotNull();
        assertThat(result.login())
                .isEqualTo("BobNikson_21");

        verify(userRepository).existsByLogin("BobNikson_21");
        verify(userRepository).save(user);
        verify(userMapper).toEntity(request);
        verify(userMapper).toResponse(userSaved);
    }

    @Test
    void registration_shouldThrowResourceAlreadyExistsException_whenLoginAlreadyExists() {

        RegisterUserRequest request = getRegisterUserRequest();

        when(userRepository.existsByLogin("BobNikson_21"))
                .thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessageContaining("BobNikson_21");

        verify(userRepository).existsByLogin("BobNikson_21");

        verify(userRepository, never()).save(any());
        verify(userMapper, never()).toEntity(any());
        verify(userMapper, never()).toResponse(any());
    }

    @Test
    void registration_shouldThrowResourceAlreadyExistsException_whenEmailAlreadyExists() {
        RegisterUserRequest request = getRegisterUserRequest();

        when(userRepository.existsByLogin(request.login())).thenReturn(false);
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessageContaining(request.email());

        verify(userRepository, never()).save(any());
    }

    @Test
    void registration_shouldThrowResourceAlreadyExistsException_whenPhoneNumberAlreadyExists() {
        RegisterUserRequest request = getRegisterUserRequest();

        when(userRepository.existsByLogin(request.login())).thenReturn(false);
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(request.phoneNumber())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessageContaining(request.phoneNumber());

        verify(userRepository, never()).save(any());
    }

    @Test
    void login_shouldReturnToken_whenCredentialsAreValid() {

        LoginRequest request = new LoginRequest(LOGIN, PASSWORD);
        CustomUserDetails userDetails = new CustomUserDetails(getActiveUser());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn(TOKEN);

        AuthResponse response = authService.login(request);

        assertThat(response.token()).isEqualTo(TOKEN);

        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken(LOGIN, PASSWORD));
        verify(jwtService).generateToken(userDetails);
    }

    @Test
    void login_shouldThrowBadCredentials_whenPasswordIsWrong() {

        LoginRequest request = new LoginRequest(LOGIN, "wrongPassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);

        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void login_shouldThrowBadCredentials_whenLoginDoesNotExist() {

        LoginRequest request = new LoginRequest("unknownLogin", PASSWORD);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);

        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void login_shouldThrowDisabled_whenUserIsBanned() {

        LoginRequest request = new LoginRequest(LOGIN, PASSWORD);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new DisabledException("User is disabled"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(DisabledException.class);

        verify(jwtService, never()).generateToken(any());
    }

    private static RegisterUserRequest getRegisterUserRequest() {
        return new RegisterUserRequest(
                "test@gmail.com",
                "Bob",
                "Nikson",
                "+380671111111",
                "BobNikson_21",
                "12345678"
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

    private User getActiveUser() {
        User user = new User();
        user.setId(1L);
        user.setLogin(LOGIN);
        user.setPassword("encodedPassword");
        user.setRole(Role.USER);
        user.setStatus(UserStatus.ACTIVE);
        return user;
    }
}