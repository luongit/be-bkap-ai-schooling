package com.bkap.aispark.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Component
public class JwtUtil {

    // 🔐 Khóa bảo mật mạnh (ít nhất 32 ký tự)
    private static final String SECRET_KEY = "mysecretkeymysecretkeymysecretkey123";
    private final SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    // ⏱️ Thời hạn token
    private static final long ACCESS_TOKEN_EXPIRATION = 1000 * 60 * 15; // 15 phút
    private static final long REFRESH_TOKEN_EXPIRATION = 1000L * 60 * 60 * 24 * 7; // 7 ngày

    // Sinh Access Token (cũ gọi generateToken)
    public String generateToken(Long userId, String email, String role) {
        return generateAccessToken(userId, email, role);

    }
    @PostConstruct
    public void debugKey() {
        System.out.println("🔑 JWTUtil key loaded: " + key.hashCode());
    }

    // Lấy email từ token
    public String getEmail(String token) {
        return parseClaims(token).getSubject();
    }

    // Lấy userId từ token
    public Long getUserId(String token) {
        return parseClaims(token).get("userId", Long.class);
    }

    // Lấy role từ token
    public String getRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    // Lấy username (nếu có)
    public String getUsername(String token) {
        return parseClaims(token).get("username", String.class);
    }

    // Kiểm tra token hợp lệ
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }



    public String generateAccessToken(Long userId, String email, String role) {
        return buildToken(userId, email, role, "access", ACCESS_TOKEN_EXPIRATION);
    }

    public String generateRefreshToken(Long userId, String email) {
        return buildToken(userId, email, null, "refresh", REFRESH_TOKEN_EXPIRATION);
    }

    private String buildToken(Long userId, String email, String role, String type, long ttlMillis) {
        JwtBuilder builder = Jwts.builder()
                .setSubject(email)
                .claim("userId", userId)
                .claim("type", type)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ttlMillis))
                .signWith(key);
        if (role != null) builder.claim("role", role);
        return builder.compact();
    }

    public boolean isAccessToken(String token) {
        return "access".equals(parseClaims(token).get("type", String.class));
    }

    public boolean isRefreshToken(String token) {
        return "refresh".equals(parseClaims(token).get("type", String.class));
    }

    // ============================================================
    // 🧩 Hàm dùng chung
    // ============================================================
   
    private Claims parseClaims(String token) {

        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}
