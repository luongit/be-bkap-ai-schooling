package com.bkap.aispark.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SpaController {

    // Cho admin FE
    @RequestMapping({ "/admin", "/admin/{path:[^\\.]*}" })
    public String forwardAdmin() {
        return "forward:/admin/index.html";
    }

    // Cho trang chủ FE
    @RequestMapping({
            "/",
            "/**/{path:[^\\.]*}" // FIX: match route nhiều cấp
    })
    public String forwardClient() {
        return "forward:/index.html"; // React xử lý bằng React Router
    }
}
