package com.bkap.aispark.api;

import com.bkap.aispark.dto.StudentGoalRequest;
import com.bkap.aispark.entity.StudentGoal;
import com.bkap.aispark.service.StudentGoalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student-goals")
public class StudentGoalApi {

    @Autowired
    private StudentGoalService studentGoalService;

    // Lấy tất cả goals của 1 student
    @GetMapping("/student/{studentId}")
    public List<StudentGoal> getGoalsByStudent(@PathVariable Long studentId) {
        return studentGoalService.getGoalsByStudent(studentId);
    }

    // Lấy chi tiết 1 goal
    @GetMapping("/{id}")
    public ResponseEntity<StudentGoal> getGoalById(@PathVariable Long id) {
        return studentGoalService.getGoalById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Tạo mới goal cho student
    @PostMapping("/student/{studentId}")
    public ResponseEntity<StudentGoal> addGoal(
            @PathVariable Long studentId,
            @RequestBody StudentGoalRequest req) {

        StudentGoal goal = new StudentGoal();
        goal.setGoal(req.getGoal());
        goal.setStatus(req.getStatus());
        goal.setDeadline(req.getDeadline());

        StudentGoal saved = studentGoalService.addGoal(studentId, goal);
        return ResponseEntity.ok(saved);
    }

    // Cập nhật goal
    @PutMapping("/{id}")
    public ResponseEntity<StudentGoal> updateGoal(
            @PathVariable Long id,
            @RequestBody StudentGoalRequest req) {

        StudentGoal goal = new StudentGoal();
        goal.setGoal(req.getGoal());
        goal.setStatus(req.getStatus());
        goal.setDeadline(req.getDeadline());

        try {
            return studentGoalService.updateGoal(id, goal)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // Xóa goal
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGoal(@PathVariable Long id) {
        boolean deleted = studentGoalService.deleteGoal(id);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
}
