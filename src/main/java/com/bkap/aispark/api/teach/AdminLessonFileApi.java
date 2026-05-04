package com.bkap.aispark.api.teach;

import com.bkap.aispark.security.JwtUtil;
import com.bkap.aispark.service.teach.AdminLessonFileService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/admin/lessons")
public class AdminLessonFileApi {

    @Autowired
    private AdminLessonFileService adminLessonFileService;

    @Autowired
    private JwtUtil jwtUtil;

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Missing Authorization header"
            );
        }

        return header.substring(7);
    }

    private void checkAdminRole(HttpServletRequest request) {
        String token = extractToken(request);

        if (!jwtUtil.validateToken(token)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid token"
            );
        }

        String role = jwtUtil.getRole(token);

        if (!isAdminRole(role)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Thao tác này yêu cầu quyền Admin hoặc System Admin"
            );
        }
    }

    private boolean isAdminRole(String role) {
        return "ADMIN".equals(role) || "SYSTEM_ADMIN".equals(role);
    }

    @PostMapping("/{lessonId}/files")
    public ResponseEntity<?> uploadLessonMaterial(
            HttpServletRequest request,
            @PathVariable Long lessonId,
            @RequestParam("file") MultipartFile file
    ) {
        checkAdminRole(request);

        try {
            var response = adminLessonFileService.uploadLessonMaterial(lessonId, file);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }

    @PostMapping("/{lessonId}/cover")
    public ResponseEntity<?> uploadLessonCover(
            HttpServletRequest request,
            @PathVariable Long lessonId,
            @RequestParam("file") MultipartFile file
    ) {
        checkAdminRole(request);

        try {
            String coverUrl = adminLessonFileService.uploadLessonCover(lessonId, file);
            return ResponseEntity.ok(coverUrl);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }

    @DeleteMapping("/files/{fileId}")
    public ResponseEntity<?> deleteLessonFile(
            HttpServletRequest request,
            @PathVariable Long fileId
    ) {
        checkAdminRole(request);

        try {
            adminLessonFileService.deleteLessonFile(fileId);
            return ResponseEntity.ok("Xóa file thành công");
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }
}