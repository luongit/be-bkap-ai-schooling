package com.bkap.aispark.repository;

import com.bkap.aispark.entity.CreditTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CreditTransactionRepository extends JpaRepository<CreditTransaction, Long> {
//    List<CreditTransaction> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<List<CreditTransaction>> findByUserIdOrderByCreatedAtDesc(Long userId);
}
