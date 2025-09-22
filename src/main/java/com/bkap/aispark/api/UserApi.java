package com.bkap.aispark.api;

import com.bkap.aispark.security.JwtUtil;
import com.bkap.aispark.service.CreditService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserApi {

    private final CreditService creditService;
    private final JwtUtil jwtUtil;

    public UserApi(CreditService creditService, JwtUtil jwtUtil) {
        this.creditService = creditService;
        this.jwtUtil = jwtUtil;
    }

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
}