package com.modulith.ecommerce.order;

import java.math.BigDecimal;

public record OrderItemDTO(
    Long id,
    Long productId,
    Integer quantity,
    BigDecimal unitPriceAmount,
    String unitPriceCurrency
) {

    public static OrderItemDTO fromEntity(OrderItem item){
        return new OrderItemDTO(
            item.getId(),
            item.getProductId(),
            item.getQuantity(),
            item.getUnitPriceAmount(),
            item.getUnitPriceCurrency()
        );
    }
}