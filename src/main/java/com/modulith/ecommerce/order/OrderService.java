package com.modulith.ecommerce.order;

import com.modulith.ecommerce.event.CheckoutEvent;
import com.modulith.ecommerce.event.OrderCancelledEvent;
import com.modulith.ecommerce.exception.ResourceNotFoundException;
import com.modulith.ecommerce.exception.InvalidOperationException;
import com.modulith.ecommerce.user.UserModuleAPI;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;


@Service
@AllArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepository repository;

    private final UserModuleAPI userModuleAPI;

    private final ApplicationEventPublisher eventPublisher;


    public OrderDTO findById(Long id){
        return buildOrderDTO(repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Order", id)));
    }

    public List<OrderDTO> findByUserId(Long id){
        userModuleAPI.findUserById(id).orElseThrow(() -> new ResourceNotFoundException("User", id));

        return repository.findByUserId(id).stream().map(this::buildOrderDTO).toList();
    }


    public List<OrderDTO> getAllOrders(){
        return repository.findAll().stream().map(this::buildOrderDTO).toList();
    }

    private OrderDTO buildOrderDTO(Order order){
        return OrderDTO.fromEntity(order, order.getItems().stream().map(OrderItemDTO::fromEntity).toList());
    }

    @Transactional
    public OrderDTO cancelOrder(Long orderId) {
        log.info("Starting order cancellation: {}", orderId);

        Order order = findOrderById(orderId);

        validateOrderStatusToCancel(order);

        order.setStatus(OrderStatus.CANCELLED);
        Order cancelledOrder = repository.save(order);

        // Build cancellation event
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

        // Publish event to restore stock
        eventPublisher.publishEvent(event);

        log.info("Order {} cancelled successfully. Event published to restore stock for {} items",
                orderId, cancelledItems.size());

        return buildOrderDTO(cancelledOrder);
    }

    @TransactionalEventListener
    public void onCheckoutEvent(CheckoutEvent event) {
        log.info("Received checkout event for user: {}, cart: {}", event.userId(), event.cartId());

        try {
            // Create the order
            Order order = new Order();
            order.setUserId(event.userId());
            order.setTotalAmount(event.totalAmount());
            order.setStatus(OrderStatus.PENDING);
            order.setOrderDate(event.checkoutDate());

            // Create order items
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

            // Save the order
            Order savedOrder = repository.save(order);

            log.info("Order created successfully. ID: {}, User: {}, Total: {}",
                    savedOrder.getId(), savedOrder.getUserId(),
                    savedOrder.getTotalAmount());

        } catch (Exception e) {
            log.error("Error processing checkout event for user: {}", event.userId(), e);
            throw new RuntimeException("Failed to create order from checkout", e);
        }
    }

    private Order findOrderById(Long id){
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
    }

    private void validateOrderStatusToCancel(Order order){
        if (order.getStatus().equals(OrderStatus.CANCELLED)) {
            throw new InvalidOperationException("cancel order", "order is already cancelled");
        }

        if (order.getStatus().equals(OrderStatus.DELIVERED)) {
            throw new InvalidOperationException("cancel order", "cannot cancel a delivered order");
        }
    }
}