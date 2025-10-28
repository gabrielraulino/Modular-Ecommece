package com.modulith.ecommerce.cart;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/carts")
public class CartController {
    private final CartService service;

    @PostMapping
    public CartDTO addOrUpdateItem(@RequestBody AddCartItemDTO cartData) {
        return service.addOrUpdateItem(cartData);
    }

    @GetMapping
    public List<CartDTO> getAllCarts() {
        return service.getAllCarts();
    }

    @GetMapping("/user/{id}")
    public CartDTO getCartByUserId(@PathVariable Long id) {
        return service.getCartUserById(id);
    }

    @PostMapping("/user/{userId}/checkout")
    public CartDTO checkout(@PathVariable Long userId) {
        return service.checkout(userId);
    }

    @PatchMapping("/{userId}/items")
    public CartDTO patchItem(
            @PathVariable Long userId,
            @RequestParam PatchCartItemDTO itemDTO
    ) {
        return service.updateItemQuantity(userId, itemDTO);
    }

}
