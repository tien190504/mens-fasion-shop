package com.powerranger.fashion_shop_backend.controller;

import com.powerranger.fashion_shop_backend.common.ApiResponse;
import com.powerranger.fashion_shop_backend.dto.response.PasswordResetTokenResponse;
import com.powerranger.fashion_shop_backend.mapper.PasswordResetTokenMapper;
import com.powerranger.fashion_shop_backend.repository.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/password-reset-tokens")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class PasswordResetTokenController {

    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PasswordResetTokenResponse>>> getAll() {
        List<PasswordResetTokenResponse> response = passwordResetTokenRepository.findAll().stream()
                .map(PasswordResetTokenMapper::toResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
