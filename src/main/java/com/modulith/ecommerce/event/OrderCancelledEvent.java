package com.modulith.ecommerce.event;

import java.util.List;

/**
 * Order cancellation event.
 * This event is published when an order is cancelled.
 * Products will have their stock incremented back.
 */
public record OrderCancelledEvent(
        Long orderId,
        Long userId,
        List<CancelledItem> items
) {
    /**
     * Cancelled item that needs to have its stock restored.
     */
    public record CancelledItem(
            Long productId,
            Integer quantity
    ) {}
}

