package com.bkap.aispark.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bkap.aispark.entity.Pricing;

@Repository
public interface PricingRepository extends JpaRepository<Pricing, Integer> {
    /** ✅ Kiểm tra trùng mã thao tác */
    boolean existsByActionCode(String actionCode);

    /** ✅ Tìm theo actionCode để trừ credit */
    Optional<Pricing> findByActionCode(String actionCode);
}
