package com.bkap.aispark.async;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.bkap.aispark.service.Storybook.StorybookGenerateService;

@Component
@RequiredArgsConstructor
public class StorybookGenerateJob {

    private final StorybookGenerateService generateService;

    @Async
    public void generateAsync(Long storybookId) {
        generateService.generate(storybookId);
    }
}
