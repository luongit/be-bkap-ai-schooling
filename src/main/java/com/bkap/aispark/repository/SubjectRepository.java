package com.bkap.aispark.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bkap.aispark.entity.Subject;

public interface SubjectRepository extends JpaRepository<Subject, Integer> {
}

