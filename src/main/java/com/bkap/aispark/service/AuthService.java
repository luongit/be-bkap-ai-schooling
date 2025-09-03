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

    public String login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Mật khẩu không đúng");
        }

        // sinh token (email + role)
        return jwtUtil.generateToken(user.getEmail(), user.getRole().name());
    }
}





