package com.modulith.ecommerce.event;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Evento de cancelamento de pedido.
 * Este evento é publicado quando um pedido é cancelado.
 * Os produtos terão seu estoque incrementado de volta.
 */
public record OrderCancelledEvent(
        Long orderId,
        Long userId,
        List<CancelledItem> items,
        LocalDateTime cancelledDate
) {
    /**
     * Item cancelado que precisa ter o estoque restaurado.
     */
    public record CancelledItem(
            Long productId,
            Integer quantity
    ) {}
}

