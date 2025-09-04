package com.bkap.aispark.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bkap.aispark.entity.Student;



@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByCode(String code);

    boolean existsByCode(String code);
    boolean existsByUsername(String username);
}
