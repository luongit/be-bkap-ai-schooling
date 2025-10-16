package com.bkap.aispark.service;

import java.util.List;

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
}
