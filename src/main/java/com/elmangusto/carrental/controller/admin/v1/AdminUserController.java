package com.elmangusto.carrental.controller.admin.v1;


import com.elmangusto.carrental.dto.response.UserAdminResponse;
import com.elmangusto.carrental.entity.enums.Role;
import com.elmangusto.carrental.entity.enums.UserStatus;
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
    public UserAdminResponse getById(@PathVariable Long id,
                                     @AuthenticationPrincipal CustomUserDetails principal) {
        return userService.getById(id, principal);
    }

    @GetMapping
    public Page<UserAdminResponse> getAll(
            @ParameterObject
            @PageableDefault(size = 10, sort = "login")
            Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return userService.getAll(pageable, principal);
    }

    @PatchMapping("/{id}/ban")
    public UserAdminResponse setBanStatus(@PathVariable Long id,
                                          UserStatus status,
                                          @AuthenticationPrincipal CustomUserDetails principal) {
        return userService.setBanStatus(id, status, principal);
    }

    @PatchMapping("/{id}/role")
    public UserAdminResponse setRole(@PathVariable Long id,
                                     Role role,
                                     @AuthenticationPrincipal CustomUserDetails principal) {
        return userService.setRole(id, role, principal);
    }
}
