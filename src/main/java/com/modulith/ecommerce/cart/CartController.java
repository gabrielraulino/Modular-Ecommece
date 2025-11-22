package com.modulith.ecommerce.cart;

import com.modulith.ecommerce.payment.PaymentMethod;
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

    @PutMapping
    @Operation(summary = "Add or update item in cart")
    public CartDTO addOrUpdateItem(@RequestBody AddCartItemDTO cartData) {
        return service.addOrUpdateItem(cartData);
    }

    @GetMapping
    @Operation(summary = "Get all carts with pagination")
    public List<CartDTO> getAllCarts(
            @ParameterObject
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return service.getAllCarts(pageable);
    }

    @GetMapping("/user/{id}")
    public CartDTO getCartByUserId(@PathVariable Long id) {
        return service.getCartUserById(id);
    }

    @PostMapping("/user/{userId}/checkout")
    public ResponseEntity<String> checkout(
            @PathVariable Long userId,
            @RequestParam PaymentMethod paymentMethod
    ) {
        return service.checkout(userId, paymentMethod);
    }

}
