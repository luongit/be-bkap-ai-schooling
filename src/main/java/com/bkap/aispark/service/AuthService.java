package com.bkap.aispark.service;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.bkap.aispark.dto.LoginRequest;
import com.bkap.aispark.dto.LoginResponse;
import com.bkap.aispark.entity.User;
import com.bkap.aispark.entity.UserRole;
import com.bkap.aispark.repository.UserRepository;
import com.bkap.aispark.security.JwtUtil;



@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public LoginResponse login(String identifier, String password) {
        User user;

        if (identifier.contains("@")) {
            // đăng nhập bằng email
            user = userRepository.findByEmail(identifier)
                    .orElseThrow(() -> new RuntimeException("Email không tồn tại"));
        } else {
            // đăng nhập bằng username
            user = userRepository.findByUsername(identifier)
                    .orElseThrow(() -> new RuntimeException("Username không tồn tại"));
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Mật khẩu không đúng");
        }

        // ✅ sinh JWT token có userId + email + role
        String token = jwtUtil.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );

        return new LoginResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhone(),
                user.getRole().name(),
                user.getObjectType() != null ? user.getObjectType().name() : null,
                user.getObjectId(),
                token
        );
    }
}









