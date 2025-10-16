package com.bkap.aispark.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.bkap.aispark.service.PricingVersionService;

@Component
public class PricingVersionScheduler {

    private static final Logger log = LoggerFactory.getLogger(PricingVersionScheduler.class);

    private final PricingVersionService pricingVersionService;

    public PricingVersionScheduler(PricingVersionService pricingVersionService) {
        this.pricingVersionService = pricingVersionService;
    }

    // Chạy mỗi 1 phút: tìm version đến hạn và kích hoạt
    @Scheduled(fixedRate = 60_000)
    public void run() {
        int activated = pricingVersionService.activateDueVersions();
        if (activated > 0) {
            log.info("PricingVersionScheduler: activated {} version(s)", activated);
        }
    }
}
