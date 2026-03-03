package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.charset.StandardCharsets;

import org.springframework.web.util.UriUtils;

@Controller
public class HomeController {
    @GetMapping("/")
    public String root() {
        return "redirect:/products";
    }

    @GetMapping("/home/search")
    public String search(@RequestParam(value = "keyword", required = false) String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return "redirect:/products";
        }
        String encoded = UriUtils.encodeQueryParam(keyword, StandardCharsets.UTF_8);
        return "redirect:/products?keyword=" + encoded;
    }
}
