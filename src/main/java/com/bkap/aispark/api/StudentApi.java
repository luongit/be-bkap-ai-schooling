package com.bkap.aispark.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.bkap.aispark.entity.Student;
import com.bkap.aispark.service.StudentService;

@RestController
@RequestMapping("/api/students")
public class StudentApi {

    @Autowired private StudentService studentService;

    // Lấy danh sách tất cả sinh viên
    @GetMapping
    public List<Student> getAllStudent() {
        return studentService.getAllStudent();
    }

    // Thêm mới sinh viên 
    @PostMapping
    public ResponseEntity<Student> addStudent(
            @RequestBody Student student,
            @RequestParam Long classId,
            @RequestParam(required = false) String hobbies) {

        Student saved = studentService.addStudent(student, classId, hobbies);
        return ResponseEntity.ok(saved);
    }

    // Lấy thông tin sinh viên theo ID
    @GetMapping("/{id}")
    public ResponseEntity<Student> getByIdStudent(@PathVariable Long id) {
        return studentService.getByIdStudent(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Cập nhật thông tin sinh viên (classId có thể null)
    @PutMapping("/{id}")
    public ResponseEntity<Student> updateStudent(
            @PathVariable Long id,
            @RequestBody Student student,
            @RequestParam(required = false) Long classId) {

        try {
            return studentService.updateStudent(id, student, classId)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // Xóa sinh viên theo ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        boolean deleted = studentService.deleteStudent(id);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
}
