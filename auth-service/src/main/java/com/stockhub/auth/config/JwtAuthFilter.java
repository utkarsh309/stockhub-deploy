package com.stockhub.auth.config;

import com.stockhub.auth.entity.User;
import com.stockhub.auth.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // ── Step 1: Get Authorization header ──
        final String authHeader = request.getHeader("Authorization");

        // ── Step 2: Check header valid ─────────
        // If no header or not Bearer token
        // skip filter and continue
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // ── Step 3: Extract JWT token ──────────
        // Remove "Bearer " prefix (7 characters)
        final String jwt = authHeader.substring(7);

        // ── Step 4: Extract email from token ───
        final String email = jwtUtil.extractEmail(jwt);

        // ── Step 5: Check email extracted ──────
        // and user not already authenticated
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // ── Step 6: Load user from DB ──────
            Optional<User> userOptional = userRepository.findByEmail(email);

            if (userOptional.isPresent()) {
                User user = userOptional.get();

                // ── Step 7: Validate token ─────
                if (jwtUtil.isTokenValid(jwt, user) && user.isEnabled()) {

                    // ── Step 8: Create auth object
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken
                            (user, null, user.getAuthorities());

                    // Add request details
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // ── Step 9: Set in context ──
                    // Tells Spring Security:
                    // "This user is authenticated"
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        }

        // ── Step 10: Continue to next filter ───
        // or controller
        filterChain.doFilter(request, response);
    }

    // ─── Skip filter for public endpoints ──────
    // Login and Register dont need token
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        String path = request.getServletPath();
        return path.equals("/api/auth/login") || path.equals("/api/auth/register");
    }
}
