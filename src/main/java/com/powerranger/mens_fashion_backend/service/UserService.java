package com.powerranger.mens_fashion_backend.service;

import com.powerranger.mens_fashion_backend.dto.response.UserResponse;
import java.util.List;

public interface UserService {
    UserResponse getProfile(String email);
    List<UserResponse> listAllUsers();
}
