package com.bkap.aispark.api;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bkap.aispark.dto.LoginRequest;
import com.bkap.aispark.dto.LoginResponse;
import com.bkap.aispark.dto.ParentRegisterRequest;
import com.bkap.aispark.dto.StudentRegisterRequest;
import com.bkap.aispark.dto.UserDTO;
import com.bkap.aispark.entity.PasswordResetToken;
import com.bkap.aispark.entity.User;
import com.bkap.aispark.repository.PasswordResetTokenRepository;
import com.bkap.aispark.repository.UserRepository;
import com.bkap.aispark.service.AuthService;
import com.bkap.aispark.service.PasswordResetService;
import com.bkap.aispark.service.UserService;

import jakarta.servlet.http.HttpServletResponse;

@CrossOrigin(origins = { "http://bkapai.vn", "http://localhost:3000" })
@RestController
@RequestMapping("/api/auth")
public class AuthApi {
	@Autowired
	private PasswordResetTokenRepository tokenRepo;
	@Autowired
	private PasswordResetService passwordResetService;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private AuthService authService;

	private final UserService userService;

	public AuthApi(UserService userService) {
		this.userService = userService;
	}

	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
		LoginResponse response = authService.login(request.getIdentifier(), request.getPassword());
		return ResponseEntity.ok(response);
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
			String otp = passwordResetService.createPasswordResetToken(email); // ✅ nhận OTP
			return ResponseEntity.ok(Map.of("message", "Đã gửi OTP vào email", "otp", otp // ⚠️ chỉ dùng để test
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
	public void verifyUser(@RequestParam String email, @RequestParam String token,
			HttpServletResponse response) throws IOException {
		authService.verifyUserByLink(email, token); // gọi Service đúng cách
		// redirect về frontend login
		response.sendRedirect("http://bkapai.vn/auth/login");
	}

}
