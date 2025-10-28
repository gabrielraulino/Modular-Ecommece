package com.modulith.ecommerce.cart;

import com.modulith.ecommerce.event.CheckoutEvent;
import com.modulith.ecommerce.exception.ResourceNotFoundException;
import com.modulith.ecommerce.exception.InvalidOperationException;
import com.modulith.ecommerce.product.ProductDTO;
import com.modulith.ecommerce.product.ProductModuleAPI;
import com.modulith.ecommerce.user.UserModuleAPI;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
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

    public CartDTO getCartUserById(Long id) {
        Cart cart = repository.findCartByUserId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", id));
        return buildCartDTO(cart);
    }

    public CartDTO addOrUpdateItem(AddCartItemDTO cartData) {
        userModule.validateUserExists(cartData.userId());

        productModule.validateProductStock(cartData.productId(), cartData.quantity());

        Cart cart = repository.findCartByUserId(cartData.userId()).orElseGet(() -> {
            Cart newCart = new Cart(null, cartData.userId(), LocalDateTime.now(), null);
            return repository.save(newCart);
        });

        Optional<CartItem> existingItemOpt = cart.getItems().stream().filter(item -> item.getProductId().equals(cartData.productId())).findFirst();

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

    public CartDTO updateItemQuantity(Long userId, PatchCartItemDTO cartData) {
        Cart cart = findCartByUser(userId);

        productModule.validateProductStock(cartData.productId(), cartData.quantity());

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(cartData.productId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "product_id", cartData.productId()));

        CartItem updatedItem = item.updateQuantity(cartData.quantity());
        cart.removeItem(item);
        cart.addItem(updatedItem);

        // Update updatedAt
        cart.setUpdatedAt(LocalDateTime.now());

        Cart savedCart = repository.save(cart);
        return buildCartDTO(savedCart);
    }

    private CartDTO buildCartDTO(Cart cart) {
        List<CartItemDTO> enrichedItems = cart.getItems()
                .stream()
                .map(this::buildCartItemDTO)
                .toList();

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

    private CartItemDTO buildCartItemDTO(CartItem item) {
        Optional<ProductDTO> productOpt = productModule.findProductById(item.getProductId());

        if (productOpt.isEmpty()) {
            throw new ResourceNotFoundException("Product", item.getProductId());
        }

        ProductDTO product = productOpt.get();
        BigDecimal subtotal = product.priceAmount().multiply(BigDecimal.valueOf(item.getQuantity()));

        return new CartItemDTO(
                item.getId(),
                item.getProductId(),
                product.name(),
                product.priceAmount(),
                item.getQuantity(),
                subtotal
        );
    }


    @Transactional
    public CartDTO checkout(Long userId) {
        userModule.validateUserExists(userId);

        Cart cart = findCartByUser(userId);

        validateCartEmpty(cart);

        List<CheckoutEvent.CheckoutItem> checkoutItems = cart.getItems().stream()
                .map(item -> {
                    ProductDTO product = productModule.validateProductStock(item.getProductId(), item.getQuantity());

                    return new CheckoutEvent.CheckoutItem(
                            item.getProductId(),
                            item.getQuantity(),
                            product.priceAmount()
                    );
                })
                .toList();

        // Calculate total amount
        BigDecimal totalAmount = checkoutItems.stream()
                .map(item -> item.unitPrice().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Publish checkout event
        CheckoutEvent event = new CheckoutEvent(
                cart.getId(),
                userId,
                checkoutItems,
                totalAmount,
                LocalDateTime.now()
        );

        eventPublisher.publishEvent(event);

        CartDTO checkoutCart = buildCartDTO(cart);
        // Clean cart itens (orphanRemoval = true will handle the deletion)
        cart.getItems().clear();

        cart.setUpdatedAt(LocalDateTime.now());

        repository.save(cart);

        return checkoutCart;
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
