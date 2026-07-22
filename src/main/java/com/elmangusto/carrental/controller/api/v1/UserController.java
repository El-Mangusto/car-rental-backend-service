package com.elmangusto.carrental.controller.api.v1;

import com.elmangusto.carrental.dto.request.RegisterUserRequest;
import com.elmangusto.carrental.dto.response.UserResponse;
import com.elmangusto.carrental.security.CustomUserDetails;
import com.elmangusto.carrental.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @GetMapping
    public Page<UserResponse> getAll(
            @ParameterObject
            @PageableDefault(size = 10, sort = "login")
            Pageable pageable) {
        return userService.getAll(pageable);
    }

    @GetMapping("/me")
    public UserResponse getCurrentUser(@AuthenticationPrincipal CustomUserDetails principal) {
        return userService.getById(principal.getId(), principal);
    }

    @GetMapping("/{id}")
    public UserResponse getById(@PathVariable Long id,
                                @AuthenticationPrincipal CustomUserDetails principal) {
        return userService.getById(id, principal);
    }

}
