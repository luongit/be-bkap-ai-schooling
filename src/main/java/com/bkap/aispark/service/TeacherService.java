package com.bkap.aispark.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.bkap.aispark.dto.TeacherDTO;
import com.bkap.aispark.dto.TeacherRequestDTO;
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

    public Teacher updateTeacher(Long id, TeacherRequestDTO dto) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        teacher.setFullName(dto.getFullName());
        teacher.setEmail(dto.getEmail());
        teacher.setPhone(dto.getPhone());
        teacher.setIsActive(dto.getIsActive());

        if (dto.getClassId() != null) {
            Classes cls = classRepository.findById(dto.getClassId())
                    .orElseThrow(() -> new RuntimeException("Class not found"));
            teacher.setHomeroomClass(cls);
        } else {
            teacher.setHomeroomClass(null); // nếu bỏ chọn lớp
        }

        return teacherRepository.save(teacher);
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
