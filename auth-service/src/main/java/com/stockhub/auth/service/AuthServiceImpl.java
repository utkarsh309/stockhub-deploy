package com.stockhub.auth.service;

import com.stockhub.auth.config.JwtUtil;
import com.stockhub.auth.dto.LoginRequest;
import com.stockhub.auth.dto.LoginResponse;
import com.stockhub.auth.dto.RegisterRequest;
import com.stockhub.auth.entity.User;
import com.stockhub.auth.enums.Role;
import com.stockhub.auth.exception.InvalidCredentialsException;
import com.stockhub.auth.exception.UserAlreadyExistsException;
import com.stockhub.auth.exception.UserNotFoundException;
import com.stockhub.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    // ─── 1. Register ───────────────────────────
    @Override
    public User register(RegisterRequest request) {

        // Check if email already exists
        if (userRepository.existsByEmail(
                request.getEmail())) {
            throw new UserAlreadyExistsException(
                    "Email already exists: "
                            + request.getEmail());
        }

        // Build User entity from request
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                // Hash password before saving
                .password(passwordEncoder.encode(
                        request.getPassword()))
                .phone(request.getPhone())
                .department(request.getDepartment())
                .role(request.getRole())
                .isActive(true)
                .build();

        // Save to database
        User savedUser = userRepository.save(user);

        log.info("New user registered: {}",
                savedUser.getEmail());

        return savedUser;
    }

    // ─── 2. Login ──────────────────────────────
    @Override
    public LoginResponse login(LoginRequest request) {

        try {
            // Authenticate using Spring Security
            // This checks email and password
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            throw new InvalidCredentialsException(
                    "Invalid email or password");
        }

        // Load user from DB
        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found"));

        // Check if user is active
        if (!user.isActive()) {
            throw new InvalidCredentialsException(
                    "Account is deactivated. " +
                            "Contact administrator.");
        }

        // Update last login time
        userRepository.updateLastLoginAt(
                user.getUserId(),
                LocalDateTime.now());

        // Generate JWT token
        String token = jwtUtil.generateToken(user);

        log.info("User logged in: {}",
                user.getEmail());

        // Return response with token
        return LoginResponse.builder()
                .userId(user.getUserId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .token(token)
                .tokenType("Bearer")
                .build();
    }

    // ─── 3. Get User By ID ─────────────────────
    @Override
    @Transactional(readOnly = true)
    public User getUserById(Integer userId) {
        return userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found " +
                                        "with id: " + userId));
    }

    // ─── 4. Get All Users ──────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // ─── 5. Get Users By Role ──────────────────
    @Override
    @Transactional(readOnly = true)
    public List<User> getUsersByRole(String role) {
        return userRepository.findByRole(
                Role.valueOf(role.toUpperCase()));
    }

    // ─── 6. Update Profile ─────────────────────
    @Override
    public User updateProfile(
            Integer userId,
            RegisterRequest request) {

        User user = getUserById(userId);

        // Update only non-null fields
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getDepartment() != null) {
            user.setDepartment(
                    request.getDepartment());
        }
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }

        User updatedUser = userRepository.save(user);

        log.info("Profile updated for user: {}",
                user.getEmail());

        return updatedUser;
    }

    // ─── 7. Change Password ────────────────────
    @Override
    public void changePassword(
            Integer userId,
            String oldPassword,
            String newPassword) {

        User user = getUserById(userId);

        // Verify old password is correct
        if (!passwordEncoder.matches(
                oldPassword, user.getPassword())) {
            throw new InvalidCredentialsException(
                    "Current password is incorrect");
        }

        // Encode and save new password
        user.setPassword(
                passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("Password changed for user: {}",
                user.getEmail());
    }

    // ─── 8. Deactivate User ────────────────────
    @Override
    public void deactivateUser(Integer userId) {

        // Check user exists first
        User user = getUserById(userId);

        userRepository.deactivateUser(userId);

        log.info("User deactivated: {}",
                user.getEmail());
    }

    // ─── 9. Activate User ──────────────────────
    @Override
    public void activateUser(Integer userId) {

        User user = getUserById(userId);
        user.setActive(true);
        userRepository.save(user);

        log.info("User activated: {}",
                user.getEmail());
    }
}