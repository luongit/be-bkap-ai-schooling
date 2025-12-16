package com.bkap.aispark.repository;

import com.bkap.aispark.entity.AiAssistant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AiAssistantRepository extends JpaRepository<AiAssistant, Integer> {
    Optional<AiAssistant> findByPublicSlug(String slug);
    boolean existsByName(String name);
    boolean existsByPublicSlug(String publicSlug);
    
    @Modifying
    @Query("update AiAssistant a set a.views = a.views + 1 where a.id = :id")
    void incrementView(@Param("id") Integer id);


}
