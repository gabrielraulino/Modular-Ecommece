package com.modulith.ecommerce.event;

public record OrderProcessedEvent(Long orderId, Long userId) {
}
