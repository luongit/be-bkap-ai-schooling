package com.bkap.aispark.api;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Map;
import com.bkap.aispark.service.refresh_tokens.RefreshTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.bkap.aispark.dto.LoginRequest;
import com.bkap.aispark.dto.LoginResponse;
import com.bkap.aispark.dto.ParentRegisterRequest;
import com.bkap.aispark.dto.StudentRegisterRequest;
import com.bkap.aispark.dto.UserDTO;
import com.bkap.aispark.entity.PasswordResetToken;
import com.bkap.aispark.entity.User;
import com.bkap.aispark.repository.PasswordResetTokenRepository;
import com.bkap.aispark.repository.UserRepository;
import com.bkap.aispark.security.JwtUtil;
import com.bkap.aispark.service.AuthService;
import com.bkap.aispark.service.PasswordResetService;
import com.bkap.aispark.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.http.HttpHeaders;

@CrossOrigin(origins = {"http://bkapai.vn", "http://localhost:3000"})
@RestController
@RequestMapping("/api/auth")
public class AuthApi {

    @Value("${fe.login.redirect}")
    private String feLoginRedirect;

    @Autowired
    private PasswordResetTokenRepository tokenRepo;
    @Autowired
    private PasswordResetService passwordResetService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AuthService authService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private JwtUtil jwtUtil;

    private final UserService userService;

    public AuthApi(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request,
            HttpServletResponse servletResponse
    ) {

        //  Kiểm tra user theo email hoặc username
        User user = userRepository.findByEmail(request.getIdentifier())
                .orElseGet(() -> userRepository.findByUsername(request.getIdentifier())
                        .orElseThrow(() -> new RuntimeException("User không tồn tại")));

        // Check mật khẩu
        if (!authService.checkPassword(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401).body(Map.of("message", "Sai mật khẩu"));
        }

        // generator Access Token (15p)
        String accessToken = jwtUtil.generateAccessToken(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getRole().name()
        );

        // Tick Remember Me → tao Refresh Token + save DB + set cookie
        if (request.isRememberMe()) {

            // Tao refresh token
            String refreshToken = jwtUtil.generateRefreshToken(
                    user.getId(),
                    user.getEmail(),
                    true
            );

            // Lưu DB để quản lý token
            refreshTokenService.createToken(user, refreshToken, 7); // 7 day

            // Tao cookie HttpOnly de fontend không reading duoc
            ResponseCookie cookie = ResponseCookie.from("refresh_token", refreshToken)
                    .httpOnly(true)
                    .secure(false) // đặt true nếu dùng HTTPS
                    .path("/")
                    .maxAge(7L * 24 * 60 * 60) // 7 day
                    .sameSite("Lax")
                    .build();

            // send cookie xuong client
            servletResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        }

        //
        return ResponseEntity.ok(Map.of(
                "accessToken", accessToken,
                "userId", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail(),
                "role", user.getRole().name(),
                "rememberMe", request.isRememberMe()
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @CookieValue(name = "refresh_token", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        if (refreshToken != null) {
            refreshTokenService.findByToken(refreshToken).ifPresent(token -> {
                refreshTokenService.revokeToken(token);
            });
        }

        // Xoá cookie
        ResponseCookie cookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .maxAge(0)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(Map.of("message", "Đăng xuất thành công"));
    }


    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()
                    || "anonymousUser".equals(authentication.getName())) {
                return ResponseEntity.status(401)
                        .body(new ErrorResponse("Không tìm thấy người dùng: Chưa đăng nhập hoặc token không hợp lệ"));
            }
            String email = authentication.getName(); // Lấy email từ token
            User user = userRepository.findByEmail(email) // Tìm theo email
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với email: " + email));
            return ResponseEntity.ok(new UserDTO(user.getId(), user.getUsername(), user.getEmail()));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(new ErrorResponse("Không tìm thấy người dùng: " + e.getMessage()));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> req) {
        try {
            String email = req.get("email");
            String otp = passwordResetService.createPasswordResetToken(email); // nhận OTP
            return ResponseEntity.ok(Map.of("message", "Đã gửi OTP vào email", "otp", otp // chỉ dùng để test
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> req) {
        try {
            String otp = req.get("token");
            String newPassword = req.get("newPassword");
            passwordResetService.resetPassword(otp, newPassword);
            return ResponseEntity.ok(Map.of("message", "Đổi mật khẩu thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        try {
            PasswordResetToken resetToken = tokenRepo.findByToken(token)
                    .orElseThrow(() -> new RuntimeException("OTP không hợp lệ"));

            if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("OTP đã hết hạn");
            }

            return ResponseEntity.ok(Map.of("message", "OTP hợp lệ"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/register/student")
    public ResponseEntity<String> registerStudent(@RequestBody StudentRegisterRequest req) {
        String msg = authService.registerStudent(req);
        return ResponseEntity.ok(msg);
    }

    @PostMapping("/register/parent")
    public ResponseEntity<String> registerParent(@RequestBody ParentRegisterRequest req) {
        String msg = authService.registerParent(req);
        return ResponseEntity.ok(msg);
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verifyUser(
            @RequestParam String email,
            @RequestParam String token
    ) {
        try {
            authService.verifyUserByLink(email, token);

            String redirectUrl = feLoginRedirect + "?activated=success";
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(redirectUrl))
                    .build();

        } catch (Exception e) {

            String redirectUrl = feLoginRedirect + "?activated=fail";
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(redirectUrl))
                    .build();
        }
    }





    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(
            @CookieValue(name = "refresh_token", required = false) String refreshToken
    ) {
        if (refreshToken == null || !jwtUtil.validateToken(refreshToken) || !jwtUtil.isRefreshToken(refreshToken)) {
            return ResponseEntity.status(401).body(Map.of("message", "Refresh token không hợp lệ hoặc đã hết hạn"));
        }

        Long userId = jwtUtil.getUserId(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        String newAccess = jwtUtil.generateAccessToken(userId, user.getEmail(), user.getUsername(), user.getRole().name());

        return ResponseEntity.ok(Map.of(
                "accessToken", newAccess
        ));
    }


    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> req) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || auth.getName().equals("anonymousUser")) {
                return ResponseEntity.status(401).body(Map.of("message", "Chưa đăng nhập"));
            }

            String email = auth.getName(); // Lấy email từ token
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

            String oldPassword = req.get("oldPassword");
            String newPassword = req.get("newPassword");

            if (oldPassword == null || newPassword == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Thiếu dữ liệu"));
            }

            // Kiểm tra mật khẩu cũ
            if (!authService.checkPassword(oldPassword, user.getPassword())) {
                return ResponseEntity.badRequest().body(Map.of("message", "Mật khẩu cũ không đúng"));
            }

            // Lưu mật khẩu mới
            authService.updatePassword(user, newPassword);

            return ResponseEntity.ok(Map.of("message", "Đổi mật khẩu thành công!"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", e.getMessage()));
        }
    }

}
