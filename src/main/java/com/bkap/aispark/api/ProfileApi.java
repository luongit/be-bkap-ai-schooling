package com.bkap.aispark.api;

import com.bkap.aispark.dto.ProfileDTO;
import com.bkap.aispark.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileApi {

    @Autowired
    private ProfileService profileService;

    @GetMapping("/{userId}")
    public ProfileDTO getProfile(@PathVariable Long userId) {
        return profileService.getProfileByUserId(userId);
    }
    @PutMapping("/{userId}")
    public ResponseEntity<?> updateProfile(@PathVariable Long userId, @RequestBody ProfileDTO dto) {
        return ResponseEntity.ok(profileService.updateProfile(userId, dto));
    }
}
