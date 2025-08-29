package com.bkap.aispark.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bkap.aispark.entity.Schools;

public interface SchoolsRepository extends JpaRepository<Schools, Long> {
}
