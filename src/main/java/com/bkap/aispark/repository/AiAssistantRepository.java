package com.bkap.aispark.repository;

import com.bkap.aispark.entity.AiAssistant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AiAssistantRepository extends JpaRepository<AiAssistant, Integer> {
    Optional<AiAssistant> findByPublicSlug(String slug);
    boolean existsByName(String name);
    boolean existsByPublicSlug(String publicSlug);

}
