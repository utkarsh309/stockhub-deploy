package com.stockhub.auth.config;

import com.stockhub.auth.entity.User;
import com.stockhub.auth.enums.Role;
import com.stockhub.auth.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock private JwtUtil jwtUtil;
    @Mock private UserRepository userRepository;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain filterChain;

    @InjectMocks
    private JwtAuthFilter jwtAuthFilter;

    private User user;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();

        user = User.builder()
                .userId(1)
                .fullName("Test User")
                .email("test@stockhub.com")
                .password("encoded")
                .role(Role.STAFF)
                .isActive(true)
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ─── No / Invalid Header Tests ─────────────

    @Test
    void doFilterInternal_noAuthHeader_continuesChain() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).extractEmail(anyString());
    }

    @Test
    void doFilterInternal_headerNotBearer_continuesChain() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Basic abc123");

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).extractEmail(anyString());
    }

    @Test
    void doFilterInternal_emptyHeader_continuesChain() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("");

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).extractEmail(anyString());
    }

    // ─── Valid Token Tests ─────────────────────

    @Test
    void doFilterInternal_validToken_setsAuthentication() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer valid.jwt.token");
        when(jwtUtil.extractEmail("valid.jwt.token")).thenReturn("test@stockhub.com");
        when(userRepository.findByEmail("test@stockhub.com")).thenReturn(Optional.of(user));
        when(jwtUtil.isTokenValid("valid.jwt.token", user)).thenReturn(true);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                .isEqualTo(user);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_validToken_setsCorrectAuthorities() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer valid.jwt.token");
        when(jwtUtil.extractEmail("valid.jwt.token")).thenReturn("test@stockhub.com");
        when(userRepository.findByEmail("test@stockhub.com")).thenReturn(Optional.of(user));
        when(jwtUtil.isTokenValid("valid.jwt.token", user)).thenReturn(true);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                .anyMatch(a -> a.getAuthority().equals("ROLE_STAFF"));
    }

    // ─── Invalid Token Cases ───────────────────

    @Test
    void doFilterInternal_nullEmail_skipsAuth() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer bad.token");
        when(jwtUtil.extractEmail("bad.token")).thenReturn(null);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_userNotFound_skipsAuth() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer valid.jwt.token");
        when(jwtUtil.extractEmail("valid.jwt.token")).thenReturn("ghost@stockhub.com");
        when(userRepository.findByEmail("ghost@stockhub.com")).thenReturn(Optional.empty());

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_invalidToken_skipsAuth() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer expired.jwt.token");
        when(jwtUtil.extractEmail("expired.jwt.token")).thenReturn("test@stockhub.com");
        when(userRepository.findByEmail("test@stockhub.com")).thenReturn(Optional.of(user));
        when(jwtUtil.isTokenValid("expired.jwt.token", user)).thenReturn(false);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_inactiveUser_skipsAuth() throws Exception {
        user.setActive(false);
        when(request.getHeader("Authorization")).thenReturn("Bearer valid.jwt.token");
        when(jwtUtil.extractEmail("valid.jwt.token")).thenReturn("test@stockhub.com");
        when(userRepository.findByEmail("test@stockhub.com")).thenReturn(Optional.of(user));
        when(jwtUtil.isTokenValid("valid.jwt.token", user)).thenReturn(true);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // isEnabled() returns false for inactive user → no auth set
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    // ─── shouldNotFilter Tests ─────────────────

    @Test
    void shouldNotFilter_loginPath_returnsTrue() throws Exception {
        when(request.getServletPath()).thenReturn("/api/auth/login");

        boolean result = jwtAuthFilter.shouldNotFilter(request);

        assertThat(result).isTrue();
    }

    @Test
    void shouldNotFilter_registerPath_returnsTrue() throws Exception {
        when(request.getServletPath()).thenReturn("/api/auth/register");

        boolean result = jwtAuthFilter.shouldNotFilter(request);

        assertThat(result).isTrue();
    }

    @Test
    void shouldNotFilter_otherPath_returnsFalse() throws Exception {
        when(request.getServletPath()).thenReturn("/api/auth/users/1");

        boolean result = jwtAuthFilter.shouldNotFilter(request);

        assertThat(result).isFalse();
    }

    @Test
    void shouldNotFilter_usersPath_returnsFalse() throws Exception {
        when(request.getServletPath()).thenReturn("/api/auth/users");

        boolean result = jwtAuthFilter.shouldNotFilter(request);

        assertThat(result).isFalse();
    }
}