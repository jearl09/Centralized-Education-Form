package com.example.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = AuthApplication.class)
@ActiveProfiles("test")
public class BackendApplicationTests {

    @Test
    void contextLoads() {
    }
}
