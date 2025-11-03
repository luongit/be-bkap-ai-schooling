package com.bkap.aispark.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bkap.aispark.entity.Exercise;
import com.bkap.aispark.entity.Student;
import com.bkap.aispark.entity.StudentExercise;
import com.bkap.aispark.entity.Subject;
import com.bkap.aispark.repository.ExerciseRepository;
import com.bkap.aispark.repository.StudentExerciseRepository;
import com.bkap.aispark.repository.StudentRepository;
import com.bkap.aispark.repository.SubjectRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

@Service
public class TutorService {

    @Autowired
    private SubjectRepository subjectRepo;
    @Autowired
    private ExerciseRepository exerciseRepo;
    @Autowired
    private StudentExerciseRepository studentExerciseRepo;
    @Autowired
    private AiService aiService; // để chấm bài hoặc tạo bài tập
    @Autowired
    private StudentRepository studentRepo;

    // Lấy danh sách môn học
    public List<Subject> getSubjects() {
        return subjectRepo.findAll();
    }

    // Lấy danh sách bài tập của 1 môn
    public List<Exercise> getExercisesBySubject(Integer subjectId) {
        return exerciseRepo.findBySubjectId(subjectId);
    }

    // Học sinh nộp bài → gọi AI chấm
    public StudentExercise submitExercise(Long studentId, Integer exerciseId, String answerJson) {
        Exercise exercise = exerciseRepo.findById(exerciseId).orElseThrow();
        Student student = studentRepo.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        StudentExercise se = new StudentExercise();
        se.setStudent(student);
        se.setExercise(exercise);

        // ✅ Parse JSON answer thành Map
        Map<String, Object> answerMap = new HashMap<>();
        try {
            answerMap = new ObjectMapper().readValue(
                    answerJson,
                    new TypeReference<Map<String, Object>>() {
                    });
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Invalid answer JSON: " + e.getMessage());
        }
        se.setAnswer(answerMap);

        // ✅ Gọi AI chấm điểm
        Map<String, Object> result = aiService.gradeExercise(exercise.getContent(), answerJson);

        Number scoreNum = (Number) result.get("score");
        Double score = scoreNum != null ? scoreNum.doubleValue() : 0.0;

        String feedback = result.get("feedback") != null
                ? result.get("feedback").toString()
                : "Không có phản hồi";

        se.setScore(score);
        se.setFeedback(feedback);

        return studentExerciseRepo.save(se);
    }
}
