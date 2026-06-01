package com.deskflow.module.auth.service;

import com.deskflow.module.auth.dto.LoginRequest;
import com.deskflow.module.auth.dto.RegisterRequest;
import com.deskflow.module.auth.security.JwtTokenProvider;
import com.deskflow.module.user.domain.User;
import com.deskflow.module.user.domain.UserRole;
import com.deskflow.module.user.repository.UserRepository;
import com.deskflow.shared.exception.BusinessRuleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtTokenProvider jwtTokenProvider;
    @Mock RedisTemplate<String, Object> redisTemplate;
    @Mock ValueOperations<String, Object> valueOps;

    @InjectMocks AuthService authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "refreshTokenExpiryMs", 604_800_000L);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }

    @Test
    void register_newEmail_createsCustomerAndReturnsTokens() {
        var req = new RegisterRequest("alice@example.com", "password123", "Alice", "Smith");
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            ReflectionTestUtils.setField(u, "id", UUID.randomUUID());
            return u;
        });
        when(jwtTokenProvider.generateAccessToken(any())).thenReturn("access");
        when(jwtTokenProvider.generateRefreshToken(any())).thenReturn("refresh");
        when(jwtTokenProvider.getAccessTokenExpiryMs()).thenReturn(900_000L);

        var response = authService.register(req);

        assertThat(response.accessToken()).isEqualTo("access");
        assertThat(response.user().role()).isEqualTo(UserRole.CUSTOMER);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_duplicateEmail_throwsBusinessRuleException() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);
        var req = new RegisterRequest("dup@example.com", "password123", "A", "B");

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("already registered");
    }

    @Test
    void login_validCredentials_returnsTokens() {
        User user = buildUser();
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashed")).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(user)).thenReturn("access");
        when(jwtTokenProvider.generateRefreshToken(user)).thenReturn("refresh");
        when(jwtTokenProvider.getAccessTokenExpiryMs()).thenReturn(900_000L);

        var response = authService.login(new LoginRequest("alice@example.com", "password123"));

        assertThat(response.accessToken()).isEqualTo("access");
    }

    @Test
    void login_wrongPassword_throwsBadCredentials() {
        User user = buildUser();
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("alice@example.com", "wrong")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void login_unknownEmail_throwsBadCredentials() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("nobody@example.com", "pw")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void login_disabledAccount_throwsBusinessRuleException() {
        User user = buildUser();
        user.setActive(false);
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashed")).thenReturn(true);

        assertThatThrownBy(() -> authService.login(new LoginRequest("alice@example.com", "password123")))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("disabled");
    }

    private User buildUser() {
        User user = new User();
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        user.setEmail("alice@example.com");
        user.setPasswordHash("hashed");
        user.setFirstName("Alice");
        user.setLastName("Smith");
        user.setRole(UserRole.CUSTOMER);
        user.setActive(true);
        return user;
    }
}