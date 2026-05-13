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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl authService;

    private User user;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .userId(1)
                .fullName("Test User")
                .email("test@stockhub.com")
                .password("encoded_password")
                .role(Role.STAFF)
                .isActive(true)
                .build();

        registerRequest = new RegisterRequest();
        registerRequest.setFullName("Test User");
        registerRequest.setEmail("test@stockhub.com");
        registerRequest.setPassword("password123");
        registerRequest.setRole(Role.STAFF);
    }

    // ─── Register Tests ────────────────────────

    @Test
    void register_success() {
        when(userRepository.existsByEmail(anyString()))
                .thenReturn(false);
        when(passwordEncoder.encode(anyString()))
                .thenReturn("encoded_password");
        when(userRepository.save(any(User.class)))
                .thenReturn(user);

        User result = authService.register(registerRequest);

        assertThat(result).isNotNull();
        assertThat(result.getEmail())
                .isEqualTo("test@stockhub.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_duplicateEmail_throwsException() {
        when(userRepository.existsByEmail(anyString()))
                .thenReturn(true);

        assertThatThrownBy(() ->
                authService.register(registerRequest))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("Email already exists");

        verify(userRepository, never()).save(any());
    }

    // ─── Login Tests ───────────────────────────

    @Test
    void login_success() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@stockhub.com");
        loginRequest.setPassword("password123");

        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(any(User.class)))
                .thenReturn("jwt_token");

        LoginResponse response =
                authService.login(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.getToken())
                .isEqualTo("jwt_token");
        assertThat(response.getEmail())
                .isEqualTo("test@stockhub.com");
    }

    @Test
    void login_badCredentials_throwsException() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@stockhub.com");
        loginRequest.setPassword("wrongpassword");

        doThrow(new BadCredentialsException("bad"))
                .when(authenticationManager)
                .authenticate(any());

        assertThatThrownBy(() ->
                authService.login(loginRequest))
                .isInstanceOf(
                        InvalidCredentialsException.class);
    }

    @Test
    void login_inactiveUser_throwsException() {
        user.setActive(false);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@stockhub.com");
        loginRequest.setPassword("password123");

        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(user));

        assertThatThrownBy(() ->
                authService.login(loginRequest))
                .isInstanceOf(
                        InvalidCredentialsException.class)
                .hasMessageContaining("deactivated");
    }

    // ─── Get User Tests ────────────────────────

    @Test
    void getUserById_success() {
        when(userRepository.findById(1))
                .thenReturn(Optional.of(user));

        User result = authService.getUserById(1);

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(1);
    }

    @Test
    void getUserById_notFound_throwsException() {
        when(userRepository.findById(99))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                authService.getUserById(99))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void getAllUsers_success() {
        when(userRepository.findAll())
                .thenReturn(List.of(user));

        List<User> result = authService.getAllUsers();

        assertThat(result).hasSize(1);
    }

    @Test
    void getUsersByRole_success() {
        when(userRepository.findByRole(Role.STAFF))
                .thenReturn(List.of(user));

        List<User> result =
                authService.getUsersByRole("STAFF");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRole())
                .isEqualTo(Role.STAFF);
    }

    // ─── Update Profile Tests ──────────────────

    @Test
    void updateProfile_success() {
        when(userRepository.findById(1))
                .thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class)))
                .thenReturn(user);

        registerRequest.setFullName("Updated Name");
        User result =
                authService.updateProfile(1, registerRequest);

        assertThat(result).isNotNull();
        verify(userRepository).save(any(User.class));
    }

    // ─── Change Password Tests ─────────────────

    @Test
    void changePassword_success() {
        when(userRepository.findById(1))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(
                "oldPass", "encoded_password"))
                .thenReturn(true);
        when(passwordEncoder.encode("newPass"))
                .thenReturn("new_encoded");
        when(userRepository.save(any(User.class)))
                .thenReturn(user);

        authService.changePassword(1, "oldPass", "newPass");

        verify(userRepository).save(any(User.class));
    }

    @Test
    void changePassword_wrongOldPassword_throwsException() {
        when(userRepository.findById(1))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString()))
                .thenReturn(false);

        assertThatThrownBy(() ->
                authService.changePassword(
                        1, "wrongOld", "newPass"))
                .isInstanceOf(
                        InvalidCredentialsException.class)
                .hasMessageContaining(
                        "Current password is incorrect");
    }

    // ─── Activate / Deactivate Tests ───────────

    @Test
    void deactivateUser_success() {
        when(userRepository.findById(1))
                .thenReturn(Optional.of(user));

        authService.deactivateUser(1);

        verify(userRepository).deactivateUser(1);
    }

    @Test
    void activateUser_success() {
        when(userRepository.findById(1))
                .thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class)))
                .thenReturn(user);

        authService.activateUser(1);

        verify(userRepository).save(any(User.class));
    }
}