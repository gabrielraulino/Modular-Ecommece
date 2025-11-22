package com.modulith.ecommerce.order;

import com.modulith.ecommerce.event.CheckoutEvent;
import com.modulith.ecommerce.event.OrderCancelledEvent;
import com.modulith.ecommerce.event.OrderCreatedEvent;
import com.modulith.ecommerce.event.UpdateEvent;
import com.modulith.ecommerce.exception.ResourceNotFoundException;
import com.modulith.ecommerce.exception.InvalidOperationException;
import com.modulith.ecommerce.product.ProductDTO;
import com.modulith.ecommerce.product.ProductModuleAPI;
import com.modulith.ecommerce.user.UserModuleAPI;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@AllArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepository repository;

    private final UserModuleAPI userModuleAPI;

    private final ApplicationEventPublisher eventPublisher;

    private final ProductModuleAPI productModule;


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

        List<OrderItemDTO> items = buildOrderItemsDTO(order.getItems());

        BigDecimal totalPrice = items.stream().map(OrderItemDTO::totalPrice).reduce(BigDecimal.ZERO, BigDecimal::add);

        Integer totalQuantity = items.stream().map(OrderItemDTO::quantity).reduce(0, Integer::sum);

        return new OrderDTO(
                order.getId(),
                order.getUserId(),
                order.getStatus(),
                order.getUpdatedAt(),
                order.getCreatedAt(),
                items,
                order.getPaymentMethod(),
                totalQuantity,
                totalPrice
        );
    }

    private List<OrderItemDTO> buildOrderItemsDTO(List<OrderItem> items){
        if (items.isEmpty()) {
            return List.of();
        }

        // Extract unique product IDs
        Set<Long> productIds = items.stream()
                .map(OrderItem::getProductId)
                .collect(Collectors.toSet());

        // Fetch all products in a single call (avoids N+1)
        Map<Long, ProductDTO> productMap = productModule.findAllProductsByIds(productIds)
                .stream()
                .collect(Collectors.toMap(ProductDTO::id, Function.identity()));

        // Iterate over original items to maintain order and process all
        // Filter items whose products were not found
        return items.stream()
                .filter(item -> productMap.containsKey(item.getProductId()))
                .map(item -> {
                    ProductDTO product = productMap.get(item.getProductId());
                    BigDecimal totalPrice = product.priceAmount()
                            .multiply(BigDecimal.valueOf(item.getQuantity()));
                    
                    return new OrderItemDTO(
                            item.getId(),
                            product.name(),
                            item.getProductId(),
                            item.getQuantity(),
                            product.priceAmount(),
                            totalPrice
                    );
                })
                .toList();
    }

    @Transactional
    public OrderDTO cancelOrder(Long orderId) {
        log.info("Starting order cancellation: {}", orderId);

        Order order = findOrderById(orderId);

        validateOrderStatusToCancel(order);

        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());
        Order cancelledOrder = repository.save(order);

        // Build cancellation event
        List<OrderCancelledEvent.CancelledItem> cancelledItems = order.getItems().stream()
                .map(item -> new OrderCancelledEvent.CancelledItem(
                        item.getProductId(),
                        item.getQuantity()
                ))
                .toList();

        OrderCancelledEvent event = new OrderCancelledEvent(
                order.getId(),
                order.getUserId(),
                cancelledItems
        );

        // Publish event to restore stock
        eventPublisher.publishEvent(event);

        log.info("Order {} cancelled successfully. Event published to restore stock for {} items",
                orderId, cancelledItems.size());

        return buildOrderDTO(cancelledOrder);
    }

    @EventListener
    public void onCheckoutEvent(CheckoutEvent event) {
        log.info("Received checkout event for user: {}, cart: {}", event.user(), event.cart());

            // Create the order
            Order order = Order.builder()
                    .userId(event.user())
                    .status(OrderStatus.PENDING)
                    .updatedAt(LocalDateTime.now())
                    .createdAt(LocalDateTime.now())
                    .paymentMethod(event.paymentMethod())
                    .build();

            Map<Long, Integer> productQuantities = event.items().stream()
                    .collect(java.util.stream.Collectors.toMap(
                            CheckoutEvent.CheckoutItem::product,
                            CheckoutEvent.CheckoutItem::quantity
                    ));

            UpdateEvent updateProductEvent = new UpdateEvent(
                    event.cart(),
                    event.user(),
                    productQuantities
            );

            eventPublisher.publishEvent(updateProductEvent);

            // Create order items
            List<OrderItem> orderItems = event.items().stream()
                    .map(item -> {
                        OrderItem orderItem = new OrderItem(
                                item.product(),
                                item.quantity()
                        );
                        orderItem.setOrder(order);
                        return orderItem;
                    })
                    .toList();

            order.setItems(orderItems);

            // Save the order
            Order savedOrder = repository.save(order);

        OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent(
                savedOrder.getId()
        );

        eventPublisher.publishEvent(orderCreatedEvent);

            log.info("Order created successfully. ID: {}, User: {}",
                    savedOrder.getId(), savedOrder.getUserId());

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

        if(order.getStatus().equals(OrderStatus.SHIPPED)) {
            throw new InvalidOperationException("cancel order", "cannot cancel a shipped order");
        }
    }
}