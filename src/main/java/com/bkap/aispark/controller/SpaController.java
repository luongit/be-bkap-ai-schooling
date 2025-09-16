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
    @RequestMapping({ "/", "/{path:[^\\.]*}" })
    public String forwardRoot() {
        return "forward:/index.html";
    }
}
