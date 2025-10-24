package com.bkap.aispark.api;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bkap.aispark.entity.CreditTransaction;
import com.bkap.aispark.service.CreditService;

@RestController
@RequestMapping("/api/credit")
public class CreditApi {

    private final CreditService creditService;

    public CreditApi(CreditService creditService) {
        this.creditService = creditService;
    }

    // 🧾 API trừ credit (test)
    @PostMapping("/deduct")
    public String deduct(@RequestParam Long userId,
            @RequestParam(defaultValue = "1") int amount,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String referenceId) {
        boolean success = creditService.deductCredit(userId, amount, description, referenceId);
        return success ? "Đã trừ " + amount + " credit" : "Không đủ credit để trừ!";
    }

    @PostMapping("/deduct-action")
    public ResponseEntity<?> deductByAction(@RequestBody Map<String, Object> req) {
        Long userId = Long.valueOf(req.get("userId").toString());
        String actionCode = req.get("actionCode").toString();
        String referenceId = (String) req.getOrDefault("referenceId", null);

        boolean success = creditService.deductByAction(userId, actionCode, referenceId);
        if (!success) {
            return ResponseEntity.badRequest().body("Không đủ credit để thực hiện hành động này!");
        }

        int remaining = creditService.getRemainingCredit(userId);
        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "Đã trừ credit cho hành động: " + actionCode,
                "remainingCredit", remaining));
    }

    // 🧾 API xem số dư còn lại
    @GetMapping("/balance/{userId}")
    public int getBalance(@PathVariable Long userId) {
        return creditService.getRemainingCredit(userId);
    }

    // 🧾 API xem lịch sử giao dịch
    @GetMapping("/history/{userId}")
    public List<CreditTransaction> getHistory(@PathVariable Long userId) {
        return creditService.getHistory(userId);
    }
}
