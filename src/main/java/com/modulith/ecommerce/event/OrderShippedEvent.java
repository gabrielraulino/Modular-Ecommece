package com.modulith.ecommerce.event;

public record OrderShippedEvent(long orderId, Long userId) {
}
