package com.bkap.aispark.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bkap.aispark.dto.LoginRequest;
import com.bkap.aispark.dto.LoginResponse;
import com.bkap.aispark.dto.UserDTO;
import com.bkap.aispark.entity.User;
import com.bkap.aispark.repository.UserRepository;
import com.bkap.aispark.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthApi {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request.getIdentifier(), request.getPassword());
        return ResponseEntity.ok(response);
    }
    
@GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
                return ResponseEntity.status(401).body(new ErrorResponse("Không tìm thấy người dùng: Chưa đăng nhập hoặc token không hợp lệ"));
            }
            String email = authentication.getName(); // Lấy email từ token
            User user = userRepository.findByEmail(email) // Tìm theo email
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với email: " + email));
            return ResponseEntity.ok(new UserDTO(user.getId(), user.getUsername(), user.getEmail()));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(new ErrorResponse("Không tìm thấy người dùng: " + e.getMessage()));
        }
    }
}
