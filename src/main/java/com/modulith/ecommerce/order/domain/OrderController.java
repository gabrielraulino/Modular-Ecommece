package com.modulith.ecommerce.order.domain;

import com.modulith.ecommerce.auth.AuthModuleAPI;
import com.modulith.ecommerce.order.OrderDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/orders")
@Tag(name = "Orders", description = "Order management endpoints")
public class OrderController {
    private final OrderService orderService;

    private final AuthModuleAPI authModuleAPI;

    @GetMapping("/all")
    @Operation(summary = "Get all orders with pagination")
    public List<OrderDTO> findAllOrders(
            @ParameterObject
            @PageableDefault(size = 20, sort = "id") Pageable pageable){
        return orderService.getAllOrders(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID")
    public OrderDTO findById(@PathVariable Long id){
        return orderService.findById(id);
    }

    @GetMapping("/user")
    @Operation(summary = "Get current user's orders")
    public List<OrderDTO> getCurrentUserOrders(){
        Long userId = authModuleAPI.getCurrentUserId();
        return orderService.findByUserId(userId);
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel order by ID")
    public OrderDTO cancelOrder(@PathVariable Long id) {
        Long userId = authModuleAPI.getCurrentUserId();
        return orderService.cancelOrder(id, userId);
    }
}
