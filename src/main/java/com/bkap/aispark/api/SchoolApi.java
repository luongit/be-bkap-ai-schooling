package com.bkap.aispark.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bkap.aispark.entity.Schools;
import com.bkap.aispark.repository.SchoolsRepository;

@RestController
@RequestMapping("/schools")
public class SchoolApi {

    private final SchoolsRepository schoolRepository;

    public SchoolApi(SchoolsRepository schoolRepository) {
        this.schoolRepository = schoolRepository;
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

}
