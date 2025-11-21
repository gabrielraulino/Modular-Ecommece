package com.modulith.ecommerce.cart;

import com.modulith.ecommerce.event.CheckoutEvent;
import com.modulith.ecommerce.exception.InsufficientStockException;
import com.modulith.ecommerce.exception.ResourceNotFoundException;
import com.modulith.ecommerce.exception.InvalidOperationException;
import com.modulith.ecommerce.product.ProductDTO;
import com.modulith.ecommerce.product.ProductModuleAPI;
import com.modulith.ecommerce.user.UserModuleAPI;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class CartService {
    private final CartRepository repository;

    private final ProductModuleAPI productModule;

    private final UserModuleAPI userModule;

    private final ApplicationEventPublisher eventPublisher;

    public List<CartDTO> getAllCarts() {
        return repository.findAll().stream()
                .map(this::buildCartDTO)
                .toList();
    }

    // returns cart by user id, or an empty cart if none exists
    public CartDTO getCartUserById(Long id) {
        Cart cart = repository.findCartByUserId(id)
                .orElseGet(Cart::new);
        return buildCartDTO(cart);
    }

    public CartDTO addOrUpdateItem(AddCartItemDTO cartData) {
        userModule.validateUserExists(cartData.userId());

        productModule.validateProductStock(cartData.productId(), cartData.quantity());

        Cart cart = repository.findCartByUserId(cartData.userId()).orElseGet(() -> {
            Cart newCart = new Cart(null, cartData.userId(), LocalDateTime.now(), null);
            return repository.save(newCart);
        });

        Optional<CartItem> existingItemOpt = cart.getItems()
                .stream()
                .filter(
                        item -> item
                                .getProductId()
                                .equals(cartData.productId()))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            CartItem existingItem = existingItemOpt.get();
            CartItem updatedItem = existingItem.updateQuantity(cartData.quantity());
            cart.removeItem(existingItem);
            cart.addItem(updatedItem);
        } else {
            CartItem newItem = new CartItem(cart, cartData.productId(), cartData.quantity());
            cart.addItem(newItem);
        }

        // Update updatedAt
        cart.setUpdatedAt(LocalDateTime.now());

        Cart savedCart = repository.save(cart);
        return buildCartDTO(savedCart);
    }

    private CartDTO buildCartDTO(Cart cart) {
        List<CartItemDTO> enrichedItems = buildCartItemsDTO(cart.getItems());

        // Calculate total quantity
        int totalQuantity = enrichedItems.stream()
                .mapToInt(CartItemDTO::quantity)
                .sum();

        // Calculate total price
        BigDecimal totalPrice = enrichedItems.stream()
                .map(CartItemDTO::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartDTO(
                cart.getId(),
                cart.getUserId(),
                enrichedItems,
                totalQuantity,
                totalPrice,
                cart.getCreatedAt(),
                cart.getUpdatedAt()
        );
    }

    private List<CartItemDTO> buildCartItemsDTO(List<CartItem> items) {
        if (items.isEmpty()) {
            return List.of();
        }

        // Extract unique product IDs
        Set<Long> productIds = items.stream()
                .map(CartItem::getProductId)
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
                    BigDecimal subtotal = product.priceAmount()
                            .multiply(BigDecimal.valueOf(item.getQuantity()));
                    
                    return new CartItemDTO(
                            item.getId(),
                            item.getProductId(),
                            product.name(),
                            product.priceAmount(),
                            item.getQuantity(),
                            subtotal
                    );
                })
                .toList();
    }

    @Transactional
    public ResponseEntity<String> checkout(Long userId) {
        userModule.validateUserExists(userId);

        Cart cart = findCartByUser(userId);

        validateCartEmpty(cart);

        Map<Long, Integer> productQuantities = cart.getItems().stream()
                .collect(Collectors.toMap(CartItem::getProductId, CartItem::getQuantity));

//        productModule.validateProductsStock(productQuantities);

        List<CheckoutEvent.CheckoutItem> checkoutItems = cart.getItems().stream()
                .map(item -> {
                    return new CheckoutEvent.CheckoutItem(
                            item.getProductId(),
                            item.getQuantity()
                    );
                })
                .toList();

        // Publish checkout event
        CheckoutEvent event = new CheckoutEvent(
                cart.getId(),
                userId,
                checkoutItems
        );

            eventPublisher.publishEvent(event);

        // Clean cart itens (orphanRemoval = true will handle the deletion)
        cart.getItems().clear();

        cart.setUpdatedAt(LocalDateTime.now());

        repository.save(cart);

        return ResponseEntity.ok("Checkout completed successfully for cart ID: " + cart.getId());
    }

    private Cart findCartByUser(Long userId) {
        return repository.findCartByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "user_id", userId));
    }

    private void validateCartEmpty(Cart cart) {
        if (cart.getItems().isEmpty()) {
            throw new InvalidOperationException("checkout", "cart is empty");
        }
    }

}
