package com.bkap.aispark.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.bkap.aispark.dto.TeacherDTO;
import com.bkap.aispark.entity.Teacher;;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    Optional<Teacher> findByEmail(String email);

    @Query("SELECT new com.bkap.aispark.dto.TeacherDTO(" +
            "t.id, t.fullName, t.email, t.phone, t.code, t.isActive, t.createdAt, " +
            "c.name, s.name) " +
            "FROM Teacher t " +
            "LEFT JOIN t.homeroomClass c " +
            "LEFT JOIN c.school s")
    List<TeacherDTO> findAllWithClassAndSchool();

}
