package com.bkap.aispark.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bkap.aispark.entity.Parent;

public interface ParentRepository extends JpaRepository<Parent, Long> {
    boolean existsByCode(String code);
}
