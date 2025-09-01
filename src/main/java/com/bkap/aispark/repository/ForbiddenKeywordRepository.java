package com.bkap.aispark.repository;

import com.bkap.aispark.entity.ForbiddenKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ForbiddenKeywordRepository extends JpaRepository<ForbiddenKeyword, Long> {
}