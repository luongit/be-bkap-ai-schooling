package com.bkap.aispark.api.teach;

import com.bkap.aispark.dto.teach.TeacherLessonContentResponse;
import com.bkap.aispark.dto.teach.TeacherLessonResponse;
import com.bkap.aispark.repository.TeacherRepository;
import com.bkap.aispark.security.JwtUtil;
import com.bkap.aispark.service.teach.TeacherLessonService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/teachers/lessons")
public class TeacherLessonApi {

    @Autowired
    private TeacherLessonService teacherLessonService;

    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private TeacherRepository teacherRepository;

    /**
     * API: Lấy danh sách bài giảng theo khối được phân công
     */
    @GetMapping
    public ResponseEntity<List<TeacherLessonResponse>> getAssignedLessons(
            HttpServletRequest request,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer grade,
            @RequestParam(required = false) Integer month
    ) {
        Long teacherId = getTeacherIdFromToken(request);

        List<TeacherLessonResponse> lessons =
                teacherLessonService.teacherLessonByAssignedGrades(
                        teacherId, keyword, grade, month
                );

        return ResponseEntity.ok(lessons);
    }

    /**
     * API: Lấy chi tiết tài liệu của một bài giảng
     */
    @GetMapping("/{lessonId}")
    public ResponseEntity<TeacherLessonContentResponse> getLessonDetail(
            HttpServletRequest request,
            @PathVariable Long lessonId
    ) {
        Long teacherId = getTeacherIdFromToken(request);

        TeacherLessonContentResponse content =
                teacherLessonService.getLessonContent(lessonId, teacherId);

        return ResponseEntity.ok(content);
    }

    // --- HELPER METHODS ---

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

    private Long getTeacherIdFromToken(HttpServletRequest request) {
        String token = extractToken(request);

        if (!jwtUtil.validateToken(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }

        String role = jwtUtil.getRole(token);
        if (!"TEACHER".equals(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Không có quyền giáo viên");
        }

        String email = jwtUtil.getEmail(token);

        return teacherRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giáo viên theo email"))
                .getId();
    }
}