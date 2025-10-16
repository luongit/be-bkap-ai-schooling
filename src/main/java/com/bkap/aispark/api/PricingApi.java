package com.bkap.aispark.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bkap.aispark.entity.Pricing;
import com.bkap.aispark.service.PricingService;

@RestController
@RequestMapping("/api/pricing")
public class PricingApi {

    @Autowired
    private PricingService pricingService;

    @GetMapping
    public ResponseEntity<List<Pricing>> getAllPricing() {
        return ResponseEntity.ok(pricingService.getAllPricing());
    }
}
