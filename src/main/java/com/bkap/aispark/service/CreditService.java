package com.bkap.aispark.service;

import com.bkap.aispark.entity.UserCredit;
import com.bkap.aispark.repository.UserCreditRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreditService {

    private final UserCreditRepository creditRepo;

    public CreditService(UserCreditRepository creditRepo) {
        this.creditRepo = creditRepo;
    }

    @Transactional
    public boolean deductCredit(Long userId) {
        UserCredit credit = creditRepo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User credit not found"));

        if (credit.getCredit() <= 0) {
            return false;
        }

        credit.setCredit(credit.getCredit() - 1);
        creditRepo.save(credit);
        return true;
    }

    public int getRemainingCredit(Long userId) {
        return creditRepo.findByUserId(userId)
                .map(UserCredit::getCredit)
                .orElse(0);
    }
}
