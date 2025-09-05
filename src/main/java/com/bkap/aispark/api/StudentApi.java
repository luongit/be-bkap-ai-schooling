package com.bkap.aispark.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.bkap.aispark.dto.StudentRequest;
import com.bkap.aispark.entity.Student;
import com.bkap.aispark.service.StudentService;

@RestController
@RequestMapping("/api/students")
public class StudentApi {

    @Autowired private StudentService studentService;

    @GetMapping
    public List<Student> getAllStudent() {
        return studentService.getAllStudent();
    }

    @PostMapping
    public ResponseEntity<Student> addStudent(@RequestBody StudentRequest req) {
        Student student = new Student();
        student.setFullName(req.getFullName());
        student.setUsername(req.getUsername());
        student.setDefaultPassword(req.getDefaultPassword());
        student.setPhone(req.getPhone());
        student.setBirthdate(req.getBirthdate());
        student.setHobbies(req.getHobbies());

        Student saved = studentService.addStudent(student, req.getClassId(), req.getHobbies());
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Student> getByIdStudent(@PathVariable Long id) {
        return studentService.getByIdStudent(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Student> updateStudent(@PathVariable Long id, @RequestBody StudentRequest req) {
        Student student = new Student();
        student.setFullName(req.getFullName());
        student.setUsername(req.getUsername());
        student.setDefaultPassword(req.getDefaultPassword());
        student.setPhone(req.getPhone());
        student.setBirthdate(req.getBirthdate());
        student.setHobbies(req.getHobbies());

        try {
            return studentService.updateStudent(id, student, req.getClassId())
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        boolean deleted = studentService.deleteStudent(id);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
}

