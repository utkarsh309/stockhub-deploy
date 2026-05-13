package com.stockhub.auth.dto;

import com.stockhub.auth.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private Integer userId;
    private String fullName;
    private String email;
    private Role role;
    private String token;      // JWT Token
    private String tokenType;  // Always "Bearer"
}