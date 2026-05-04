package com.bkap.aispark.api.teach;

import com.bkap.aispark.dto.teach.AdminCourseRequest;
import com.bkap.aispark.dto.teach.AdminLessonRequest;
import com.bkap.aispark.dto.teach.TeacherLessonContentResponse;
import com.bkap.aispark.dto.teach.TeacherLessonResponse;
import com.bkap.aispark.entity.teach.Course;
import com.bkap.aispark.security.JwtUtil;
import com.bkap.aispark.service.teach.AdminCourseLessonService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminCourseLessonApi {

    @Autowired
    private AdminCourseLessonService adminCourseLessonService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/courses")
    public ResponseEntity<List<Course>> getCourses(
            HttpServletRequest request,
            @RequestParam(required = false) Integer grade,
            @RequestParam(required = false, name = "month") Integer teachingMonth,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean activeOnly
    ) {
        checkAdminRole(request);

        if (Boolean.TRUE.equals(activeOnly)) {
            return ResponseEntity.ok(
                    adminCourseLessonService.getActiveCourses(
                            grade,
                            teachingMonth,
                            keyword
                    )
            );
        }

        return ResponseEntity.ok(
                adminCourseLessonService.getCourses(
                        grade,
                        teachingMonth,
                        keyword
                )
        );
    }

    @PostMapping("/courses")
    public ResponseEntity<Course> createCourse(
            HttpServletRequest request,
            @RequestBody AdminCourseRequest body
    ) {
        checkAdminRole(request);
        return ResponseEntity.ok(adminCourseLessonService.createCourse(body));
    }

    @PutMapping("/courses/{courseId}")
    public ResponseEntity<Course> updateCourse(
            HttpServletRequest request,
            @PathVariable Long courseId,
            @RequestBody AdminCourseRequest body
    ) {
        checkAdminRole(request);
        return ResponseEntity.ok(adminCourseLessonService.updateCourse(courseId, body));
    }

    @PostMapping("/courses/{courseId}/cover")
    public ResponseEntity<String> uploadCourseCover(
            HttpServletRequest request,
            @PathVariable Long courseId,
            @RequestParam("file") MultipartFile file
    ) {
        checkAdminRole(request);
        return ResponseEntity.ok(adminCourseLessonService.uploadCourseCover(courseId, file));
    }

    @DeleteMapping("/courses/{courseId}")
    public ResponseEntity<String> deleteCourse(
            HttpServletRequest request,
            @PathVariable Long courseId
    ) {
        checkAdminRole(request);
        adminCourseLessonService.deleteCourse(courseId);
        return ResponseEntity.ok("Xóa khóa học thành công");
    }

    @GetMapping("/courses/{courseId}/lessons")
    public ResponseEntity<List<TeacherLessonResponse>> getLessonsByCourse(
            HttpServletRequest request,
            @PathVariable Long courseId,
            @RequestParam(required = false) Boolean includeHidden
    ) {
        checkAdminRole(request);

        if (Boolean.TRUE.equals(includeHidden)) {
            return ResponseEntity.ok(
                    adminCourseLessonService.getAllLessonsByCourse(courseId)
            );
        }

        return ResponseEntity.ok(adminCourseLessonService.getLessonsByCourse(courseId));
    }

    @PostMapping("/lessons")
    public ResponseEntity<TeacherLessonResponse> createLesson(
            HttpServletRequest request,
            @RequestBody AdminLessonRequest body
    ) {
        checkAdminRole(request);
        return ResponseEntity.ok(adminCourseLessonService.createLesson(body));
    }

    @PutMapping("/lessons/{lessonId}")
    public ResponseEntity<TeacherLessonResponse> updateLesson(
            HttpServletRequest request,
            @PathVariable Long lessonId,
            @RequestBody AdminLessonRequest body
    ) {
        checkAdminRole(request);
        return ResponseEntity.ok(adminCourseLessonService.updateLesson(lessonId, body));
    }

    @DeleteMapping("/lessons/{lessonId}")
    public ResponseEntity<String> deleteLesson(
            HttpServletRequest request,
            @PathVariable Long lessonId
    ) {
        checkAdminRole(request);
        adminCourseLessonService.deleteLesson(lessonId);
        return ResponseEntity.ok("Xóa bài học thành công");
    }

    @GetMapping("/lessons/{lessonId}")
    public ResponseEntity<TeacherLessonContentResponse> getLessonDetail(
            HttpServletRequest request,
            @PathVariable Long lessonId
    ) {
        checkAdminRole(request);
        return ResponseEntity.ok(adminCourseLessonService.getLessonDetail(lessonId));
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
}