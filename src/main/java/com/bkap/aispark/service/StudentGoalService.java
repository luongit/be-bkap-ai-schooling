package com.bkap.aispark.service;

import com.bkap.aispark.entity.Student;
import com.bkap.aispark.entity.StudentGoal;
import com.bkap.aispark.repository.StudentGoalRepository;
import com.bkap.aispark.repository.StudentRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StudentGoalService {

    @Autowired
    private StudentGoalRepository studentGoalRepository;

    @Autowired
    private StudentRepository studentRepository;

    // Lấy danh sách mục tiêu theo studentId
    public List<StudentGoal> getGoalsByStudent(Long studentId) {
        return studentGoalRepository.findByStudentId(studentId);
    }

    // Lấy chi tiết mục tiêu
    public Optional<StudentGoal> getGoalById(Long id) {
        return studentGoalRepository.findById(id);
    }

    // Tạo mới mục tiêu cho học sinh
    @Transactional
    public StudentGoal addGoal(Long studentId, StudentGoal goal) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy học sinh với id: " + studentId));

        goal.setStudent(student);
        return studentGoalRepository.save(goal);
    }

    // Cập nhật mục tiêu
    @Transactional
    public Optional<StudentGoal> updateGoal(Long id, StudentGoal newGoal) {
        return studentGoalRepository.findById(id).map(goal -> {
            goal.setGoal(newGoal.getGoal());
            goal.setStatus(newGoal.getStatus());
            goal.setDeadline(newGoal.getDeadline());
            return studentGoalRepository.save(goal);
        });
    }

    // Xóa mục tiêu
    @Transactional
    public Boolean deleteGoal(Long id) {
        return studentGoalRepository.findById(id).map(goal -> {
            studentGoalRepository.delete(goal);
            return true;
        }).orElse(false);
    }
}
