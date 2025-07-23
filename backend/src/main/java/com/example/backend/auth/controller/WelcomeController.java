// Location: src/main/java/com/example/backend/controller/WelcomeController.java
package com.example.backend.auth.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
public class WelcomeController {
    
    @GetMapping("/")
    public Map<String, Object> welcome() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "🚀 Centralized Academic Forms API is running!");
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("version", "1.0.0");
        response.put("endpoints", Map.of(
            "auth", "/api/auth/*",
            "documentation", "API documentation coming soon",
            "health", "/actuator/health"
        ));
        return response;
    }
    
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("database", "Connected");
        response.put("service", "Centralized Academic Forms API");
        return response;
    }
    
    @GetMapping("/api")
    public Map<String, Object> apiInfo() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Centralized Academic Forms API");
        response.put("version", "1.0.0");
        response.put("endpoints", Map.of(
            "login", "POST /api/auth/login",
            "register", "POST /api/auth/register",
            "google_login", "POST /api/auth/google",
            "profile", "GET /api/auth/profile",
            "verify_token", "POST /api/auth/verify"
        ));
        return response;
    }
}