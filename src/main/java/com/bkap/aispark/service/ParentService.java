package com.bkap.aispark.service;


import com.bkap.aispark.entity.Parent;
import com.bkap.aispark.entity.Student;
import com.bkap.aispark.repository.ParentRepository;
import com.bkap.aispark.repository.StudentRepository;
import com.bkap.aispark.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ParentService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private ParentRepository parentRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;


    public List<Parent> getAllParent() {
        return parentRepository.findAll();
    }


    // ----------------- SINH MÃ PHỤ HUYNH -----------------
    public String generateParentCode() {
        String code;
        do {
            code = "PH-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
        } while (parentRepository.existsByCode(code));
        return code;
    }
}
