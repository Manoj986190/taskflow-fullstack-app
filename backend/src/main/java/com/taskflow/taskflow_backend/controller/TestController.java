package com.taskflow.taskflow_backend.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/secure")
    public Map<String, String> securedEndpoint() {
        return Map.of("message", "Protected endpoint accessed successfully!");
    }

    @GetMapping("/simple")
    public String test() {
        return "Protected endpoint works!";
    }
}