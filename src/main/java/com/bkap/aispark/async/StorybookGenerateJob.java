package com.bkap.aispark.async;

import com.bkap.aispark.service.StorybookGenerateService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StorybookGenerateJob {

    private final StorybookGenerateService generateService;

    @Async
    public void generateAsync(Long storybookId) {
        generateService.generate(storybookId);
    }
}
