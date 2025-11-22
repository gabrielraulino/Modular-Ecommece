package com.modulith.ecommerce.product;

import com.modulith.ecommerce.product.domain.Product;

import java.math.BigDecimal;

public record ProductDTO(
        Long id,
        String name,
        String description,
        BigDecimal priceAmount,
        int stock
) {

    public static ProductDTO fromEntity(Product product) {
        return new ProductDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPriceAmount(),
                product.getStock());
    }
}
