package com.elmangusto.carrental.controller.api.v1;

import com.elmangusto.carrental.dto.response.UserAdminResponse;
import com.elmangusto.carrental.security.CustomUserDetails;
import com.elmangusto.carrental.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public UserAdminResponse getCurrentUser(@AuthenticationPrincipal CustomUserDetails principal) {
        return userService.getById(principal.getId(), principal);
    }
}
