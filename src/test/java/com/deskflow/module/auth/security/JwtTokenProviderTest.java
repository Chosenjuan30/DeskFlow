package com.deskflow.module.auth.security;

import com.deskflow.module.user.domain.User;
import com.deskflow.module.user.domain.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider provider;

    @BeforeEach
    void setUp() {
        provider = new JwtTokenProvider();
        ReflectionTestUtils.setField(provider, "secret",
                "deskflow-secret-key-at-least-256-bits-change-in-prod-1234567890abcdef");
        ReflectionTestUtils.setField(provider, "accessTokenExpiryMs", 900_000L);
        provider.init();
    }

    @Test
    void generateAccessToken_isValid() {
        String token = provider.generateAccessToken(buildUser());
        assertThat(provider.isValid(token)).isTrue();
    }

    @Test
    void extractUserId_fromAccessToken_matchesUser() {
        User user = buildUser();
        String token = provider.generateAccessToken(user);
        assertThat(provider.extractUserId(token)).isEqualTo(user.getId());
    }

    @Test
    void isValid_tamperedSignature_returnsFalse() {
        String token = provider.generateAccessToken(buildUser());
        String tampered = token.substring(0, token.length() - 4) + "XXXX";
        assertThat(provider.isValid(tampered)).isFalse();
    }

    @Test
    void isValid_randomString_returnsFalse() {
        assertThat(provider.isValid("not.a.jwt")).isFalse();
    }

    @Test
    void generateRefreshToken_subjectMatchesUser() {
        User user = buildUser();
        String token = provider.generateRefreshToken(user);
        assertThat(provider.extractUserId(token)).isEqualTo(user.getId());
    }

    private User buildUser() {
        User user = new User();
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        user.setEmail("test@example.com");
        user.setPasswordHash("hashed");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setRole(UserRole.CUSTOMER);
        user.setActive(true);
        return user;
    }
}