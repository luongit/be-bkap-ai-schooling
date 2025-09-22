package com.bkap.aispark.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.bkap.aispark.entity.Classes;
import com.bkap.aispark.entity.ObjectType;
import com.bkap.aispark.entity.Student;
import com.bkap.aispark.entity.User;
import com.bkap.aispark.entity.UserCredit; // Thêm import
import com.bkap.aispark.entity.UserRole;
import com.bkap.aispark.repository.ClassesRepository;
import com.bkap.aispark.repository.StudentRepository;
import com.bkap.aispark.repository.UserRepository;
import com.bkap.aispark.repository.UserCreditRepository; // Thêm import

import jakarta.transaction.Transactional;

@Service
public class StudentService {

    @Autowired private StudentRepository studentRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private ClassesRepository classesRepository;
    @Autowired private UserCreditRepository userCreditRepository; // Inject repository

    public List<Student> getAllStudent() {
        return studentRepository.findAll();
    }

    @Transactional
    public Student addStudent(Student student, Long classId, String hobbies) {
        if (student.getCode() == null || student.getCode().isBlank()) {
            student.setCode(generateStudentCode());
        }

        // Gán sở thích
        student.setHobbies(hobbies);

        // Tìm class theo classId
        Classes clazz = classesRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lớp với id: " + classId));

        student.setClassEntity(clazz);

        // Lưu student
        Student saved = studentRepository.save(student);

        // Tạo user tương ứng
        User user = new User();
        user.setUsername(saved.getUsername());
        user.setPhone(saved.getPhone());
        user.setPassword(passwordEncoder.encode(saved.getDefaultPassword()));
        user.setRole(UserRole.STUDENT);
        user.setObjectType(ObjectType.STUDENT);
        user.setObjectId(saved.getId());
        user.setIsActive(true);
        User savedUser = userRepository.save(user);

        // Tạo UserCredit với 100 credit
        UserCredit credit = new UserCredit(savedUser, 100, null); // null cho expiredDate
        userCreditRepository.save(credit);

        return saved;
    }

    private String generateStudentCode() {
        long count = studentRepository.count();
        return "HS" + java.time.Year.now().getValue() + "-" + String.format("%04d", count + 1);
    }

    public Optional<Student> getByIdStudent(Long id) {
        return studentRepository.findById(id);
    }

    public Optional<Student> updateStudent(Long id, Student newStudent, Long classId) {
        return studentRepository.findById(id).map(student -> {
            student.setFullName(newStudent.getFullName());
            student.setUsername(newStudent.getUsername());
            student.setDefaultPassword(newStudent.getDefaultPassword());
            student.setPhone(newStudent.getPhone());
            student.setBirthdate(newStudent.getBirthdate());
            student.setHobbies(newStudent.getHobbies());

            // Gán lại class nếu có
            if (classId != null) {
                Classes clazz = classesRepository.findById(classId)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy lớp với id: " + classId));
                student.setClassEntity(clazz);
            }

            return studentRepository.save(student);
        });
    }

    @Transactional
    public Boolean deleteStudent(Long id) {
        return studentRepository.findById(id).map(student -> {
            userRepository.findByObjectTypeAndObjectId(ObjectType.STUDENT, student.getId())
                    .ifPresent(user -> {
                        // Xóa UserCredit trước
                        userCreditRepository.findByUserId(user.getId())
                                .ifPresent(userCreditRepository::delete);
                        userRepository.delete(user);
                    });
            studentRepository.delete(student);
            return true;
        }).orElse(false);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean existsByPhone(String phone) {
        return userRepository.existsByPhone(phone);
    }
}