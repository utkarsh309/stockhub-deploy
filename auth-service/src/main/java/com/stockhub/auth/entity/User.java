package com.stockhub.auth.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.stockhub.auth.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    // Primary key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    // Basic details
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Email
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @JsonIgnore // hide password in API responses
    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "phone", length = 15)
    private String phone;

    @Column(name = "department", length = 100)
    private String department;

    // User role (stored as string)
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    // Account status
    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    // Audit timestamps
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Last login time (set manually)
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    // Spring Security authorities
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Tells Spring Security what role this user has
        // "ROLE_" prefix is required by Spring Security
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    // Use email as username
    @Override
    public String getUsername() {
        return email;
    }

    // Account status flags
    @Override
    public boolean isAccountNonExpired() {
        // Account never expires in StockHub
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // Account is not locked
        // (we use isActive for deactivation)
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // Credentials never expire
        // JWT handles token expiry
        return true;
    }

    // Only active users can login
    @Override
    public boolean isEnabled() {
        // User is enabled only if isActive = true
        // Deactivated users cannot login
        return isActive;
    }
}