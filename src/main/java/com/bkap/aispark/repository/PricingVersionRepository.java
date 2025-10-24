package com.bkap.aispark.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.bkap.aispark.entity.Pricing;
import com.bkap.aispark.entity.PricingVersion;

@Repository
public interface PricingVersionRepository extends JpaRepository<PricingVersion, Integer> {

    List<PricingVersion> findByPricingOrderByEffectiveFromDesc(Pricing pricing);

    List<PricingVersion> findByPricingIdOrderByEffectiveFromDesc(Integer pricingId);

    Optional<PricingVersion> findFirstByPricingIdAndActiveTrueOrderByEffectiveFromDesc(Integer pricingId);

    @Query("SELECT pv FROM PricingVersion pv " +
            "WHERE pv.active = false AND pv.effectiveFrom <= :asOf")
    List<PricingVersion> findAllDueToActivate(LocalDateTime asOf);

    @Modifying
    @Query("UPDATE PricingVersion pv SET pv.active = false WHERE pv.pricing.id = :pricingId AND pv.id <> :activeId")
    int deactivateOthers(Integer pricingId, Integer activeId);

    @Query("SELECT v FROM PricingVersion v WHERE v.pricing.id = :pricingId AND v.active = true ORDER BY v.effectiveFrom DESC LIMIT 1")
    PricingVersion findNewestActiveByPricingId(@Param("pricingId") Integer pricingId);

}
