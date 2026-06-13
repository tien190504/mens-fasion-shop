package com.powerranger.fashion_shop_backend.service;

import com.powerranger.fashion_shop_backend.dto.request.AdminResetPasswordRequest;
import com.powerranger.fashion_shop_backend.dto.request.AdminUserCreateRequest;
import com.powerranger.fashion_shop_backend.dto.request.AdminUserUpdateRequest;
import com.powerranger.fashion_shop_backend.dto.request.UpdateProfileRequest;
import com.powerranger.fashion_shop_backend.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {
    UserResponse getProfile(String email);
    UserResponse updateProfile(String email, UpdateProfileRequest request);
    List<UserResponse> listAllUsers();
    Page<UserResponse> searchUsersForAdmin(String keyword, Boolean active, Boolean admin, Pageable pageable);
    UserResponse getUserByIdForAdmin(Long id);
    UserResponse createUserForAdmin(AdminUserCreateRequest request);
    UserResponse updateUserForAdmin(Long id, String currentAdminEmail, AdminUserUpdateRequest request);
    void resetPasswordForAdmin(Long id, AdminResetPasswordRequest request);
}
