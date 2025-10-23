package com.bkap.aispark.api;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bkap.aispark.entity.Pricing;
import com.bkap.aispark.repository.PricingRepository;
import com.bkap.aispark.service.PricingService;

@RestController
@RequestMapping("/api/pricing")
public class PricingApi {

    @Autowired
    private PricingService pricingService;

    @Autowired
    private PricingRepository pricingRepository;

    @GetMapping
    public ResponseEntity<List<Pricing>> getAllPricing() {
        return ResponseEntity.ok(pricingService.getAllPricing());
    }

    // 🔹 Thêm mới Pricing
    @PostMapping
    public Pricing createPricing(@RequestBody Pricing pricing) {
        if (pricingRepository.existsByActionCode(pricing.getActionCode())) {
            throw new IllegalArgumentException("Mã thao tác đã tồn tại!");
        }
        System.out.println("DEBUG => Class: " + pricing.getClass().getName());
        System.out.println("DEBUG => ActionCode: " + pricing.getActionCode());
        System.out.println("DEBUG => ActionName: " + pricing.getActionName());

        if (pricing.getActionName() == null || pricing.getActionName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên thao tác không được để trống!");
        }

        return pricingRepository.save(pricing);
    }

    // 🔹 Xóa Pricing
    @DeleteMapping("/{id}")
    public void deletePricing(@PathVariable Integer id) {
        Optional<Pricing> optional = pricingRepository.findById(id);
        if (optional.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy pricing với id = " + id);
        }

        Pricing pricing = optional.get();

        // Nếu pricing có version liên kết, bạn có thể xử lý logic riêng ở đây
        // (VD: không cho xóa nếu có version đang active)
        pricingRepository.delete(pricing);
    }

}
