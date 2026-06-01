package com.deskflow.module.auth.service;

import com.deskflow.module.auth.dto.*;
import com.deskflow.module.auth.security.JwtTokenProvider;
import com.deskflow.module.user.domain.User;
import com.deskflow.module.user.domain.UserRole;
import com.deskflow.module.user.repository.UserRepository;
import com.deskflow.shared.exception.BusinessRuleException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private static final String REFRESH_PREFIX   = "refresh:";
    private static final String BLACKLIST_PREFIX = "blacklist:";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${app.jwt.refresh-token-expiry-ms}")
    private long refreshTokenExpiryMs;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email().toLowerCase())) {
            throw new BusinessRuleException("Email is already registered");
        }

        User user = new User();
        user.setEmail(request.email().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setRole(UserRole.CUSTOMER);
        user.setActive(true);

        userRepository.save(user);
        log.info("Registered user {}", user.getEmail());

        return buildAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email().toLowerCase())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        if (!user.isActive()) {
            throw new BusinessRuleException("Account is disabled");
        }

        return buildAuthResponse(user);
    }

    public AuthResponse refresh(RefreshRequest request) {
        String key = REFRESH_PREFIX + request.refreshToken();
        Object storedId = redisTemplate.opsForValue().get(key);

        if (storedId == null) {
            throw new BadCredentialsException("Refresh token is expired or invalid");
        }

        UUID userId = UUID.fromString(storedId.toString());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadCredentialsException("User not found"));

        redisTemplate.delete(key); // rotate: old token is consumed
        return buildAuthResponse(user);
    }

    public void logout(String accessToken) {
        if (!jwtTokenProvider.isValid(accessToken)) {
            return;
        }
        long remainingMs = jwtTokenProvider.parseToken(accessToken).getExpiration().getTime()
                - System.currentTimeMillis();
        if (remainingMs > 0) {
            redisTemplate.opsForValue().set(
                    BLACKLIST_PREFIX + accessToken, "1",
                    remainingMs, TimeUnit.MILLISECONDS
            );
        }
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken  = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        redisTemplate.opsForValue().set(
                REFRESH_PREFIX + refreshToken,
                user.getId().toString(),
                refreshTokenExpiryMs,
                TimeUnit.MILLISECONDS
        );

        return new AuthResponse(
                accessToken,
                refreshToken,
                jwtTokenProvider.getAccessTokenExpiryMs(),
                UserDto.from(user)
        );
    }
}