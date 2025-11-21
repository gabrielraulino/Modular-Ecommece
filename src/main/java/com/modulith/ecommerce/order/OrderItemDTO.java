package com.modulith.ecommerce.order;

import java.math.BigDecimal;

public record OrderItemDTO(
    Long id,
    String name,
    Long productId,
    Integer quantity,
    BigDecimal priceAmount,
    BigDecimal totalPrice
) {}