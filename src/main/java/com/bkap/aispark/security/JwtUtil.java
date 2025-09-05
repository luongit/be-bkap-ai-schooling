package com.bkap.aispark.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    private static final String SECRET_KEY = "mysecretkeymysecretkeymysecretkey123"; // >=32 ký tự
    private static final long EXPIRATION = 1000 * 60 * 60; // 1h

    private final SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    // ✅ Sinh token có userId + email + role
    public String generateToken(Long userId, String email, String role) {
        return Jwts.builder()
                .setSubject(email)                 // email làm subject
                .claim("userId", userId)           // thêm userId
                .claim("role", role)               // thêm role
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(key)
                .compact();
    }
    
    // Lấy email từ token
    public String getEmail(String token) {
        return parseClaims(token).getSubject();
    }

    // ✅ Lấy userId từ token
    public Long getUserId(String token) {
        return parseClaims(token).get("userId", Long.class);
    }

    // Lấy role từ token
    public String getRole(String token) {
        return parseClaims(token).get("role", String.class);
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
    


    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
   

}
