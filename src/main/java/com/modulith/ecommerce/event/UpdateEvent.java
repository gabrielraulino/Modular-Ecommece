package com.modulith.ecommerce.event;

import java.util.Map;

public record UpdateEvent(
        Long cart,
        Long user,
        Map<Long, Integer> productQuantities
) {
}
