package com.stockhub.auth.controller;

import com.stockhub.auth.dto.LoginRequest;
import com.stockhub.auth.dto.LoginResponse;
import com.stockhub.auth.dto.RegisterRequest;
import com.stockhub.auth.entity.User;
import com.stockhub.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    // ─── 1. Register ───────────────────────────
    // POST http://localhost:8081/api/auth/register
    @PostMapping("/register")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> register(
            @Valid @RequestBody
            RegisterRequest request) {

        User user = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED) // 201
                .body(user);
    }

    // ─── 2. Login ──────────────────────────────
    // PUBLIC - No token needed
    // POST http://localhost:8081/api/auth/login
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody
            LoginRequest request) {

        LoginResponse response =
                authService.login(request);
        return ResponseEntity.ok(response); // 200
    }

    // ─── 3. Get User By ID ─────────────────────
    // PROTECTED - Token needed
    // GET http://localhost:8081/api/auth/users/1
    @GetMapping("/users/{userId}")
    public ResponseEntity<User> getUserById(
            @PathVariable Integer userId) {

        User user = authService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    // ─── 4. Get All Users ──────────────────────
    // ADMIN ONLY
    // GET http://localhost:8081/api/auth/users
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {

        List<User> users = authService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    // ─── 5. Get Users By Role ──────────────────
    // ADMIN ONLY
    // GET http://localhost:8081/api/auth/users/role/STAFF
    @GetMapping("/users/role/{role}")
    public ResponseEntity<List<User>> getUsersByRole(
            @PathVariable String role) {

        List<User> users =
                authService.getUsersByRole(role);
        return ResponseEntity.ok(users);
    }

    // ─── 6. Update Profile ─────────────────────
    // PROTECTED - Token needed
    // PUT http://localhost:8081/api/auth/users/1/profile
    @PutMapping("/users/{userId}/profile")
    public ResponseEntity<User> updateProfile(
            @PathVariable Integer userId,
            @Valid @RequestBody
            RegisterRequest request) {

        User user = authService
                .updateProfile(userId, request);
        return ResponseEntity.ok(user);
    }

    // ─── 7. Change Password ────────────────────
    // PROTECTED - Token needed
    // PUT http://localhost:8081/api/auth/users/1/password
    @PutMapping("/users/{userId}/password")
    public ResponseEntity<String> changePassword(
            @PathVariable Integer userId,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {

        authService.changePassword(
                userId, oldPassword, newPassword);

        return ResponseEntity.ok(
                "Password changed successfully");
    }

    // ─── 8. Deactivate User ────────────────────
    // ADMIN ONLY
    // PUT http://localhost:8081/api/auth/users/1/deactivate
    @PutMapping("/users/{userId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deactivateUser(
            @PathVariable Integer userId) {

        authService.deactivateUser(userId);
        return ResponseEntity.ok(
                "User deactivated successfully");
    }

    // ─── 9. Activate User ──────────────────────
    // ADMIN ONLY
    // PUT http://localhost:8081/api/auth/users/1/activate
    @PutMapping("/users/{userId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> activateUser(
            @PathVariable Integer userId) {

        authService.activateUser(userId);
        return ResponseEntity.ok(
                "User activated successfully");
    }
}