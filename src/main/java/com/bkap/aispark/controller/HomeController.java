package com.bkap.aispark.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
	@GetMapping({"","/"})
	public String home() {
		return "index";
	}
	@GetMapping({"/excel"})
	public String excel() {
		return "test_excel";
	}
}
