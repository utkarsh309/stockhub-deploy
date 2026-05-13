package com.stockhub.gateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtUtil {

    // Must match auth-service jwt.secret
    @Value("${jwt.secret}")
    private String SECRET_KEY;

    // Build signing key from secret string
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(
                SECRET_KEY.getBytes(
                        StandardCharsets.UTF_8));
    }

    // Extract all claims from token payload
    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Extract userId stored as subject
    public String extractUserId(String token) {
        return extractAllClaims(token).getSubject();
    }

    // Extract role from token payload
    public String extractRole(String token) {
        return extractAllClaims(token)
                .get("role", String.class);
    }

    // Extract email from token payload
    public String extractEmail(String token) {
        return extractAllClaims(token)
                .get("email", String.class);
    }

    // Validate token signature and expiry
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}