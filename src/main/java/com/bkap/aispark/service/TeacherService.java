package com.bkap.aispark.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.bkap.aispark.dto.TeacherDTO;
import com.bkap.aispark.entity.Classes;
import com.bkap.aispark.entity.ObjectType;
import com.bkap.aispark.entity.Teacher;
import com.bkap.aispark.entity.User;
import com.bkap.aispark.entity.UserRole;
import com.bkap.aispark.repository.ClassesRepository;
import com.bkap.aispark.repository.TeacherRepository;
import com.bkap.aispark.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class TeacherService {
    @Autowired
    private TeacherRepository teacherRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ClassesRepository classRepository;

    public List<TeacherDTO> getTeachers() {
        return teacherRepository.findAllWithClassAndSchool();
    }

    public List<Teacher> getAllTeacher() {
        return teacherRepository.findAll();
    }

    @Transactional
    public Teacher addTeacher(Teacher teacher) {
        Teacher saved = teacherRepository.save(teacher);

        User user = new User();
        user.setEmail(saved.getEmail());
        user.setPhone(saved.getPhone());
        user.setPassword(passwordEncoder.encode("123456")); // mã hóa
        user.setRole(UserRole.TEACHER);
        user.setObjectType(ObjectType.TEACHER);
        user.setObjectId(saved.getId());
        user.setIsActive(true);

        userRepository.save(user);
        return saved;
    }

    public Optional<Teacher> getByIdTeacher(Long id) {
        return teacherRepository.findById(id);
    }

    public Optional<Teacher> updateTeacher(Long id, Teacher newTeacher) {
        return teacherRepository.findById(id).map(teacher -> {
            teacher.setEmail(newTeacher.getEmail());
            teacher.setCode(newTeacher.getCode());
            teacher.setFullName(newTeacher.getFullName());
            teacher.setPhone(newTeacher.getPhone());
            if (newTeacher.getHomeroomClass() != null && newTeacher.getHomeroomClass().getId() != null) {
                Classes clazz = classRepository.findById(newTeacher.getHomeroomClass().getId())
                        .orElseThrow(() -> new RuntimeException("Class not found"));
                teacher.setHomeroomClass(clazz);
            }
            return teacherRepository.save(teacher);
        });
    }

    @Transactional
    public Boolean deleteTeacher(Long id) {
        return teacherRepository.findById(id).map(teacher -> {
            // Xóa user tương ứng
            userRepository.findByObjectTypeAndObjectId(ObjectType.TEACHER, teacher.getId())
                    .ifPresent(userRepository::delete);

            // Xóa teacher
            teacherRepository.delete(teacher);
            return true;
        }).orElse(false);
    }

}
