package com.modulith.ecommerce.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderDTO(
        Long id,
        Long userId,
        BigDecimal totalAmount,
        OrderStatus status,
        LocalDateTime orderDate,
        List<OrderItemDTO> items
) {
    public static OrderDTO fromEntity(Order order, List<OrderItemDTO> items) {
        return new OrderDTO(
                order.getId(),
                order.getUserId(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getOrderDate(),
                items
        );
    }
}
