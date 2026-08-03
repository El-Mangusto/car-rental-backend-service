package com.elmangusto.carrental.controller.admin.v1;


import com.elmangusto.carrental.dto.request.UserRoleRequest;
import com.elmangusto.carrental.dto.request.UserStatusRequest;
import com.elmangusto.carrental.dto.response.UserResponse;
import com.elmangusto.carrental.security.CustomUserDetails;
import com.elmangusto.carrental.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public UserResponse getById(@PathVariable Long id,
                                @AuthenticationPrincipal CustomUserDetails principal) {
        return userService.getById(id, principal);
    }

    @GetMapping
    public Page<UserResponse> getAll(
            @ParameterObject
            @PageableDefault(size = 10, sort = "login")
            Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return userService.getAll(pageable, principal);
    }

    @PatchMapping("/{id}/ban")
    public UserResponse setBanStatus(@PathVariable Long id,
                                     @RequestBody UserStatusRequest request,
                                     @AuthenticationPrincipal CustomUserDetails principal) {
        return userService.setBanStatus(id, request, principal);
    }

    @PatchMapping("/{id}/role")
    public UserResponse setRole(@PathVariable Long id,
                                @RequestBody UserRoleRequest request,
                                @AuthenticationPrincipal CustomUserDetails principal) {
        return userService.setRole(id, request, principal);
    }
}
