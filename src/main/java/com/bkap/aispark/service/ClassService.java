package com.bkap.aispark.service;

import com.bkap.aispark.entity.Classes;

import com.bkap.aispark.repository.ClassesRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClassService {

    @Autowired
    private ClassesRepository classRepository;

    public List<Classes> getAllClasses() {
        return classRepository.findAll();
    }

    public Classes getByName(String name) {
        return classRepository.findByName(name)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy lớp: " + name));
    }
}
