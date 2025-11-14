package com.inventory.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.inventory.model.Order;
import com.inventory.model.OrderItem;
import com.inventory.repository.OrderRepository;
import com.inventory.repository.StockRepository;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final StockService stockService;
    private final StockRepository stockRepository;

    public OrderService(OrderRepository orderRepository, StockService stockService, StockRepository stockRepository) {
        this.orderRepository = orderRepository;
        this.stockService = stockService;
        this.stockRepository = stockRepository;
    }

    @Transactional
    public Order createOrder(Order o) {
        // reduce stock for each item
        if (o.items != null) {
            for (OrderItem it : o.items) {
                stockService.reduceStock(it.product.id, it.quantity == null ? 0 : it.quantity);
            }
        }
        o.orderDate = java.time.LocalDateTime.now();
        o.status = "PLACED";
        return orderRepository.save(o);
    }
}
 
