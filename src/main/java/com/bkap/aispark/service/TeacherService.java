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
import com.bkap.aispark.entity.UserCredit; // Thêm import
import com.bkap.aispark.entity.UserRole;
import com.bkap.aispark.repository.ClassesRepository;
import com.bkap.aispark.repository.TeacherRepository;
import com.bkap.aispark.repository.UserRepository;
import com.bkap.aispark.repository.UserCreditRepository; // Thêm import

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
    @Autowired
    private UserCreditRepository userCreditRepository; // Inject repository

    public List<TeacherDTO> getTeachers() {
        return teacherRepository.findAllWithClassAndSchool();
    }

    public List<Teacher> getAllTeacher() {
        return teacherRepository.findAll();
    }

    @Transactional
    public Teacher addTeacher(Teacher teacher) {
        // Lưu teacher
        Teacher saved = teacherRepository.save(teacher);

        // Tạo user tương ứng
        User user = new User();
        user.setEmail(saved.getEmail());
        user.setPhone(saved.getPhone());
        user.setPassword(passwordEncoder.encode("123456")); // Mã hóa
        user.setRole(UserRole.TEACHER);
        user.setObjectType(ObjectType.TEACHER);
        user.setObjectId(saved.getId());
        user.setIsActive(true);
        User savedUser = userRepository.save(user);

        // Tạo UserCredit với 100 credit
        UserCredit credit = new UserCredit(savedUser, 3000, null); // null cho expiredDate
        userCreditRepository.save(credit);

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
            teacher.setHomeroomClass(null);
        }

        return teacherRepository.save(teacher);
    }

    @Transactional
    public Boolean deleteTeacher(Long id) {
        return teacherRepository.findById(id).map(teacher -> {
            // Xóa user tương ứng
            userRepository.findByObjectTypeAndObjectId(ObjectType.TEACHER, teacher.getId())
                    .ifPresent(user -> {
                        // Xóa UserCredit trước
                        userCreditRepository.findByUserId(user.getId())
                                .ifPresent(userCreditRepository::delete);
                        userRepository.delete(user);
                    });

            // Xóa teacher
            teacherRepository.delete(teacher);
            return true;
        }).orElse(false);
    }

    public boolean existsByEmail(String email) {
        return teacherRepository.existsByEmail(email);
    }

    public boolean existsByCode(String code) {
        return teacherRepository.existsByCode(code);
    }
}