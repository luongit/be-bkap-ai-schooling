package com.bkap.aispark.service;

import com.bkap.aispark.dto.ProfileDTO;
import com.bkap.aispark.entity.*;
import com.bkap.aispark.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProfileService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private TeacherRepository teacherRepository;

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
                dto.setBirthdate(student.getBirthdate());
                if (student.getClassEntity() != null) {
                    dto.setClassName(student.getClassEntity().getName());
                }
                if (student.getHobbies() != null && !student.getHobbies().isBlank()) {
                    ObjectMapper mapper = new ObjectMapper();
                    try {
                        dto.setHobbies(mapper.readValue(student.getHobbies(), new TypeReference<List<String>>() {
                        }));
                    } catch (Exception e) {
                        throw new RuntimeException("Lỗi parse hobbies: " + e.getMessage());
                    }
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

    public ProfileDTO updateProfile(Long userId, ProfileDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        // update thông tin chung
        if (dto.getEmail() != null)
            user.setEmail(dto.getEmail());
        if (dto.getPhone() != null)
            user.setPhone(dto.getPhone());
        userRepository.save(user);

        switch (user.getObjectType()) {
            case STUDENT:
                Student student = studentRepository.findById(user.getObjectId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy học sinh"));

                if (dto.getFullName() != null) {
                    student.setFullName(dto.getFullName());
                }
                if (dto.getBirthdate() != null) {
                    student.setBirthdate(dto.getBirthdate());
                }
                if (dto.getHobbies() != null) {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        String json = mapper.writeValueAsString(dto.getHobbies());
                        student.setHobbies(json);
                    } catch (Exception e) {
                        throw new RuntimeException("Lỗi khi lưu hobbies");
                    }
                }

                studentRepository.save(student);
                break;

            case TEACHER:
                Teacher teacher = teacherRepository.findById(user.getObjectId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy giáo viên"));

                if (dto.getFullName() != null) {
                    teacher.setFullName(dto.getFullName());
                }
                // không cho update homeroom

                teacherRepository.save(teacher);
                break;

            case SCHOOL:
            case SYSTEM:
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Administrator không được cập nhật profile");
        }

        return getProfileByUserId(userId);
    }


}
