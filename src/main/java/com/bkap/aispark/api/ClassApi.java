package com.bkap.aispark.api;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bkap.aispark.dto.ClassesDTO;
import com.bkap.aispark.repository.ClassesRepository;

@RestController
@RequestMapping("class")
public class ClassApi {

    @Autowired
    private ClassesRepository classesRepository;

    @GetMapping
    public List<ClassesDTO> getAll() {
        return classesRepository.findAll()
                .stream()
                .map(ClassesDTO::new)
                .collect(Collectors.toList());
    }

}
