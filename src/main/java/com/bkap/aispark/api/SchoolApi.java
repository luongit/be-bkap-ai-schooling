package com.bkap.aispark.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
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

    @GetMapping
    public List<Schools> getAllSchools() {
        return schoolRepository.findAll();
    }

}
