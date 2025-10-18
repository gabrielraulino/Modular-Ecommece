package com.modulith.ecommerce.user;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserCreateDTO(
        @Schema(description = "User's full name", example = "John Doe", requiredMode = Schema.RequiredMode.REQUIRED)
        String name,
        @Schema(description = "User's email address", example = "john@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
        String email,
        @Schema(description = "password", example = "password123", requiredMode = Schema.RequiredMode.REQUIRED)
        String password
) {

}
