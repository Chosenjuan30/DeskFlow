package com.deskflow.module.ticket.web;

import com.deskflow.module.auth.dto.LoginRequest;
import com.deskflow.module.auth.dto.RegisterRequest;
import com.deskflow.module.ticket.domain.TicketStatus;
import com.deskflow.module.ticket.dto.CreateTicketRequest;
import com.deskflow.module.ticket.dto.UpdateStatusRequest;
import com.deskflow.module.user.domain.User;
import com.deskflow.module.user.domain.UserRole;
import com.deskflow.module.user.repository.UserRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@EmbeddedKafka(partitions = 1)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TicketControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("deskflow_test")
            .withUsername("test")
            .withPassword("test");

    @SuppressWarnings("resource")
    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url",      postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
        r.add("spring.data.redis.host",     redis::getHost);
        r.add("spring.data.redis.port",     () -> redis.getMappedPort(6379));
    }

    @LocalServerPort int port;

    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;

    static String customerToken;
    static String agentToken;
    static String ticketId;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "";
    }

    @Test @Order(1)
    void setup_registerCustomerAndCreateAgent() {
        // Register customer
        customerToken = given()
            .contentType(ContentType.JSON)
            .body(new RegisterRequest("customer@tickets.com", "password123", "Jane", "Customer"))
            .post("/api/v1/auth/register")
            .jsonPath().getString("accessToken");

        // Create agent directly in DB (no agent-registration endpoint yet)
        User agent = new User();
        agent.setEmail("agent@tickets.com");
        agent.setPasswordHash(passwordEncoder.encode("password123"));
        agent.setFirstName("John");
        agent.setLastName("Agent");
        agent.setRole(UserRole.SUPPORT_AGENT);
        agent.setActive(true);
        userRepository.save(agent);

        agentToken = given()
            .contentType(ContentType.JSON)
            .body(new LoginRequest("agent@tickets.com", "password123"))
            .post("/api/v1/auth/login")
            .jsonPath().getString("accessToken");

        assertThat(customerToken).isNotNull();
        assertThat(agentToken).isNotNull();
    }

    @Test @Order(2)
    void createTicket_asCustomer_returns201WithOpenStatus() {
        ticketId = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + customerToken)
            .body(new CreateTicketRequest("Login broken", "Cannot access my account",
                    com.deskflow.module.ticket.domain.TicketPriority.HIGH,
                    com.deskflow.module.ticket.domain.TicketCategory.ACCOUNT))
        .when()
            .post("/api/v1/tickets")
        .then()
            .statusCode(201)
            .body("status", equalTo("OPEN"))
            .body("referenceNumber", startsWith("TKT-"))
            .body("priority", equalTo("HIGH"))
            .extract().jsonPath().getString("id");

        assertThat(ticketId).isNotNull();
    }

    @Test @Order(3)
    void createTicket_asAgent_returns403() {
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + agentToken)
            .body(new CreateTicketRequest("Test", "Test", com.deskflow.module.ticket.domain.TicketPriority.LOW,
                    com.deskflow.module.ticket.domain.TicketCategory.GENERAL))
        .when()
            .post("/api/v1/tickets")
        .then()
            .statusCode(403);
    }

    @Test @Order(4)
    void listTickets_asCustomer_returnsOwnTickets() {
        given()
            .header("Authorization", "Bearer " + customerToken)
        .when()
            .get("/api/v1/tickets")
        .then()
            .statusCode(200)
            .body("content.size()", equalTo(1))
            .body("content[0].status", equalTo("OPEN"));
    }

    @Test @Order(5)
    void updateStatus_agentMovesToInProgress_succeeds() {
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + agentToken)
            .body(new UpdateStatusRequest(TicketStatus.IN_PROGRESS, "Investigating now"))
        .when()
            .patch("/api/v1/tickets/" + ticketId + "/status")
        .then()
            .statusCode(200)
            .body("status", equalTo("IN_PROGRESS"))
            .body("statusUpdates.size()", equalTo(1));
    }

    @Test @Order(6)
    void updateStatus_agentResolvesTicket_succeeds() {
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + agentToken)
            .body(new UpdateStatusRequest(TicketStatus.RESOLVED, "Fixed the issue"))
        .when()
            .patch("/api/v1/tickets/" + ticketId + "/status")
        .then()
            .statusCode(200)
            .body("status", equalTo("RESOLVED"))
            .body("resolvedAt", notNullValue());
    }

    @Test @Order(7)
    void updateStatus_customerClosesResolvedTicket_succeeds() {
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + customerToken)
            .body(new UpdateStatusRequest(TicketStatus.CLOSED, "Thank you!"))
        .when()
            .patch("/api/v1/tickets/" + ticketId + "/status")
        .then()
            .statusCode(200)
            .body("status", equalTo("CLOSED"))
            .body("closedAt", notNullValue());
    }

    @Test @Order(8)
    void updateStatus_invalidTransition_returns422() {
        // Ticket is now CLOSED — cannot transition anywhere
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + agentToken)
            .body(new UpdateStatusRequest(TicketStatus.OPEN, null))
        .when()
            .patch("/api/v1/tickets/" + ticketId + "/status")
        .then()
            .statusCode(422);
    }

    // ── static import for test assertions ─────────────────────────
    private static void assertThat(Object actual) {
        org.assertj.core.api.Assertions.assertThat(actual).isNotNull();
    }
}
