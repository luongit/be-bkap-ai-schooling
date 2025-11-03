package com.bkap.aispark.api;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bkap.aispark.entity.Exercise;
import com.bkap.aispark.entity.StudentExercise;
import com.bkap.aispark.entity.Subject;
import com.bkap.aispark.service.TutorService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/tutor")
public class TutorApi {

    @Autowired
    private TutorService tutorService;

    @GetMapping("/subjects")
    public List<Subject> getSubjects() {
        return tutorService.getSubjects();
    }

    @GetMapping("/exercises")
    public List<Exercise> getExercises(@RequestParam Integer subjectId) {
        return tutorService.getExercisesBySubject(subjectId);
    }

    @PostMapping("/submit")
    public ResponseEntity<?> submitExercise(
            @RequestParam Long studentId,
            @RequestParam Integer exerciseId,
            @RequestBody Map<String, Object> body) {

        try {
            String answer = new ObjectMapper().writeValueAsString(body.get("answer"));
            StudentExercise result = tutorService.submitExercise(studentId, exerciseId, answer);
            return ResponseEntity.ok(result);
        } catch (JsonProcessingException e) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", "Invalid answer format", "details", e.getMessage()));
        }

    }
}
