package com.bkap.aispark.service;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.bkap.aispark.dto.LoginRequest;
import com.bkap.aispark.dto.LoginResponse;
import com.bkap.aispark.entity.User;
import com.bkap.aispark.repository.UserRepository;



@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("Email không tồn tại"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Mật khẩu không đúng");
        }

        LoginResponse response = new LoginResponse();
        response.setUserId(user.getId());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setRole(user.getRole().name());
        response.setObjectType(user.getObjectType() != null ? user.getObjectType().name() : null);
        response.setObjectId(user.getObjectId());
        response.setStudentCode(user.getStudentCode());

        return response;
    }
}


