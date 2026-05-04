package com.bkap.aispark.api.teach;

import com.bkap.aispark.dto.teach.AdminTeacherGradeRequest;
import com.bkap.aispark.dto.teach.AdminTeacherGradeResponse;
import com.bkap.aispark.security.JwtUtil;
import com.bkap.aispark.service.teach.AdminTeacherGradeService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/admin/teacher-grades")
public class AdminTeacherGradeApi {

    @Autowired
    private AdminTeacherGradeService adminTeacherGradeService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping
    public ResponseEntity<List<AdminTeacherGradeResponse>> getTeachersWithGrades(
            HttpServletRequest request
    ) {
        checkAdminRole(request);
        return ResponseEntity.ok(adminTeacherGradeService.getTeachersWithGrades());
    }

    @GetMapping("/{teacherId}")
    public ResponseEntity<AdminTeacherGradeResponse> getTeacherGrades(
            HttpServletRequest request,
            @PathVariable Long teacherId
    ) {
        checkAdminRole(request);
        return ResponseEntity.ok(adminTeacherGradeService.getTeacherGrades(teacherId));
    }

    @PutMapping("/{teacherId}")
    public ResponseEntity<AdminTeacherGradeResponse> updateTeacherGrades(
            HttpServletRequest request,
            @PathVariable Long teacherId,
            @RequestBody AdminTeacherGradeRequest body
    ) {
        checkAdminRole(request);
        return ResponseEntity.ok(
                adminTeacherGradeService.updateTeacherGrades(teacherId, body)
        );
    }

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
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }

        String role = jwtUtil.getRole(token);

        if (!"ADMIN".equals(role) && !"SYSTEM_ADMIN".equals(role)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Thao tác này yêu cầu quyền Admin"
            );
        }
    }
}