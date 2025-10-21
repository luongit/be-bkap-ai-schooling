package com.bkap.aispark.api;

import com.bkap.aispark.entity.CreditTransaction;
import com.bkap.aispark.service.CreditService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

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
