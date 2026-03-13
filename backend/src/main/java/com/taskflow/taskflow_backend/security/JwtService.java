package com.taskflow.taskflow_backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    private final String SECRET =
            "my_super_secret_key_for_taskflow_application_123456";

    private final Key key =
            Keys.hmacShaKeyFor(SECRET.getBytes());

    private final long EXPIRATION_TIME =
            1000 * 60 * 60 * 24; // 24 hours

    // ===============================
    // TOKEN GENERATION (UPDATED)
    // ===============================

    public String generateToken(String email, Long userId, String fullName, String role) {

        return Jwts.builder()
                .setSubject(email)
                .claim("userId", userId)   // ✅ ADD THIS
                .claim("fullName", fullName)   // ✅ ADD
                .claim("role", role)           // ✅ ADD
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(System.currentTimeMillis() + EXPIRATION_TIME)
                )
                .signWith(key)
                .compact();
    }

    // ===============================
    // EXTRACT CLAIMS
    // ===============================

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    public Long extractUserId(String token) {
        return extractAllClaims(token).get("userId", Long.class);
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    public String extractFullName(String token) {
        return extractAllClaims(token).get("fullName", String.class);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // ===============================
    // VALIDATION
    // ===============================

    public boolean isTokenValid(String token, UserDetails userDetails) {

        final String email = extractEmail(token);

        return (email.equals(userDetails.getUsername())
                && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractAllClaims(token)
                .getExpiration()
                .before(new Date());
    }
}