package com.modulith.ecommerce.order;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderDTO(
        Long id,
        Long userId,
        OrderStatus status,
        LocalDateTime updatedAt,
        LocalDateTime createdAt,
        List<OrderItemDTO> items,
        Integer totalQuantity,
        BigDecimal totalPrice
) {

}
