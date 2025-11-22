package com.modulith.ecommerce.order.domain;

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

    @GetMapping("/all")
    @Operation(summary = "Get all orders with pagination")
    public List<OrderDTO> findAllOrders(
            @ParameterObject
            @PageableDefault(size = 20, sort = "id") Pageable pageable){
        return orderService.getAllOrders(pageable);
    }

    @GetMapping("/{id}")
    public OrderDTO findById(@PathVariable Long id){
        return orderService.findById(id);
    }

    @GetMapping("/user/{id}")
    public List<OrderDTO> findByUserId(@PathVariable Long id){
        return orderService.findByUserId(id);
    }

    @PostMapping("/{id}/cancel")
    public OrderDTO cancelOrder(@PathVariable Long id) {
        return orderService.cancelOrder(id);
    }
}
