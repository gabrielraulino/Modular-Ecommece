package com.modulith.ecommerce.cart;


import com.modulith.ecommerce.event.CheckoutEvent;
import com.modulith.ecommerce.product.ProductDTO;
import com.modulith.ecommerce.product.ProductModuleAPI;
import com.modulith.ecommerce.user.UserDTO;
import com.modulith.ecommerce.user.UserModuleAPI;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CartService {
    private final CartRepository repository;

    private final ProductModuleAPI productModule;

    private final UserModuleAPI userModule;

    private final ApplicationEventPublisher eventPublisher;

    public List<CartDTO> getAllCarts(){
        return repository.findAll().stream()
                .map(this::buildCartDTO)
                .collect(Collectors.toList());
    }

    public CartDTO getCartUserById(Long id){
        Cart cart = repository.findCartByUserId(id)
                .orElseThrow(() -> new RuntimeException("Cart not found with id: " + id));
        return buildCartDTO(cart);
    }

    public CartDTO addItem(addCartItemDTO cartData){
        Optional<UserDTO> user = userModule.findUserById(cartData.userId());
        Optional<ProductDTO> productDTO = productModule.findProductById(cartData.productId());
        if (user.isEmpty()) {
            throw new RuntimeException("User not found with id: " + cartData.userId());
        }
        if (productDTO.isEmpty()) {
            throw new RuntimeException("Product not found with id: " + cartData.productId());
        }

        Cart cart = repository.findCartByUserId(cartData.userId()).orElseGet(() ->{
            Cart newCart = new Cart(null, cartData.userId(), LocalDateTime.now(), null);
            return repository.save(newCart);
        });

        Optional<CartItem> existingItemOpt = cart.getItems().stream().filter(item -> item.getProductId().equals(cartData.productId())).findFirst();

        if (existingItemOpt.isPresent()) {
            CartItem existingItem = existingItemOpt.get();
            CartItem updatedItem = existingItem.updateQuantity(existingItem.getQuantity() + cartData.quantity());
            cart.removeItem(existingItem);
            cart.addItem(updatedItem);
        } else {
            CartItem newItem = new CartItem(cart, cartData.productId(), cartData.quantity());
            cart.addItem(newItem);
        }

        // Atualizar o updatedAt
        cart.setUpdatedAt(LocalDateTime.now());

        Cart savedCart = repository.save(cart);
        return buildCartDTO(savedCart);
    }

    private CartDTO buildCartDTO(Cart cart) {
        List<CartItemDTO> enrichedItems = cart.getItems().stream()
                .map(this::buildCartItemDTO)
                .collect(Collectors.toList());

        // Calcular total de quantidade
        int totalQuantity = enrichedItems.stream()
                .mapToInt(CartItemDTO::quantity)
                .sum();

        // Calcular preço total
        BigDecimal totalPrice = enrichedItems.stream()
                .map(CartItemDTO::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Pegar a moeda do primeiro ‘item’ (assumindo que todos são da mesma moeda)
        String currency = enrichedItems.isEmpty() ? "USD" : enrichedItems.getFirst().priceCurrency();

        return new CartDTO(
                cart.getId(),
                cart.getUserId(),
                enrichedItems,
                totalQuantity,
                totalPrice,
                currency,
                cart.getCreatedAt(),
                cart.getUpdatedAt()
        );
    }

    private CartItemDTO buildCartItemDTO(CartItem item) {
        Optional<ProductDTO> productOpt = productModule.findProductById(item.getProductId());

        if (productOpt.isEmpty()) {
            throw new RuntimeException("Product not found with id: " + item.getProductId());
        }

        ProductDTO product = productOpt.get();
        BigDecimal subtotal = product.priceAmount().multiply(BigDecimal.valueOf(item.getQuantity()));

        return new CartItemDTO(
                item.getId(),
                item.getProductId(),
                product.name(),
                product.priceAmount(),
                product.priceCurrency(),
                item.getQuantity(),
                subtotal
        );
    }

    /**
     * Realiza o checkout do carrinho, publicando um evento para o módulo de pedidos.
     * Seguindo as práticas do Spring Modulith, este método:
     * 1. Valida o carrinho e verifica se há itens
     * 2. Publica um CheckoutEvent com todos os dados necessários
     * 3. Limpa o carrinho após o checkout
     *
     * @param userId ID do usuário que está fazendo o checkout
     * @return CartDTO vazio após o checkout
     */
    @Transactional
    public CartDTO checkout(Long userId) {
        // Buscar o carrinho do usuário
        Cart cart = repository.findCartByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found for user: " + userId));

        // Validar se o carrinho tem itens
        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cannot checkout an empty cart");
        }

        // Verificar se o usuário existe
        userModule.findUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Construir os itens do checkout com informações dos produtos
        List<CheckoutEvent.CheckoutItem> checkoutItems = cart.getItems().stream()
                .map(item -> {
                    ProductDTO product = productModule.findProductById(item.getProductId())
                            .orElseThrow(() -> new RuntimeException("Product not found with id: " + item.getProductId()));

                    return new CheckoutEvent.CheckoutItem(
                            item.getProductId(),
                            item.getQuantity(),
                            product.priceAmount(),
                            product.priceCurrency()
                    );
                })
                .collect(Collectors.toList());

        // Calcular o total
        BigDecimal totalAmount = checkoutItems.stream()
                .map(item -> item.unitPrice().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String currency = checkoutItems.isEmpty() ? "BRL" : checkoutItems.get(0).currency();

        // Publicar o evento de checkout
        CheckoutEvent event = new CheckoutEvent(
                cart.getId(),
                userId,
                checkoutItems,
                totalAmount,
                currency,
                LocalDateTime.now()
        );

        eventPublisher.publishEvent(event);

        // Limpar os itens do carrinho (orphanRemoval = true garantirá a exclusão do banco)
        cart.getItems().clear();
        
        // Atualizar o updatedAt
        cart.setUpdatedAt(LocalDateTime.now());
        
        // Salvar as alterações (itens serão excluídos do banco devido ao orphanRemoval)
        Cart clearedCart = repository.save(cart);

        return buildCartDTO(clearedCart);
    }

}
