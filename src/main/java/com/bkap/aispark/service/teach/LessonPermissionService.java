package com.bkap.aispark.service.teach;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.bkap.aispark.entity.Teacher;
import com.bkap.aispark.entity.teach.Lesson;
import com.bkap.aispark.entity.teach.LessonPermission;
import com.bkap.aispark.entity.teach.TeacherGrade;
import com.bkap.aispark.repository.TeacherRepository;
import com.bkap.aispark.repository.teach.LessonPermissionRepository;
import com.bkap.aispark.repository.teach.LessonRepository;
import com.bkap.aispark.repository.teach.TeacherGradeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LessonPermissionService {

    private final LessonRepository lessonRepository;
    private final TeacherRepository teacherRepository;
    private final TeacherGradeRepository teacherGradeRepository;
    private final LessonPermissionRepository permissionRepository;

    /**
     * Admin phân quyền xem bài
     */
    public void grantViewPermission(Long lessonId, Long teacherId) {

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));

        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        Integer lessonGrade = lesson.getGrade();

        boolean hasGrade = teacherGradeRepository
                .existsByTeacherIdAndGrade(teacherId, lessonGrade);

        if (!hasGrade) {
            TeacherGrade tg = new TeacherGrade();
            tg.setTeacher(teacher);
            tg.setGrade(lessonGrade);
            tg.setAssignedAt(LocalDateTime.now());
            teacherGradeRepository.save(tg);
        }

        // 🔹 2. Cấp quyền lesson
        LessonPermission permission = permissionRepository
                .findByLessonIdAndTeacherId(lessonId, teacherId)
                .orElse(new LessonPermission());

        permission.setLesson(lesson);
        permission.setTeacher(teacher);
        permission.setCanView(true);
        permission.setAssignedAt(LocalDateTime.now());

        permissionRepository.save(permission);
    }

    /**
     * Thu quyền xem bài
     */
    public void revokeViewPermission(Long lessonId, Long teacherId) {
        permissionRepository.findByLessonIdAndTeacherId(lessonId, teacherId)
                .ifPresent(permissionRepository::delete);
    }

    /**
     * Giáo viên lấy danh sách bài được xem
     */
    public List<Lesson> getLessonsForTeacher(Long teacherId) {
        return permissionRepository.findLessonsForTeacher(teacherId);
    }
}