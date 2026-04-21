package com.pokectfree.login.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/api/test")
    public String hello() {
        return "🎉 스프링 부트 백엔드와 연결 성공! 가계부 앱 화이팅!";
    }
}