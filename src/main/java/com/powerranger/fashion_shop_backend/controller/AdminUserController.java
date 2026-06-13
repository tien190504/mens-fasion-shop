package com.powerranger.fashion_shop_backend.controller;

import com.powerranger.fashion_shop_backend.common.ApiResponse;
import com.powerranger.fashion_shop_backend.dto.request.AdminResetPasswordRequest;
import com.powerranger.fashion_shop_backend.dto.request.AdminUserCreateRequest;
import com.powerranger.fashion_shop_backend.dto.request.AdminUserUpdateRequest;
import com.powerranger.fashion_shop_backend.dto.response.UserResponse;
import com.powerranger.fashion_shop_backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserResponse>>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Boolean admin,
            Pageable pageable) {
        Page<UserResponse> response = userService.searchUsersForAdmin(keyword, active, admin, pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getById(@PathVariable Long id) {
        UserResponse response = userService.getUserByIdForAdmin(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> create(@Valid @RequestBody AdminUserCreateRequest request) {
        UserResponse response = userService.createUserForAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("User created", response));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> update(
            @PathVariable Long id,
            JwtAuthenticationToken jwt,
            @Valid @RequestBody AdminUserUpdateRequest request) {
        UserResponse response = userService.updateUserForAdmin(id, jwt.getName(), request);
        return ResponseEntity.ok(ApiResponse.ok("User updated", response));
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @PathVariable Long id,
            @Valid @RequestBody AdminResetPasswordRequest request) {
        userService.resetPasswordForAdmin(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Password reset", null));
    }
}
