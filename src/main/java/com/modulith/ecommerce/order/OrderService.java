package com.modulith.ecommerce.order;

import com.modulith.ecommerce.event.CheckoutEvent;
import com.modulith.ecommerce.event.OrderCancelledEvent;
import com.modulith.ecommerce.user.UserModuleAPI;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@AllArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepository repository;

    private final UserModuleAPI userModuleAPI;

    private final ApplicationEventPublisher eventPublisher;

    public OrderDTO findById(Long id){
        return buildOrderDTO(repository.findById(id).orElseThrow(() -> new RuntimeException("Order not found")));
    }

    public List<OrderDTO> findByUserId(Long id){
        userModuleAPI.findUserById(id).orElseThrow(() -> new RuntimeException("User not found"));

        return repository.findByUserId(id).stream().map(this::buildOrderDTO).collect(Collectors.toList());
    }


    public List<OrderDTO> getAllOrders(){
        return repository.findAll().stream().map(this::buildOrderDTO).collect(Collectors.toList());
    }

    private OrderDTO buildOrderDTO(Order order){
        return OrderDTO.fromEntity(order, order.getItems().stream().map(OrderItemDTO::fromEntity).collect(Collectors.toList()));
    }

    /**
     * Cancela um pedido e publica evento para restaurar o estoque.
     *
     * @param orderId ID do pedido a ser cancelado
     * @return OrderDTO com status atualizado para CANCELLED
     */
    @Transactional
    public OrderDTO cancelOrder(Long orderId) {
        log.info("Iniciando cancelamento do pedido: {}", orderId);

        Order order = findOrderById(orderId);

        // Validar se o pedido pode ser cancelado
        validateOrderStatusToCancel(order);

        // Atualizar status do pedido
        order.setStatus(OrderStatus.CANCELLED);
        Order cancelledOrder = repository.save(order);

        // Construir evento de cancelamento
        List<OrderCancelledEvent.CancelledItem> cancelledItems = order.getItems().stream()
                .map(item -> new OrderCancelledEvent.CancelledItem(
                        item.getProductId(),
                        item.getQuantity()
                ))
                .collect(Collectors.toList());

        OrderCancelledEvent event = new OrderCancelledEvent(
                order.getId(),
                order.getUserId(),
                cancelledItems,
                LocalDateTime.now()
        );

        // Publicar evento para restaurar estoque
        eventPublisher.publishEvent(event);

        log.info("Pedido {} cancelado com sucesso. Evento publicado para restaurar estoque de {} itens",
                orderId, cancelledItems.size());

        return buildOrderDTO(cancelledOrder);
    }

    /**
     * Listener de evento de checkout do carrinho.
     * Seguindo as práticas do Spring Modulith, este método:
     * - Usa @ApplicationModuleListener para processamento assíncrono e transacional
     * - Executa em uma nova transação (REQUIRES_NEW)
     * - É executado após o commit da transação original
      *
     * Este listener cria um pedido a partir dos dados do evento de checkout.
     *
     * @param event Evento de checkout publicado pelo módulo Cart
     */
    @ApplicationModuleListener
    public void onCheckoutEvent(CheckoutEvent event) {
        log.info("Recebido evento de checkout para o usuário: {}, carrinho: {}", event.userId(), event.cartId());

        try {
            // Criar o pedido
            Order order = new Order();
            order.setUserId(event.userId());
            order.setTotalAmount(event.totalAmount());
            order.setStatus(OrderStatus.PENDING);
            order.setOrderDate(event.checkoutDate());

            // Criar os itens do pedido
            List<OrderItem> orderItems = event.items().stream()
                    .map(item -> {
                        OrderItem orderItem = new OrderItem(
                                item.productId(),
                                item.quantity(),
                                item.unitPrice()
                        );
                        orderItem.setOrder(order);
                        return orderItem;
                    })
                    .collect(Collectors.toList());

            order.setItems(orderItems);

            // Salvar o pedido
            Order savedOrder = repository.save(order);

            log.info("Pedido criado com sucesso. ID: {}, Usuário: {}, Total: {}",
                    savedOrder.getId(), savedOrder.getUserId(),
                    savedOrder.getTotalAmount());

        } catch (Exception e) {
            log.error("Erro ao processar evento de checkout para o usuário: {}", event.userId(), e);
            throw new RuntimeException("Falha ao criar pedido a partir do checkout", e);
        }
    }

    private Order findOrderById(Long id){
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found: " + id));
    }

    private void validateOrderStatusToCancel(Order order){
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("Order is already cancelled");
        }

        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new RuntimeException("Cannot cancel a delivered order");
        }
    }
}