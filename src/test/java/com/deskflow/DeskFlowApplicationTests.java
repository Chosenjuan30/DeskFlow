package com.deskflow;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test — verifies the Spring context loads without errors.
 * Uses Testcontainers for real Postgres + Redis + Kafka in CI.
 */
@SpringBootTest
@ActiveProfiles("test")
class DeskFlowApplicationTests {

    @Test
    void contextLoads() {
        // If this passes the entire Spring application context boots successfully
    }
}