package com.bkap.aispark.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.bkap.aispark.dto.ProfileDTO;
import com.bkap.aispark.security.JwtUtil;
import com.bkap.aispark.service.ProfileService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/profile")
public class ProfileApi {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private JwtUtil jwtUtil;

    // GET /api/profile -> lấy profile dựa trên userId trong token
    @GetMapping
    public ResponseEntity<ProfileDTO> getProfile(HttpServletRequest request) {
        String token = extractToken(request);
        if (!jwtUtil.validateToken(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
        Long userId = jwtUtil.getUserId(token);
        ProfileDTO dto = profileService.getProfileByUserId(userId);
        return ResponseEntity.ok(dto);
    }

    // PUT /api/profile -> update profile cho user đang login
    @PutMapping
    public ResponseEntity<ProfileDTO> updateProfile(HttpServletRequest request,
            @RequestBody ProfileDTO dto) {
        String token = extractToken(request);
        if (!jwtUtil.validateToken(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
        Long userId = jwtUtil.getUserId(token);
        ProfileDTO updated = profileService.updateProfile(userId, dto);
        return ResponseEntity.ok(updated);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing Authorization header");
        }
        return header.substring(7);
    }
//    @GetMapping("/{id}")
//    public ResponseEntity<ProfileDTO> getUserProfile(@PathVariable Long id) {
//        ProfileDTO dto = profileService.getProfileByUserId(id);
//        if (dto == null) {
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
//        }
//        return ResponseEntity.ok(dto);
//    }
//
//    // PUT /api/users/{id} -> admin cập nhật profile theo id
//    @PutMapping("/{id}")
//    public ResponseEntity<ProfileDTO> updateUserProfile(
//            @PathVariable Long id,
//            @RequestBody ProfileDTO dto) {
//        ProfileDTO updated = profileService.updateProfile(id, dto);
//        return ResponseEntity.ok(updated);
//    }

    @GetMapping("/{id}")
    public ProfileDTO getProfile(@PathVariable Long id) {
        return profileService.getProfileByUserId(id);
    }

    @PutMapping("/{id}")
    public ProfileDTO updateProfile(@PathVariable Long id, @RequestBody ProfileDTO dto) {
        return profileService.updateProfile(id, dto);
    }
}
