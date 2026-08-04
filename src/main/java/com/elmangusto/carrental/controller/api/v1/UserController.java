package com.elmangusto.carrental.controller.api.v1;

import com.elmangusto.carrental.dto.request.UserProfilePatchRequest;
import com.elmangusto.carrental.dto.response.UserResponse;
import com.elmangusto.carrental.security.CustomUserDetails;
import com.elmangusto.carrental.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public UserResponse getCurrentUser(@AuthenticationPrincipal CustomUserDetails principal) {
        return userService.getById(principal.getId(), principal);
    }

    @PatchMapping("/{id}")
    public UserResponse updateProfile(@PathVariable Long id,
                                      @RequestBody @Valid UserProfilePatchRequest request,
                                      CustomUserDetails principal) {
        return userService.updateProfile(id, request, principal);
    }
}
