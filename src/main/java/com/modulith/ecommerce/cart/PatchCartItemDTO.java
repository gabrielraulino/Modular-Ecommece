package com.modulith.ecommerce.cart;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Request to add product to cart")
public record PatchCartItemDTO(
        @Schema(description = "Product ID to update", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        Long productId,
        @Schema(description = "New quantity", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer quantity
) {
}
