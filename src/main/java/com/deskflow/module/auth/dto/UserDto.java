package com.deskflow.module.auth.dto;

import com.deskflow.module.user.domain.User;
import com.deskflow.module.user.domain.UserRole;

import java.util.UUID;

public record UserDto(
        UUID id,
        String email,
        String firstName,
        String lastName,
        UserRole role,
        boolean active
) {
    public static UserDto from(User user) {
        return new UserDto(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole(),
                user.isActive()
        );
    }
}