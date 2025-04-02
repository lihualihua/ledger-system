package com.digital.boot.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IndexController {

    /**
     * 首页
     *
     * @return
     */
    @GetMapping("/")
    public String index() {
        return "index.html";
    }
}
