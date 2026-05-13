package com.stockhub.auth.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.stockhub.auth.repository.UserRepository;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserRepository userRepository;

    // ─── 1. Public and Protected URLs ──────────
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                // Disable CSRF
                // Not needed for REST APIs
                // CSRF is for browser form submissions
                .csrf(AbstractHttpConfigurer::disable)

                // URL Authorization Rules
                .authorizeHttpRequests(auth -> auth

                        // Public endpoints
                        // No token needed
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/register",
                                "/api/auth/users/role/**"
                        ).permitAll()

                        //  FOR SWAGGER
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/api-docs/**",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // Only ADMIN can access
                        .requestMatchers(
                                "/api/auth/users"
                        ).hasRole("ADMIN")

                        // All other endpoints
                        // need valid token
                        .anyRequest().authenticated()
                )

                // Stateless session
                // No session storage on server
                // JWT handles state
                .sessionManagement(session -> session
                        .sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS)
                )

                // Add our JWT filter
                // runs BEFORE Spring's default
                // authentication filter
                .authenticationProvider(
                        authenticationProvider()
                )
                .addFilterBefore(
                        jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    // ─── 2. Password Encoder ───────────────────
    // BCrypt hashes passwords
    // Never store plain text passwords!
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ─── 3. UserDetailsService ─────────────────
    // Tells Spring how to load user
    // from database by email
    @Bean
    public UserDetailsService userDetailsService() {
        return email -> userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found: " + email
                        )
                );
    }

    // ─── 4. AuthenticationProvider ─────────────
    // Connects UserDetailsService
    // and PasswordEncoder together
    // Spring uses this to authenticate users
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider();
        provider.setUserDetailsService(
                userDetailsService());
        provider.setPasswordEncoder(
                passwordEncoder());
        return provider;
    }

    // ─── 5. AuthenticationManager ──────────────
    // Used in AuthServiceImpl
    // to authenticate login requests
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}