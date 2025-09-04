package com.bkap.aispark.service;

import com.bkap.aispark.dto.ProfileDTO;
import com.bkap.aispark.entity.*;
import com.bkap.aispark.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {

    @Autowired private UserRepository userRepository;
    @Autowired private StudentRepository studentRepository;
    @Autowired private TeacherRepository teacherRepository;

    public ProfileDTO getProfileByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        ProfileDTO dto = new ProfileDTO();
        dto.setUserId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole());
        dto.setObjectType(user.getObjectType());

        switch (user.getObjectType()) {
            case STUDENT:
                Student student = studentRepository.findById(user.getObjectId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy học sinh"));
                dto.setFullName(student.getFullName());
                dto.setCode(student.getCode());
                if (student.getClassEntity() != null) {
                    dto.setClassName(student.getClassEntity().getName());
                }
                break;

            case TEACHER:
                Teacher teacher = teacherRepository.findById(user.getObjectId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy giáo viên"));
                dto.setFullName(teacher.getFullName());
                dto.setCode(teacher.getCode());
                if (teacher.getHomeroomClass() != null) {
                    dto.setHomeroom(teacher.getHomeroomClass().getName());
                }
                break;


            case SCHOOL:
            case SYSTEM:
                dto.setFullName("Administrator");
                break;
        }

        return dto;
    }
}
