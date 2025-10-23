package com.bkap.aispark.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bkap.aispark.entity.Pricing;
import com.bkap.aispark.repository.PricingRepository;

@Service
public class PricingService {
    @Autowired
    private PricingRepository pricingRepository;

    public List<Pricing> getAllPricing() {
        return pricingRepository.findAll();
    }

    // 🔹 Thêm mới Pricing
    public Pricing createPricing(Pricing pricing) {
        if (pricingRepository.existsByActionCode(pricing.getActionCode())) {
            throw new IllegalArgumentException("Mã thao tác đã tồn tại!");
        }

        if (pricing.getActionName() == null || pricing.getActionName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên thao tác không được để trống!");
        }

        return pricingRepository.save(pricing);
    }

    // 🔹 Xóa Pricing
    public void deletePricing(Integer id) {
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
