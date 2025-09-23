package com.bkap.aispark.api;

import com.bkap.aispark.entity.User;
import com.bkap.aispark.security.JwtUtil;
import com.bkap.aispark.service.CreditService;
import com.bkap.aispark.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserApi {

    private final CreditService creditService;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    public UserApi(CreditService creditService, JwtUtil jwtUtil, UserService userService) {
        this.creditService = creditService;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    // --- API credits ---
    @GetMapping("/credits")
    public ResponseEntity<Map<String, Object>> getUserCredits(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body(Map.of(
                        "error", "UNAUTHORIZED",
                        "message", "Missing or invalid Authorization header"
                ));
            }
            String token = authHeader.substring(7);
            Long userId = jwtUtil.getUserId(token);
            int credits = creditService.getRemainingCredit(userId);
            return ResponseEntity.ok(Map.of("credit", credits));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of(
                    "error", "INVALID_TOKEN",
                    "message", "Invalid token"
            ));
        }
    }

    // --- API quản lý user ---
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user,
                                           @RequestHeader("X-User-Id") Long actorId) {
        return ResponseEntity.ok(userService.createUser(actorId, user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id,
                                           @RequestBody User updatedUser,
                                           @RequestHeader("X-User-Id") Long actorId) {
        return ResponseEntity.ok(userService.updateUser(actorId, id, updatedUser));
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long id,
                                               @RequestHeader("X-User-Id") Long actorId) {
        userService.deactivateUser(actorId, id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<Void> activateUser(@PathVariable Long id,
                                             @RequestHeader("X-User-Id") Long actorId) {
        userService.activateUser(actorId, id);
        return ResponseEntity.ok().build();
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id,
                                                         @RequestHeader("X-User-Id") Long actorId,
                                                         @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader != null && authHeader.startsWith("Bearer ") ? authHeader.substring(7) : null;
            boolean deleted = userService.deleteUser(actorId, id, token);
            if (deleted) {
                return ResponseEntity.ok(Map.of("message", "Xóa người dùng thành công"));
            } else {
                return ResponseEntity.status(404).body(Map.of("message", "Không tìm thấy người dùng"));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("message", "Lỗi khi xóa người dùng: " + e.getMessage()));
        }
    }
}
