package com.campusgigs.api.dto;

import com.campusgigs.api.domain.Role;
import com.campusgigs.api.domain.User;

/**
 * Representacao publica de um usuario. Nunca inclui a senha/hash.
 */
public record UserResponse(
        Long id,
        String name,
        String email,
        String cep,
        String city,
        String state,
        Role role
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCep(),
                user.getCity(),
                user.getState(),
                user.getRole()
        );
    }
}
