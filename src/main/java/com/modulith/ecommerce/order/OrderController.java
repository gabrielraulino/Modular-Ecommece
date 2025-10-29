package com.modulith.ecommerce.order;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;

    @GetMapping("/all")
    public List<OrderDTO> findAllOrders(){
        return orderService.getAllOrders();
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
