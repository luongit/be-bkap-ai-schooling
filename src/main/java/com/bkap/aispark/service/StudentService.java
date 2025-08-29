package com.bkap.aispark.service;



import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.bkap.aispark.entity.ObjectType;
import com.bkap.aispark.entity.Student;
import com.bkap.aispark.entity.User;
import com.bkap.aispark.entity.UserRole;
import com.bkap.aispark.repository.StudentRepository;
import com.bkap.aispark.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class StudentService {
    @Autowired private StudentRepository studentRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    
    public List<Student> getAllStudent(){
    	return studentRepository.findAll();
    }
    
    @Transactional
    public Student addStudent(Student student) {
        Student saved = studentRepository.save(student);
        User user = new User();
        user.setEmail(saved.getEmail());
        user.setPhone(saved.getPhone());
        user.setPassword(passwordEncoder.encode("123456"));
        user.setRole(UserRole.STUDENT);
        user.setObjectType(ObjectType.STUDENT); 
        user.setObjectId(saved.getId());
        user.setStudentCode(saved.getCode());  
        user.setIsActive(true);

        userRepository.save(user);
        return saved;
    }
    
    public Optional<Student> getByIdStudent(Long id){
    	return studentRepository.findById(id);
    }
    
    public Optional<Student> updateStudent(Long id , Student newStudent){
    	return studentRepository.findById(id).map(student ->{
    		student.setClassId(newStudent.getClassId());
    		student.setCode(newStudent.getCode());
    		student.setEmail(newStudent.getEmail());
    		student.setFullName(newStudent.getFullName());
    		student.setPhone(newStudent.getPhone());
    		return studentRepository.save(student);
    	});
    }
    @Transactional
    public Boolean deleteStudent(Long id) {
        return studentRepository.findById(id).map(student -> {
            // Xóa user tương ứng
            userRepository.findByObjectTypeAndObjectId(ObjectType.STUDENT, student.getId())
                          .ifPresent(userRepository::delete);

            // Xóa student
            studentRepository.delete(student);
            return true;
        }).orElse(false);
    }

}

