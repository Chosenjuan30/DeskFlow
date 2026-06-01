package com.deskflow.module.auth.web;

import com.deskflow.module.auth.dto.LoginRequest;
import com.deskflow.module.auth.dto.RegisterRequest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.kafka.test.context.EmbeddedKafka;
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
class AuthControllerIntegrationTest {

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
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",      postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host",     redis::getHost);
        registry.add("spring.data.redis.port",     () -> redis.getMappedPort(6379));
    }

    @LocalServerPort
    int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "";
    }

    @Test
    @Order(1)
    void register_validRequest_returns201WithTokens() {
        given()
            .contentType(ContentType.JSON)
            .body(new RegisterRequest("user@test.com", "password123", "John", "Doe"))
        .when()
            .post("/api/v1/auth/register")
        .then()
            .statusCode(201)
            .body("accessToken",   notNullValue())
            .body("refreshToken",  notNullValue())
            .body("user.email",    equalTo("user@test.com"))
            .body("user.role",     equalTo("CUSTOMER"));
    }

    @Test
    @Order(2)
    void register_duplicateEmail_returns422() {
        given()
            .contentType(ContentType.JSON)
            .body(new RegisterRequest("user@test.com", "password123", "Jane", "Doe"))
        .when()
            .post("/api/v1/auth/register")
        .then()
            .statusCode(422);
    }

    @Test
    @Order(3)
    void login_validCredentials_returns200WithToken() {
        given()
            .contentType(ContentType.JSON)
            .body(new LoginRequest("user@test.com", "password123"))
        .when()
            .post("/api/v1/auth/login")
        .then()
            .statusCode(200)
            .body("accessToken",  notNullValue())
            .body("user.email",   equalTo("user@test.com"));
    }

    @Test
    @Order(4)
    void login_wrongPassword_returns401() {
        given()
            .contentType(ContentType.JSON)
            .body(new LoginRequest("user@test.com", "wrongpassword"))
        .when()
            .post("/api/v1/auth/login")
        .then()
            .statusCode(401);
    }

    @Test
    @Order(5)
    void me_validToken_returnsUserInfo() {
        String token = given()
            .contentType(ContentType.JSON)
            .body(new LoginRequest("user@test.com", "password123"))
            .post("/api/v1/auth/login")
            .jsonPath().getString("accessToken");

        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/api/v1/auth/me")
        .then()
            .statusCode(200)
            .body("email", equalTo("user@test.com"));
    }
}