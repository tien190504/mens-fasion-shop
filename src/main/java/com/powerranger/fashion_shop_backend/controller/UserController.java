package com.powerranger.fashion_shop_backend.controller;

import com.powerranger.fashion_shop_backend.common.ApiResponse;
import com.powerranger.fashion_shop_backend.dto.response.UserResponse;
import com.powerranger.fashion_shop_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(JwtAuthenticationToken jwt) {
        UserResponse response = userService.getProfile(jwt.getName());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> listAll() {
        List<UserResponse> response = userService.listAllUsers();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
