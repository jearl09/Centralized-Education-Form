// Location: src/main/java/com/example/backend/AuthApplication.java
package com.example.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
        System.out.println("🚀 Auth Application started successfully!");
        System.out.println("📊 Server running on: http://localhost:8080");
        System.out.println("🔗 API endpoints available at: http://localhost:8080/api/auth/");
        System.out.println("🌐 Frontend should be running on: http://localhost:5173");
    }
}