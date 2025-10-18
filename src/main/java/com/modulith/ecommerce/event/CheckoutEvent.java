package com.modulith.ecommerce.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Evento de checkout do carrinho.
 * Este evento é publicado quando um usuário finaliza o checkout do carrinho.
 * Seguindo as práticas do Spring Modulith, usamos um record imutável para transferir
 * os dados entre os módulos Cart e Order.
 */
public record CheckoutEvent(
        Long cartId,
        Long userId,
        List<CheckoutItem> items,
        BigDecimal totalAmount,
        String currency,
        LocalDateTime checkoutDate
) {
    /**
     * Item do checkout representando um produto e sua quantidade.
     * Contém apenas os dados essenciais para reduzir o tamanho do evento serializado.
     */
    public record CheckoutItem(
            Long productId,
            Integer quantity,
            BigDecimal unitPrice,
            String currency
    ) {}
}
