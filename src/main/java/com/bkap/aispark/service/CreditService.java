package com.bkap.aispark.service;

import com.bkap.aispark.entity.UserCredit;
import com.bkap.aispark.entity.CreditTransaction;
import com.bkap.aispark.repository.UserCreditRepository;
import com.bkap.aispark.repository.CreditTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class CreditService {

    private final UserCreditRepository creditRepo;
    private final CreditTransactionRepository transactionRepo;

    public CreditService(UserCreditRepository creditRepo, CreditTransactionRepository transactionRepo) {
        this.creditRepo = creditRepo;
        this.transactionRepo = transactionRepo;
    }

    /** ✅ Overload cho AiStreamController - mặc định trừ 1 credit */
    @Transactional
    public boolean deductCredit(Long userId) {
        return deductCredit(userId, 1, "Chat AI", "chat-session");
    }

    /** ✅ Dùng cho mọi thao tác trừ credit (tạo ảnh, video, v.v.) */
    @Transactional
    public boolean deductCredit(Long userId, int amount, String description, String referenceId) {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive");

        UserCredit credit = creditRepo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User credit not found"));

        if (credit.getCredit() < amount) {
            return false; // không đủ credit
        }

        int newBalance = credit.getCredit() - amount;
        credit.setCredit(newBalance);
        creditRepo.save(credit);

        CreditTransaction tx = new CreditTransaction();
        tx.setUserId(userId);
        tx.setType("debit");
        tx.setAmount(-amount); // ✅ ghi âm để ledger cộng dồn đúng
        tx.setBalanceAfter(newBalance);
        tx.setDescription(description);
        tx.setReferenceId(referenceId);
        tx.setCreatedAt(LocalDateTime.now());
        transactionRepo.save(tx);

        return true;
    }

    /** ✅ Khi user mua gói, hoặc hệ thống cộng thêm credit */
    @Transactional
    public void addCredit(Long userId, int amount, String type, String description, String referenceId) {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive");
        if (type == null) type = "credit"; // fallback an toàn

        UserCredit credit = creditRepo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User credit not found"));

        int newBalance = credit.getCredit() + amount;
        credit.setCredit(newBalance);
        creditRepo.save(credit);

        CreditTransaction tx = new CreditTransaction();
        tx.setUserId(userId);
        tx.setType(type); // "purchase" | "credit" | "refund"
        tx.setAmount(amount); // ✅ dương
        tx.setBalanceAfter(newBalance);
        tx.setDescription(description);
        tx.setReferenceId(referenceId);
        tx.setCreatedAt(LocalDateTime.now());
        transactionRepo.save(tx);
    }

    /** ✅ Lấy số dư hiện tại */
    public int getRemainingCredit(Long userId) {
        return creditRepo.findByUserId(userId)
                .map(UserCredit::getCredit)
                .orElse(0);
    }

    /** ✅ Lịch sử giao dịch (mới nhất trước) */
    public List<CreditTransaction> getHistory(Long userId) {
        return transactionRepo.findByUserIdOrderByCreatedAtDesc(userId)
                .orElse(Collections.emptyList());
    }
}
