package com.elmangusto.carrental.config;

import com.elmangusto.carrental.entity.User;
import com.elmangusto.carrental.entity.enums.Role;
import com.elmangusto.carrental.entity.enums.UserStatus;
import com.elmangusto.carrental.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminBootstrapRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.bootstrap.login}")
    private String login;

    @Value("${admin.bootstrap.password}")
    private String rawPassword;

    @Value("${admin.bootstrap.email}")
    private String email;

    @Value("${admin.bootstrap.firstName}")
    private String firstName;

    @Value("${admin.bootstrap.lastName}")
    private String lastName;

    @Value("${admin.bootstrap.phoneNumber}")
    private String phoneNumber;

    @Override
    public void run(String... args) {
        if (userRepository.existsByRole(Role.SUPER_ADMIN)) {
            return;
        }

        User admin = User.builder()
                .login(login)
                .password(passwordEncoder.encode(rawPassword))
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .phoneNumber(phoneNumber)
                .role(Role.SUPER_ADMIN)
                .status(UserStatus.ACTIVE)
                .build();

        userRepository.save(admin);
        log.warn("Bootstrap admin created with login={}", login);
    }
}
