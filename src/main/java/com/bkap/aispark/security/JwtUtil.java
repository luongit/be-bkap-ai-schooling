package com.bkap.aispark.security;

import java.util.Date;
import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Component
public class JwtUtil {

    // Khóa bí mật
    private static final String SECRET_KEY = "mysecretkeymysecretkeymysecretkey123";
    private final SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    // Thời hạn token


    private static final long ACCESS_TOKEN_EXPIRATION = 1000 * 60 * 30; // 30p 1 phien no remember
    private static final long REFRESH_TOKEN_EXPIRATION = 1000L * 60 * 60 * 24 * 7; // tick remember 7 day

    @PostConstruct
    public void debugKey() {
        System.out.println("JWTUtil key loaded: " + key.hashCode());
    }

    // ==========================
    //  Generate Access Token
    // ==========================

    public String generateAccessToken(Long userId, String email, String username, String role) {
        return buildToken(userId, email, username, role, "access", ACCESS_TOKEN_EXPIRATION);
    }

    // ==========================
    //  Generate Refresh Token
    // ==========================
    public String generateRefreshToken(Long userId, String email) {
        return buildToken(userId, email, null, null, "refresh", REFRESH_TOKEN_EXPIRATION);
    }

    // ==========================
    //  Build Token Chung
    // ==========================
    private String buildToken(Long userId, String email, String username, String role, String type, long ttlMillis) {

        JwtBuilder builder = Jwts.builder()
                .setSubject(email)              // subject = email
                .claim("email", email)
                .claim("userId", userId)
                .claim("username", username)     // username thật
                .claim("type", type)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ttlMillis))
                .signWith(key, SignatureAlgorithm.HS256);

        if (role != null) {
            builder.claim("role", role);
        }

        return builder.compact();
    }

    // ==========================
    //  Validate Token
    // ==========================
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    // ==========================
    //  Helpers lấy thông tin
    // ==========================
    public String getEmail(String token) {
        return parseClaims(token).getSubject();
    }

    public Long getUserId(String token) {
        return parseClaims(token).get("userId", Long.class);
    }

    public String getUsername(String token) {
        return parseClaims(token).get("username", String.class);
    }

    public String getRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    public boolean isAccessToken(String token) {
        return "access".equals(parseClaims(token).get("type", String.class));
    }

    public boolean isRefreshToken(String token) {
        return "refresh".equals(parseClaims(token).get("type", String.class));
    }

    // ==========================
    //  Parse Claims
    // ==========================
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
