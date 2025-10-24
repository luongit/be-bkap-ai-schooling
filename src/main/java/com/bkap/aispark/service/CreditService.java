package com.bkap.aispark.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bkap.aispark.entity.CreditLog;
import com.bkap.aispark.entity.CreditTransaction;
import com.bkap.aispark.entity.Pricing;
import com.bkap.aispark.entity.UserCredit;
import com.bkap.aispark.repository.CreditLogRepository;
import com.bkap.aispark.repository.CreditTransactionRepository;
import com.bkap.aispark.repository.PricingRepository;
import com.bkap.aispark.repository.UserCreditRepository;

@Service
public class CreditService {

    private final UserCreditRepository creditRepo;
    private final CreditTransactionRepository transactionRepo;
    private final PricingRepository pricingRepo;
    private final CreditLogRepository creditLogRepo;

    public CreditService(
            UserCreditRepository creditRepo,
            CreditTransactionRepository transactionRepo,
            PricingRepository pricingRepo,
            CreditLogRepository creditLogRepo) {
        this.creditRepo = creditRepo;
        this.transactionRepo = transactionRepo;
        this.pricingRepo = pricingRepo;
        this.creditLogRepo = creditLogRepo;
    }

    /** ✅ Overload cho AiStreamController - mặc định trừ 1 credit */
    @Transactional
    public boolean deductCredit(Long userId) {
        return deductCredit(userId, 1, "Chat AI", "chat-session");
    }

    /** ✅ Dùng cho mọi thao tác trừ credit (tạo ảnh, video, v.v.) */
    @Transactional
    public boolean deductCredit(Long userId, int amount, String description, String referenceId) {
        if (amount <= 0)
            throw new IllegalArgumentException("Amount must be positive");

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

    @Transactional
    public boolean deductByAction(Long userId, String actionCode, String referenceId) {
        Pricing pricing = pricingRepo.findByActionCode(actionCode)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bảng giá cho hành động: " + actionCode));

        int creditCost = pricing.getCreditCost();
        int tokenCost = pricing.getTokenCost();

        // Nếu giá trị là 0 thì không trừ
        if (creditCost <= 0 && tokenCost <= 0)
            return true;

        UserCredit credit = creditRepo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản credit cho user ID: " + userId));

        if (credit.getCredit() < creditCost) {
            return false; // không đủ credit
        }

        // Cập nhật số dư
        int newBalance = credit.getCredit() - creditCost;
        credit.setCredit(newBalance);
        creditRepo.save(credit);

        // Ghi CreditTransaction
        CreditTransaction tx = new CreditTransaction();
        tx.setUserId(userId);
        tx.setType("debit");
        tx.setAmount(-creditCost);
        tx.setBalanceAfter(newBalance);
        tx.setDescription("Sử dụng chức năng: " + pricing.getActionName());
        tx.setReferenceId(referenceId);
        tx.setCreatedAt(LocalDateTime.now());
        transactionRepo.save(tx);

        // Ghi CreditLog (cho analytics / admin)
        CreditLog log = new CreditLog();
        log.setUserId(userId);
        log.setPricingId(pricing.getId());
        log.setCreditUsed(creditCost);
        log.setTokenUsed(tokenCost);
        log.setCreatedAt(LocalDateTime.now());
        creditLogRepo.save(log);

        return true;
    }

    /** ✅ Khi user mua gói, hoặc hệ thống cộng thêm credit */
    @Transactional
    public void addCredit(Long userId, int amount, String type, String description, String referenceId) {
        if (amount <= 0)
            throw new IllegalArgumentException("Amount must be positive");
        if (type == null)
            type = "credit"; // fallback an toàn

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

    @Transactional
    public boolean deductByTokenUsage(Long userId, String actionCode, int totalTokens, String referenceId) {
        System.out.printf("🔍 [deductByTokenUsage] user=%d | action=%s | tokens=%d | ref=%s%n",
                userId, actionCode, totalTokens, referenceId);

        Pricing pricing = pricingRepo.findByActionCode(actionCode)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy actionCode: " + actionCode));

        int tokenCost = pricing.getTokenCost(); // 500
        int creditCost = pricing.getCreditCost(); // 2

        // 1️⃣ Tính tỷ lệ: 500 token = 2 credit → 1 credit = 250 token
        double tokensPerCredit = (double) tokenCost / creditCost;

        // 2️⃣ Tính số credit cần trừ (làm tròn lên)
        int creditToDeduct = (int) Math.ceil(totalTokens / tokensPerCredit);

        UserCredit credit = creditRepo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy credit user"));

        if (credit.getCredit() < creditToDeduct) {
            return false;
        }

        int newBalance = credit.getCredit() - creditToDeduct;
        credit.setCredit(newBalance);
        creditRepo.save(credit);

        // 🔹 Ghi log chi tiết vào credit_log
        CreditLog log = new CreditLog();
        log.setUserId(userId);
        log.setPricingId(pricing.getId());
        log.setCreditUsed(creditToDeduct);
        log.setTokenUsed(totalTokens);
        log.setReferenceId(referenceId);
        log.setCreatedAt(LocalDateTime.now());
        creditLogRepo.save(log);

        // 🔹 Ghi transaction để frontend hiển thị
        CreditTransaction tx = new CreditTransaction();
        tx.setUserId(userId);
        tx.setType("debit");
        tx.setAmount(-creditToDeduct);
        tx.setBalanceAfter(newBalance);
        tx.setDescription("Sử dụng chức năng: " + pricing.getActionName());
        tx.setReferenceId(referenceId);
        tx.setCreatedAt(LocalDateTime.now());
        transactionRepo.save(tx);

        return true;
    }


}
