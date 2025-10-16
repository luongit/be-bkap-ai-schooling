package com.bkap.aispark.api;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bkap.aispark.dto.CreatePricingVersionRequest;
import com.bkap.aispark.dto.PricingVersionResponse;
import com.bkap.aispark.service.PricingVersionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/pricing-version")
public class PricingVersionApi {

    @Autowired
    private PricingVersionService pricingVersionService;

    // Tạo version mới (có thể set effective_from trong tương lai)
    @PostMapping
    public ResponseEntity<PricingVersionResponse> create(@Valid @RequestBody CreatePricingVersionRequest req,
            Principal principal) {
        // lấy adminUserId từ principal nếu bạn có SecurityContext; tạm thời mock = null
        Long adminUserId = null;
        PricingVersionResponse res = pricingVersionService.createVersion(req, adminUserId);
        return ResponseEntity.ok(res);
    }

    // Danh sách version của 1 pricing
    @GetMapping("/{pricingId}")
    public ResponseEntity<List<PricingVersionResponse>> list(@PathVariable Integer pricingId) {
        return ResponseEntity.ok(pricingVersionService.listVersions(pricingId));
    }

    // Kích hoạt ngay 1 version (bất kể effective_from)
    @PostMapping("/{versionId}/activate")
    public ResponseEntity<PricingVersionResponse> activate(@PathVariable Integer versionId,
            Principal principal) {
        Long adminUserId = null;
        PricingVersionResponse res = pricingVersionService.activateNow(versionId, adminUserId);
        return ResponseEntity.ok(res);
    }
}
