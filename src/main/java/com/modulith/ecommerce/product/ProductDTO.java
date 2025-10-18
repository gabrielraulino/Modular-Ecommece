package com.modulith.ecommerce.product;

import java.math.BigDecimal;

public record ProductDTO(
    Long id,
    String name,
    String description,
    String priceCurrency,
    BigDecimal priceAmount,
    int stock
) {

    public static ProductDTO fromEntity(Product product) {
        return new ProductDTO(product.getId(), product.getName(), product.getDescription(), product.getPriceCurrency(), product.getPriceAmount(), product.getStock());
    }
}
