package com.bkap.aispark.repository.teach;

import com.bkap.aispark.entity.teach.TeacherGrade;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TeacherGradeRepository extends JpaRepository<TeacherGrade, Long> {
    List<TeacherGrade> findByTeacherId(Long teacherId);

    void deleteByTeacherIdAndGrade(Long teacherId, Integer grade);

    boolean existsByTeacherIdAndGrade(Long teacherId, Integer grade);
 // Tìm danh sách khối mà giáo viên phụ trách

}