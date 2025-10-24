package com.bkap.aispark.api;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.bkap.aispark.entity.CreditTransaction;
import com.bkap.aispark.service.CreditService;

@RestController
@RequestMapping("/api/credit")
public class CreditApi {

    private final CreditService creditService;

    public CreditApi(CreditService creditService) {
        this.creditService = creditService;
    }

    /** 🧾 API trừ credit cơ bản (test) */
    @PostMapping("/deduct")
    public ResponseEntity<?> deduct(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") int amount,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String referenceId) {

        boolean success = creditService.deductCredit(userId, amount, description, referenceId);
        if (!success) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "FAILED",
                    "message", "Không đủ credit để trừ!"));
        }

        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "Đã trừ " + amount + " credit.",
                "remainingCredit", creditService.getRemainingCredit(userId)));
    }

    /** 💡 API trừ credit theo hành động định giá (pricing.actionCode) */
    @PostMapping("/deduct-action")
    public ResponseEntity<?> deductByAction(@RequestBody Map<String, Object> req) {
        try {
            Long userId = Long.valueOf(req.get("userId").toString());
            String actionCode = req.get("actionCode").toString();
            String referenceId = (String) req.getOrDefault("referenceId", null);
            Integer totalTokens = req.containsKey("totalTokens") 
                    ? Integer.parseInt(req.get("totalTokens").toString()) : null;

            boolean success;

            // ⚙️ Nếu có token usage (ví dụ chat stream) thì dùng hàm tính theo token
            if (totalTokens != null && totalTokens > 0) {
                success = creditService.deductByTokenUsage(userId, actionCode, totalTokens, referenceId);
            } else {
                success = creditService.deductByAction(userId, actionCode, referenceId);
            }

            if (!success) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "FAILED",
                        "message", "Không đủ credit để thực hiện hành động này!"));
            }

            int remaining = creditService.getRemainingCredit(userId);
            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "message", "Đã trừ credit cho hành động: " + actionCode,
                    "remainingCredit", remaining));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "ERROR",
                    "message", e.getMessage()));
        }
    }

    /** 🧾 API cộng credit (mua gói / thưởng / hoàn tiền) */
    @PostMapping("/add")
    public ResponseEntity<?> addCredit(@RequestBody Map<String, Object> req) {
        try {
            Long userId = Long.valueOf(req.get("userId").toString());
            int amount = Integer.parseInt(req.get("amount").toString());
            String type = (String) req.getOrDefault("type", "purchase");
            String description = (String) req.getOrDefault("description", "Nạp credit");
            String referenceId = (String) req.getOrDefault("referenceId", null);

            creditService.addCredit(userId, amount, type, description, referenceId);

            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "message", "Đã cộng " + amount + " credit cho user " + userId,
                    "remainingCredit", creditService.getRemainingCredit(userId)));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "ERROR",
                    "message", e.getMessage()));
        }
    }

    /** 💳 API xem số dư còn lại */
    @GetMapping("/balance/{userId}")
    public ResponseEntity<?> getBalance(@PathVariable Long userId) {
        return ResponseEntity.ok(Map.of(
                "userId", userId,
                "balance", creditService.getRemainingCredit(userId)));
    }

    /** 📜 API xem lịch sử giao dịch */
    @GetMapping("/history/{userId}")
    public ResponseEntity<?> getHistory(@PathVariable Long userId) {
        List<CreditTransaction> list = creditService.getHistory(userId);
        return ResponseEntity.ok(list);
    }
}
