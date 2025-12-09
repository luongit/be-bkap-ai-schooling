package com.bkap.aispark.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.bkap.aispark.entity.Student;



@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByCode(String code);
    Optional<Student> findByEmail(String email);
    boolean existsByCode(String code);
    boolean existsByUsername(String username);
    @Query("SELECT MAX(s.code) FROM Student s WHERE s.code LIKE CONCAT('HS', :year, '%')")
    String findMaxCodeByYear(@Param("year") int year);
}
