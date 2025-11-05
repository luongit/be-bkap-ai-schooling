package com.bkap.aispark.controller;

import com.bkap.aispark.service.Json2VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/video")
public class Json2VideoController {

    @Autowired
    private Json2VideoService json2VideoService;

    //  Tạo video từ ảnh + phụ đề
    @PostMapping("/create")
    public String createVideo(@RequestBody Map<String, String> body) {
        String imageUrl = body.get("imageUrl");
        String subtitle = body.get("subtitle");

        if (imageUrl == null || subtitle == null) {
            return "{\"error\":\"Thiếu trường imageUrl hoặc subtitle trong JSON body\"}";
        }

        return json2VideoService.renderImageWithSubtitle(imageUrl, subtitle);
    }
}
