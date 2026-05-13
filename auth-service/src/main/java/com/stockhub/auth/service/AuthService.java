package com.stockhub.auth.service;

import com.stockhub.auth.dto.LoginRequest;
import com.stockhub.auth.dto.LoginResponse;
import com.stockhub.auth.dto.RegisterRequest;
import com.stockhub.auth.entity.User;

import java.util.List;

public interface AuthService {

    // Register new user
    User register(RegisterRequest request);

    // Login and get JWT token
    LoginResponse login(LoginRequest request);

    // Get user by ID
    User getUserById(Integer userId);

    // Get all users (Admin only)
    List<User> getAllUsers();

    // Get users by role
    List<User> getUsersByRole(String role);

    // Update user profile
    User updateProfile(Integer userId,
                       RegisterRequest request);

    // Change password
    void changePassword(Integer userId,
                        String oldPassword,
                        String newPassword);

    // Deactivate user (Admin only)
    void deactivateUser(Integer userId);

    // Activate user (Admin only)
    void activateUser(Integer userId);
}