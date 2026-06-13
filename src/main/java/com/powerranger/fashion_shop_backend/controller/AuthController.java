package com.powerranger.fashion_shop_backend.controller;

import com.powerranger.fashion_shop_backend.common.ApiResponse;
import com.powerranger.fashion_shop_backend.dto.request.LoginRequest;
import com.powerranger.fashion_shop_backend.dto.request.RegisterRequest;
import com.powerranger.fashion_shop_backend.dto.request.UpdateProfileRequest;
import com.powerranger.fashion_shop_backend.dto.response.AuthResponse;
import com.powerranger.fashion_shop_backend.dto.response.UserResponse;
import com.powerranger.fashion_shop_backend.service.AuthService;
import com.powerranger.fashion_shop_backend.service.UserService;
import com.powerranger.fashion_shop_backend.dto.request.ForgotPasswordRequest;
import com.powerranger.fashion_shop_backend.dto.request.PasswordResetRequest;
import com.powerranger.fashion_shop_backend.service.PasswordResetTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final PasswordResetTokenService passwordResetTokenService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Registration successful", response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        return ResponseEntity.ok(ApiResponse.ok("Logout successful", null));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> me(JwtAuthenticationToken jwt) {
        UserResponse response = userService.getProfile(jwt.getName());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateMe(
            JwtAuthenticationToken jwt,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserResponse response = userService.updateProfile(jwt.getName(), request);
        return ResponseEntity.ok(ApiResponse.ok("Profile updated", response));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        String token = passwordResetTokenService.generateToken(request.email());
        // In a real application, we would send this token via email.
        // For local development and verification, we return the token in the message.
        return ResponseEntity.ok(ApiResponse.ok("Password reset token generated: " + token, token));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody PasswordResetRequest request) {
        passwordResetTokenService.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.ok(ApiResponse.ok("Password has been reset successfully", null));
    }
}
