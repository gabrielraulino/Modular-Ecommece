package com.modulith.ecommerce.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Cart checkout event.
 * This event is published when a user completes cart checkout.
 * Following Spring Modulith practices, we use an immutable record to transfer
 * data between Cart and Order modules.
 */
public record CheckoutEvent(
        Long cart,
        Long user,
        List<CheckoutItem> items
) {
    /**
     * Checkout item representing a product and its quantity.
     * Contains only essential data to reduce serialized event size.
     */
    public record CheckoutItem(
            Long product,
            Integer quantity
    ) {}
}
