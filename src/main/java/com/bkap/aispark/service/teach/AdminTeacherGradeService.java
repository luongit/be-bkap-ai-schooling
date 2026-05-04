package com.bkap.aispark.service.teach;

import com.bkap.aispark.dto.teach.AdminTeacherGradeRequest;
import com.bkap.aispark.dto.teach.AdminTeacherGradeResponse;
import com.bkap.aispark.entity.Teacher;
import com.bkap.aispark.entity.teach.TeacherGrade;
import com.bkap.aispark.repository.TeacherRepository;
import com.bkap.aispark.repository.teach.TeacherGradeRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class AdminTeacherGradeService {

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private TeacherGradeRepository teacherGradeRepository;

    public List<AdminTeacherGradeResponse> getTeachersWithGrades() {
        return teacherRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Teacher::getId))
                .map(this::toResponse)
                .toList();
    }

    public AdminTeacherGradeResponse getTeacherGrades(Long teacherId) {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giáo viên"));

        return toResponse(teacher);
    }

    @Transactional
    public AdminTeacherGradeResponse updateTeacherGrades(
            Long teacherId,
            AdminTeacherGradeRequest request
    ) {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giáo viên"));

        List<Integer> grades = request.getGrades() == null
                ? List.of()
                : request.getGrades()
                        .stream()
                        .filter(g -> g != null && g >= 1 && g <= 12)
                        .distinct()
                        .sorted()
                        .toList();

        teacherGradeRepository.deleteByTeacherId(teacherId);
        teacherGradeRepository.flush();

        List<TeacherGrade> newGrades = new ArrayList<>();

        for (Integer grade : grades) {
            TeacherGrade teacherGrade = new TeacherGrade();
            teacherGrade.setTeacher(teacher);
            teacherGrade.setGrade(grade);
            newGrades.add(teacherGrade);
        }

        teacherGradeRepository.saveAll(newGrades);

        return toResponse(teacher);
    }

    private AdminTeacherGradeResponse toResponse(Teacher teacher) {
        AdminTeacherGradeResponse res = new AdminTeacherGradeResponse();

        List<Integer> assignedGrades = teacherGradeRepository.findByTeacherId(teacher.getId())
                .stream()
                .map(TeacherGrade::getGrade)
                .distinct()
                .sorted()
                .toList();

        res.setTeacherId(teacher.getId());
        res.setTeacherName(teacher.getFullName());
        res.setEmail(teacher.getEmail());
        res.setCode(teacher.getCode());
        res.setAssignedGrades(assignedGrades);

        if (teacher.getHomeroomClass() != null) {
            res.setHomeroomClassName(teacher.getHomeroomClass().getName());
        }

        return res;
    }
}