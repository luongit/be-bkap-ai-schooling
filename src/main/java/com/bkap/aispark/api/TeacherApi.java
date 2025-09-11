package com.bkap.aispark.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bkap.aispark.dto.TeacherDTO;
import com.bkap.aispark.dto.TeacherRequestDTO;
import com.bkap.aispark.entity.Teacher;
import com.bkap.aispark.service.TeacherService;

@RestController
@RequestMapping("/api/teachers")
public class TeacherApi {

    @Autowired
    private TeacherService teacherService;

    @GetMapping
    public ResponseEntity<List<TeacherDTO>> getTeachers() {
        List<TeacherDTO> teachers = teacherService.getTeachers();
        return ResponseEntity.ok(teachers);
    }

    @PostMapping
    public ResponseEntity<Teacher> addTeacher(@RequestBody Teacher teacher) {
        Teacher saved = teacherService.addTeacher(teacher);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Teacher> getByIdTeacher(@PathVariable Long id) {
        return teacherService.getByIdTeacher(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<TeacherDTO> updateTeacher(
            @PathVariable Long id,
            @RequestBody TeacherRequestDTO dto) {

        Teacher updated = teacherService.updateTeacher(id, dto);

        TeacherDTO response = new TeacherDTO(
                updated.getId(),
                updated.getFullName(),
                updated.getEmail(),
                updated.getPhone(),
                updated.getCode(),
                updated.getIsActive(),
                updated.getCreatedAt(),
                updated.getHomeroomClass() != null ? updated.getHomeroomClass().getName() : null,
                updated.getHomeroomClass() != null ? updated.getHomeroomClass().getSchool().getName() : null);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTeacher(@PathVariable Long id) {
        boolean deleted = teacherService.deleteTeacher(id);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/check-email")
    public ResponseEntity<?> checkEmail(@org.springframework.web.bind.annotation.RequestParam String email) {
        boolean exists = teacherService.existsByEmail(email);
        return ResponseEntity.ok().body(java.util.Collections.singletonMap("exists", exists));
    }

    @GetMapping("/check-code")
    public ResponseEntity<?> checkCode(@org.springframework.web.bind.annotation.RequestParam String code) {
        boolean exists = teacherService.existsByCode(code);
        return ResponseEntity.ok().body(java.util.Collections.singletonMap("exists", exists));
    }

}
