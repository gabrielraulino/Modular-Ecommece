package com.modulith.ecommerce.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "User registration request")
public record RegisterRequest(
        @Schema(description = "User's full name", example = "John Doe")
        @NotBlank(message = "Name is required")
        String name,

        @Schema(description = "User email", example = "user@example.com")
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @Schema(description = "User password", example = "password123")
        @NotBlank(message = "Password is required")
        String password,

        @Schema(description = "Admin key (optional - if provided and valid, creates an admin user)", example = "admin-secret-key")
        String adminKey
) {
}

