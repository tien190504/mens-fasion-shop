package com.powerranger.fashion_shop_backend.service.impl;

import com.powerranger.fashion_shop_backend.dto.request.AdminResetPasswordRequest;
import com.powerranger.fashion_shop_backend.dto.request.AdminUserCreateRequest;
import com.powerranger.fashion_shop_backend.dto.request.AdminUserUpdateRequest;
import com.powerranger.fashion_shop_backend.dto.request.UpdateProfileRequest;
import com.powerranger.fashion_shop_backend.dto.response.UserResponse;
import com.powerranger.fashion_shop_backend.entity.User;
import com.powerranger.fashion_shop_backend.exception.BadRequestException;
import com.powerranger.fashion_shop_backend.exception.ConflictException;
import com.powerranger.fashion_shop_backend.exception.NotFoundException;
import com.powerranger.fashion_shop_backend.mapper.UserMapper;
import com.powerranger.fashion_shop_backend.repository.UserRepository;
import com.powerranger.fashion_shop_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getProfile(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .map(UserMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    @Override
    @Transactional
    public UserResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (request.fullName() != null) {
            user.setFullName(request.fullName());
        }
        if (request.phone() != null) {
            updatePhone(user, request.phone());
        }
        if (request.avatarUrl() != null) {
            user.setAvatarUrl(request.avatarUrl());
        }
        if (request.dateOfBirth() != null) {
            user.setDateOfBirth(request.dateOfBirth());
        }
        updatePasswordIfRequested(user, request);

        return UserMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> listAllUsers() {
        return userRepository.findAll().stream()
                .map(UserMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> searchUsersForAdmin(String keyword, Boolean active, Boolean admin, Pageable pageable) {
        return userRepository.searchForAdmin(normalizeKeyword(keyword), active, admin, pageable)
                .map(UserMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByIdForAdmin(Long id) {
        return UserMapper.toResponse(findUserById(id));
    }

    @Override
    @Transactional
    public UserResponse createUserForAdmin(AdminUserCreateRequest request) {
        String email = requireText(request.email(), "Email is required");
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("Email already registered");
        }

        String phone = normalizeOptional(request.phone());
        if (phone != null && userRepository.existsByPhone(phone)) {
            throw new ConflictException("Phone already registered");
        }

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFullName(normalizeOptional(request.fullName()));
        user.setPhone(phone);
        user.setAvatarUrl(normalizeOptional(request.avatarUrl()));
        user.setDateOfBirth(request.dateOfBirth());
        user.setActive(request.active() == null || request.active());
        user.setAdmin(Boolean.TRUE.equals(request.admin()));

        return UserMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse updateUserForAdmin(Long id, String currentAdminEmail, AdminUserUpdateRequest request) {
        User user = findUserById(id);
        User currentAdmin = userRepository.findByEmailIgnoreCase(currentAdminEmail)
                .orElseThrow(() -> new NotFoundException("Current admin not found"));

        boolean newActive = request.active() == null ? user.isActive() : request.active();
        boolean newAdmin = request.admin() == null ? user.isAdmin() : request.admin();
        ensureAdminUpdateAllowed(user, currentAdmin, newActive, newAdmin);

        if (request.email() != null) {
            updateEmail(user, request.email());
        }
        if (request.fullName() != null) {
            user.setFullName(normalizeOptional(request.fullName()));
        }
        if (request.phone() != null) {
            updatePhone(user, request.phone());
        }
        if (request.avatarUrl() != null) {
            user.setAvatarUrl(normalizeOptional(request.avatarUrl()));
        }
        if (request.dateOfBirth() != null) {
            user.setDateOfBirth(request.dateOfBirth());
        }
        user.setActive(newActive);
        user.setAdmin(newAdmin);

        return UserMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void resetPasswordForAdmin(Long id, AdminResetPasswordRequest request) {
        User user = findUserById(id);
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private void updateEmail(User user, String email) {
        String normalizedEmail = requireText(email, "Email is required");
        if (user.getEmail().equalsIgnoreCase(normalizedEmail)) {
            user.setEmail(normalizedEmail);
            return;
        }

        if (userRepository.existsByEmailIgnoreCaseAndIdNot(normalizedEmail, user.getId())) {
            throw new ConflictException("Email already registered");
        }

        user.setEmail(normalizedEmail);
    }

    private void updatePhone(User user, String phone) {
        String normalizedPhone = normalizeOptional(phone);
        if (Objects.equals(user.getPhone(), normalizedPhone)) {
            return;
        }

        if (normalizedPhone != null) {
            userRepository.findByPhone(normalizedPhone)
                    .filter(existingUser -> !existingUser.getId().equals(user.getId()))
                    .ifPresent(existingUser -> {
                        throw new ConflictException("Phone already registered");
                    });
        }

        user.setPhone(normalizedPhone);
    }

    private void updatePasswordIfRequested(User user, UpdateProfileRequest request) {
        boolean passwordUpdateRequested = request.currentPassword() != null || request.newPassword() != null;
        if (!passwordUpdateRequested) {
            return;
        }

        if (request.currentPassword() == null || request.newPassword() == null) {
            throw new BadRequestException("Current password and new password are required");
        }

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
    }

    private void ensureAdminUpdateAllowed(User target, User currentAdmin, boolean newActive, boolean newAdmin) {
        boolean selfUpdate = currentAdmin.getId().equals(target.getId());
        if (selfUpdate && target.isActive() && !newActive) {
            throw new BadRequestException("Admin cannot deactivate own account");
        }
        if (selfUpdate && target.isAdmin() && !newAdmin) {
            throw new BadRequestException("Admin cannot remove own admin role");
        }
        if (target.isAdmin()
                && target.isActive()
                && (!newAdmin || !newActive)
                && !userRepository.existsByAdminTrueAndActiveTrueAndIdNot(target.getId())) {
            throw new BadRequestException("At least one active admin is required");
        }
    }

    private String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(message);
        }
        return value.trim();
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeKeyword(String keyword) {
        String normalized = normalizeOptional(keyword);
        return normalized == null ? "" : normalized;
    }
}
