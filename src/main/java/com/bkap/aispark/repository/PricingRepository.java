package com.bkap.aispark.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bkap.aispark.entity.Pricing;

@Repository
public interface PricingRepository extends JpaRepository<Pricing, Integer> {
    boolean existsByActionCode(String actionCode);
}
