package com.modulith.ecommerce.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Login response with JWT token")
public record LoginResponse(
        @Schema(description = "JWT token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String token,

        @Schema(description = "Token type", example = "Bearer")
        String type,

        @Schema(description = "User email", example = "user@example.com")
        String email,

        @Schema(description = "User role", example = "USER")
        String role
) {
    public LoginResponse(String token, String email, String role) {
        this(token, "Bearer", email, role);
    }
}

