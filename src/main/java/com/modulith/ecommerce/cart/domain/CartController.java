package com.modulith.ecommerce.cart.domain;

import com.modulith.ecommerce.auth.AuthModuleAPI;
import com.modulith.ecommerce.cart.AddCartItemDTO;
import com.modulith.ecommerce.cart.CartDTO;
import com.modulith.ecommerce.common.PaymentMethod;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/carts")
@Tag(name = "Carts", description = "Shopping cart management endpoints")
public class CartController {
    private final CartService service;

    private final AuthModuleAPI authModuleAPI;

    @PutMapping
    @Operation(summary = "Add or update item in cart")
    public CartDTO addOrUpdateItem(@RequestBody AddCartItemDTO cartData) {
        Long userId = authModuleAPI.getCurrentUserId();
        return service.addOrUpdateItem(userId, cartData);
    }

    @GetMapping
    @Operation(summary = "Get all carts with pagination")
    public List<CartDTO> getAllCarts(
            @ParameterObject
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return service.getAllCarts(pageable);
    }

    @GetMapping("/user")
    @Operation(summary = "Get current user's cart")
    public CartDTO getCurrentUserCart() {
        Long userId = authModuleAPI.getCurrentUserId();
        return service.getCartUserById(userId);
    }

    @PostMapping("/checkout")
    @Operation(summary = "Checkout current user's cart")
    public ResponseEntity<String> checkout(
            @RequestParam PaymentMethod paymentMethod
    ) {
        Long userId = authModuleAPI.getCurrentUserId();
        return service.checkout(userId, paymentMethod);
    }

}
