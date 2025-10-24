package com.bkap.aispark.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bkap.aispark.dto.CreatePricingVersionRequest;
import com.bkap.aispark.dto.PricingVersionResponse;
import com.bkap.aispark.entity.Pricing;
import com.bkap.aispark.entity.PricingVersion;
import com.bkap.aispark.repository.PricingRepository;
import com.bkap.aispark.repository.PricingVersionRepository;

@Service
public class PricingVersionService {

    @Autowired
    private PricingRepository pricingRepository;

    @Autowired
    private PricingVersionRepository pricingVersionRepository;

    // TODO: Bạn có thể inject AuditLogService và NotificationService nếu đã có
    // @Autowired private AuditLogService auditLogService;
    // @Autowired private NotificationService notificationService;

    @Transactional
    @CacheEvict(value = { "pricing", "pricing_version" }, allEntries = true)
    public PricingVersionResponse createVersion(CreatePricingVersionRequest req, Long adminUserId) {
        Pricing pricing = pricingRepository.findById(req.getPricingId())
                .orElseThrow(() -> new IllegalArgumentException("pricingId không tồn tại"));

        if (req.getCreditCost() < 0 || req.getTokenCost() < 0 || req.getVndCost() < 0) {
            throw new IllegalArgumentException("Giá trị chi phí phải >= 0");
        }

        PricingVersion pv = new PricingVersion();
        pv.setPricing(pricing);
        pv.setTokenCost(req.getTokenCost());
        pv.setCreditCost(req.getCreditCost());
        pv.setVndCost(req.getVndCost());
        pv.setEffectiveFrom(req.getEffectiveFrom() != null ? req.getEffectiveFrom() : LocalDateTime.now());
        pv.setActive(false);
        pv.setCreatedBy(adminUserId);
        pv.setCreatedAt(LocalDateTime.now());

        PricingVersion saved = pricingVersionRepository.save(pv);

        // Audit (nếu có)
        // auditLogService.record(adminUserId, "CREATE", "pricing_version",
        // saved.getId(), ...);

        // Optionally notify
        // if (req.isNotifyUsers()) notificationService.broadcast("Pricing sẽ thay đổi
        // từ " + saved.getEffectiveFrom());

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<PricingVersionResponse> listVersions(Integer pricingId) {
        return pricingVersionRepository.findByPricingIdOrderByEffectiveFromDesc(pricingId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = { "pricing", "pricing_version" }, allEntries = true)
    public PricingVersionResponse activateNow(Integer versionId, Long adminUserId) {
        PricingVersion pv = pricingVersionRepository.findById(versionId)
                .orElseThrow(() -> new IllegalArgumentException("Version không tồn tại"));

        // Bật active version này
        pv.setActive(true);
        PricingVersion saved = pricingVersionRepository.save(pv);

        // Vô hiệu hoá các version khác cùng pricing
        pricingVersionRepository.deactivateOthers(pv.getPricing().getId(), saved.getId());

        // Đồng bộ giá hiện tại vào bảng pricing chính (nếu bạn muốn đọc từ pricing)
        updatePricingFromVersion(saved);

        // Audit
        // auditLogService.record(adminUserId, "ACTIVATE", "pricing_version",
        // saved.getId(), ...);

        return toResponse(saved);
    }

    @Transactional
    @CacheEvict(value = { "pricing", "pricing_version" }, allEntries = true)
    public int activateDueVersions() {
        List<PricingVersion> due = pricingVersionRepository
                .findAllDueToActivate(LocalDateTime.now())
                .stream()
                // ⚙️ chỉ kích hoạt version nếu tất cả version khác đều có effective_from <
                // version.effective_from
                // hoặc nếu chưa có version nào active mới hơn
                .filter(v -> {
                    PricingVersion newest = pricingVersionRepository
                            .findNewestActiveByPricingId(v.getPricing().getId());
                    return newest == null || newest.getEffectiveFrom().isBefore(v.getEffectiveFrom());
                })
                .collect(Collectors.toList());
        int activated = 0;

        for (PricingVersion pv : due) {
            // Activate
            pv.setActive(true);
            PricingVersion saved = pricingVersionRepository.save(pv);

            // Deactivate others
            pricingVersionRepository.deactivateOthers(saved.getPricing().getId(), saved.getId());

            // Sync current pricing
            updatePricingFromVersion(saved);

            activated++;
        }
        return activated;
    }

    private void updatePricingFromVersion(PricingVersion version) {
        Pricing pricing = version.getPricing();
        pricing.setTokenCost(version.getTokenCost());
        pricing.setCreditCost(version.getCreditCost());
        pricing.setVndCost(version.getVndCost());
        pricingRepository.save(pricing);
    }

    private PricingVersionResponse toResponse(PricingVersion pv) {
        PricingVersionResponse dto = new PricingVersionResponse();
        dto.setId(pv.getId());
        dto.setPricingId(pv.getPricing().getId());
        dto.setActionCode(pv.getPricing().getActionCode());
        dto.setActionName(pv.getPricing().getActionName());
        dto.setTokenCost(pv.getTokenCost());
        dto.setCreditCost(pv.getCreditCost());
        dto.setVndCost(pv.getVndCost());
        dto.setEffectiveFrom(pv.getEffectiveFrom());
        dto.setActive(pv.isActive());
        dto.setCreatedBy(pv.getCreatedBy());
        dto.setCreatedAt(pv.getCreatedAt());
        return dto;
    }
}
