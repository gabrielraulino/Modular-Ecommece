package com.modulith.ecommerce.order;

public enum OrderStatus {
    PENDING(),
    PROCESSING(),
    SHIPPED(),
    DELIVERED(),
    CANCELLED();

    OrderStatus() {
    }
}