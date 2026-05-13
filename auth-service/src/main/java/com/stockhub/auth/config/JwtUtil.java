package com.stockhub.auth.config;

import com.stockhub.auth.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    // ─── Secret key from
    //     application.properties ────────────────
    @Value("${jwt.secret}")
    private String secretKey;

    // ─── Token expiry 8 hours
    //     in milliseconds ────────────────────────
    @Value("${jwt.expiration}")
    private long jwtExpiration;

    // ─── Get signing key ───────────────────────
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(
                secretKey.getBytes());
    }

    // ─── 1. Generate Token ─────────────────────
    // Called after successful login
    public String generateToken(User user) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", user.getRole().name());
        // Store userId as extra claim too
        extraClaims.put("userId", user.getUserId());
        // Store email as extra claim
        extraClaims.put("email", user.getEmail());

        return Jwts.builder()
                .setClaims(extraClaims)
                // Store userId as subject
                .setSubject(
                        String.valueOf(user.getUserId()))
                .setIssuedAt(
                        new Date(System.currentTimeMillis()))
                .setExpiration(
                        new Date(System.currentTimeMillis()
                                + jwtExpiration))
                .signWith(getSigningKey(),
                        SignatureAlgorithm.HS256)
                .compact();
    }

    // ─── 2. Extract Email from Token ───────────
    // Used in JwtAuthFilter to find user
    public String extractEmail(String token) {
        return extractClaim(token,
                claims -> claims.get("email", String.class));
    }

    // ─── 3. Extract Role from Token ────────────
    public String extractRole(String token) {
        return extractClaim(token,
                claims -> claims.get(
                        "role", String.class));
    }

    // ─── 4. Extract UserId from Token ──────────
    public Integer extractUserId(String token) {
        return extractClaim(token,
                claims -> claims.get(
                        "userId", Integer.class));
    }

    // ─── 5. Check Token Valid ──────────────────
    // Called in JwtAuthFilter
    public boolean isTokenValid(
            String token, User user) {
        final String email = extractEmail(token);
        return (email.equals(user.getEmail()))
                && !isTokenExpired(token);
    }

    // ─── 6. Check Token Expired ────────────────
    private boolean isTokenExpired(String token) {
        return extractExpiration(token)
                .before(new Date());
    }

    // ─── Helper: Extract Expiration ────────────
    private Date extractExpiration(String token) {
        return extractClaim(token,
                Claims::getExpiration);
    }

    // ─── Helper: Extract Any Claim ─────────────
    // Generic method to extract
    // any data from token
    private <T> T extractClaim(
            String token,
            Function<Claims, T> claimsResolver) {

        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // ─── Helper: Extract All Claims ────────────
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}