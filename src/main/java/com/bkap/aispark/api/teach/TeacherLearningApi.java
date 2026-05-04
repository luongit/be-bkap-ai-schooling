package com.bkap.aispark.api.teach;

import com.bkap.aispark.dto.teach.TeacherLessonContentResponse;
import com.bkap.aispark.dto.teach.TeacherLessonResponse;
import com.bkap.aispark.entity.teach.Course;
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
@RequestMapping("/api/teacher")
public class TeacherLearningApi {

    @Autowired
    private TeacherLessonService teacherLessonService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private TeacherRepository teacherRepository;

    /**
     * Course:
     * Giáo viên phụ trách khối nào thì thấy Course thuộc khối đó.
     */
    @GetMapping("/courses")
    public ResponseEntity<List<Course>> getTeacherCourses(
            HttpServletRequest request,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer grade,
            @RequestParam(required = false, name = "month") Integer teachingMonth
    ) {
        Long teacherId = getTeacherIdFromToken(request);

        return ResponseEntity.ok(
                teacherLessonService.teacherCoursesByAssignedGrades(
                        teacherId,
                        keyword,
                        grade,
                        teachingMonth
                )
        );
    }

    /**
     * Course -> Lesson:
     * Vào 1 Course thì chỉ thấy Lesson thuộc khối giáo viên phụ trách.
     */
    @GetMapping("/courses/{courseId}/lessons")
    public ResponseEntity<List<TeacherLessonResponse>> getLessonsByCourse(
            HttpServletRequest request,
            @PathVariable Long courseId,
            @RequestParam(required = false) Integer grade,
            @RequestParam(required = false, name = "month") Integer teachingMonth,
            @RequestParam(required = false) String keyword
    ) {
        Long teacherId = getTeacherIdFromToken(request);

        return ResponseEntity.ok(
                teacherLessonService.teacherLessonsByCourse(
                        teacherId,
                        courseId,
                        grade,
                        teachingMonth,
                        keyword
                )
        );
    }

    /**
     * Lesson -> File:
     * Chi tiết bài học và danh sách file.
     */
    @GetMapping("/lessons/{lessonId}")
    public ResponseEntity<TeacherLessonContentResponse> getLessonDetail(
            HttpServletRequest request,
            @PathVariable Long lessonId
    ) {
        Long teacherId = getTeacherIdFromToken(request);

        return ResponseEntity.ok(
                teacherLessonService.getLessonContent(lessonId, teacherId)
        );
    }

    /**
     * API cũ nếu FE vẫn đang dùng:
     * Lấy trực tiếp toàn bộ Lesson theo khối giáo viên.
     */
    @GetMapping("/lessons")
    public ResponseEntity<List<TeacherLessonResponse>> getAssignedLessons(
            HttpServletRequest request,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer grade,
            @RequestParam(required = false, name = "month") Integer teachingMonth
    ) {
        Long teacherId = getTeacherIdFromToken(request);

        return ResponseEntity.ok(
                teacherLessonService.teacherLessonByAssignedGrades(
                        teacherId,
                        keyword,
                        grade,
                        teachingMonth
                )
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