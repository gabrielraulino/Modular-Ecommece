package com.modulith.ecommerce.cart;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Request to add product to cart")
public record addCartItemDTO(
        @Schema(description = "User ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        Long userId,

        @Schema(description = "Product ID to add", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        Long productId,

        @Schema(description = "Quantity to add", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
        int quantity
) {

}
