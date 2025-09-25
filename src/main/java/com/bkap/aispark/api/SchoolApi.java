package com.bkap.aispark.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bkap.aispark.dto.CreateSchoolResponse;
import com.bkap.aispark.entity.Schools;
import com.bkap.aispark.repository.SchoolsRepository;
import com.bkap.aispark.service.SchoolService;

@RestController
@RequestMapping("api/schools")
public class SchoolApi {
    private final SchoolsRepository schoolRepository;
    private final SchoolService schoolService;

    public SchoolApi(SchoolsRepository schoolRepository, SchoolService schoolService) {
        this.schoolRepository = schoolRepository;
        this.schoolService = schoolService;
    }

    // Lay tat ca cac truong
    @GetMapping
    public List<Schools> getAllSchools() {
        return schoolRepository.findAll();
    }

    // Lay ra 1 truong
    @GetMapping("/{id}")
    public ResponseEntity<Schools> getSchoolById(@PathVariable Long id) {
        return schoolRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Them 1 truong
    @PostMapping
    public ResponseEntity<CreateSchoolResponse> createSchool(@RequestBody Schools school) {
        CreateSchoolResponse resp = schoolService.createSchoolWithAdmin(school);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    // Sua thong tin truong qua ID
    @PutMapping("/{id}")
    public ResponseEntity<Schools> updateSchool(@PathVariable Long id, @RequestBody Schools schoolDetails) {
        return schoolRepository.findById(id)
                .map(school -> {
                    school.setName(schoolDetails.getName());
                    school.setAddress(schoolDetails.getAddress());
                    return ResponseEntity.ok(schoolRepository.save(school));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Xoa 1 trường qua id
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSchool(@PathVariable Long id) {
        return schoolRepository.findById(id)
                .map(school -> {
                    schoolRepository.delete(school);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

}
