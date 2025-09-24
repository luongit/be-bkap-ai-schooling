package com.bkap.aispark.api;

import com.bkap.aispark.dto.StudentGoalRequest;
import com.bkap.aispark.entity.StudentGoal;
import com.bkap.aispark.entity.User;
import com.bkap.aispark.entity.ObjectType;
import com.bkap.aispark.repository.UserRepository;
import com.bkap.aispark.security.JwtUtil;
import com.bkap.aispark.service.StudentGoalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/student-goals")
public class StudentGoalApi {

    @Autowired
    private StudentGoalService studentGoalService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    // Lấy tất cả goals của 1 student
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<StudentGoal>> getGoalsByStudent(
            @PathVariable Long studentId, HttpServletRequest request) {
        validateStudentAccess(studentId, request);
        List<StudentGoal> goals = studentGoalService.getGoalsByStudent(studentId);
        return ResponseEntity.ok(goals);
    }

    // Lấy chi tiết 1 goal
    @GetMapping("/{id}")
    public ResponseEntity<StudentGoal> getGoalById(
            @PathVariable Long id, HttpServletRequest request) {
        StudentGoal goal = studentGoalService.getGoalById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy mục tiêu"));
        validateStudentAccess(goal.getStudent().getId(), request);
        return ResponseEntity.ok(goal);
    }

    // Tạo mới goal cho student
    @PostMapping("/student/{studentId}")
    public ResponseEntity<StudentGoal> addGoal(
            @PathVariable Long studentId,
            @RequestBody StudentGoalRequest req,
            HttpServletRequest request) {
        validateStudentAccess(studentId, request);

        StudentGoal goal = new StudentGoal();
        goal.setGoal(req.getGoal());
        goal.setSubject(req.getSubject());
        goal.setLevel(req.getLevel());
        goal.setStyle(req.getStyle());
        goal.setStatus(req.getStatus());
        goal.setDeadline(req.getDeadline());

        StudentGoal saved = studentGoalService.addGoal(studentId, goal);
        return ResponseEntity.ok(saved);
    }

    // Cập nhật goal
    @PutMapping("/{id}")
    public ResponseEntity<StudentGoal> updateGoal(
            @PathVariable Long id,
            @RequestBody StudentGoalRequest req,
            HttpServletRequest request) {
        StudentGoal goal = studentGoalService.getGoalById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy mục tiêu"));
        validateStudentAccess(goal.getStudent().getId(), request);

        goal.setGoal(req.getGoal());
        goal.setSubject(req.getSubject());
        goal.setLevel(req.getLevel());
        goal.setStyle(req.getStyle());
        goal.setStatus(req.getStatus());
        goal.setDeadline(req.getDeadline());

        try {
            return studentGoalService.updateGoal(id, goal)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lỗi khi cập nhật mục tiêu: " + e.getMessage());
        }
    }

    // Xóa goal
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGoal(
            @PathVariable Long id, HttpServletRequest request) {
        StudentGoal goal = studentGoalService.getGoalById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy mục tiêu"));
        validateStudentAccess(goal.getStudent().getId(), request);

        boolean deleted = studentGoalService.deleteGoal(id);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    // Hàm kiểm tra quyền truy cập
    private void validateStudentAccess(Long studentId, HttpServletRequest request) {
        String token = extractToken(request);
        if (!jwtUtil.validateToken(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token không hợp lệ");
        }
        Long userId = jwtUtil.getUserId(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy user"));
        if (!user.getObjectType().equals(ObjectType.STUDENT) || !user.getObjectId().equals(studentId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Không có quyền truy cập");
        }
    }

    // Hàm trích xuất token từ header
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Thiếu header Authorization");
        }
        return header.substring(7);
    }
}