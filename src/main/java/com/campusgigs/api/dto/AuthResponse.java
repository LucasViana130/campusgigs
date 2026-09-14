package com.campusgigs.api.dto;

public record AuthResponse(
        String token,
        String type,
        long expiresInMs,
        UserResponse user
) {
    public static AuthResponse of(String token, long expiresInMs, UserResponse user) {
        return new AuthResponse(token, "Bearer", expiresInMs, user);
    }
}
