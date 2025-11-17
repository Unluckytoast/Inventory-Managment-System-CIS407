package com.inventory.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.inventory.model.Order;
import com.inventory.service.OrderService;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<Long> createOrder(@RequestBody Order o) {
        // basic validation on items
        if (o.items != null) {
            for (var it : o.items) {
                if (it.product == null || it.product.id == null) {
                    throw new IllegalArgumentException("Missing product id in order item");
                }
                if (it.quantity == null || it.quantity <= 0) {
                    throw new IllegalArgumentException("Invalid quantity in order item");
                }
            }
        }
        Order saved = orderService.createOrder(o);
        return ResponseEntity.ok(saved.id);
    }
}
 
