package com.example.interviewcoach.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private static final String SECRET_KEY =
            "mysecretkeymysecretkeymysecretkey123456";

    private final SecretKey key = Keys.hmacShaKeyFor(
            SECRET_KEY.getBytes(StandardCharsets.UTF_8)
    );

    // Generate JWT
    public String generateToken(String email, String role) {

        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(
                        new Date(System.currentTimeMillis() + 86400000)
                )
                .signWith(key)
                .compact();
    }

    // Extract email
    public String extractEmail(String token) {

        return extractAllClaims(token).getSubject();
    }

    // Extract role
    public String extractRole(String token) {

        return extractAllClaims(token)
                .get("role", String.class);
    }

    // Check expiration
    public boolean isTokenExpired(String token) {

        return extractAllClaims(token)
                .getExpiration()
                .before(new Date());
    }

    // Validate JWT
    public boolean isTokenValid(String token) {

        try {

            Claims claims = extractAllClaims(token);

            return claims.getExpiration().after(new Date());

        } catch (Exception e) {

            System.out.println("JWT validation error: "
                    + e.getMessage());

            return false;
        }
    }

    // Read JWT claims
    private Claims extractAllClaims(String token) {

        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}