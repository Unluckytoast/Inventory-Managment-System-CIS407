package com.inventory.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.inventory.model.Order;
import com.inventory.model.OrderItem;
import com.inventory.repository.OrderRepository;
import com.inventory.repository.StockRepository;
import com.inventory.repository.CustomerRepository;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final StockService stockService;
    private final StockRepository stockRepository;
    private final CustomerRepository customerRepository;

    public OrderService(OrderRepository orderRepository, StockService stockService, StockRepository stockRepository, CustomerRepository customerRepository) {
        this.orderRepository = orderRepository;
        this.stockService = stockService;
        this.stockRepository = stockRepository;
        this.customerRepository = customerRepository;
    }

    @Transactional
    public Order createOrder(Order o) {
        // reduce stock for each item
        if (o.items != null) {
            for (OrderItem it : o.items) {
                stockService.reduceStock(it.product.id, it.quantity == null ? 0 : it.quantity);
            }
        }
        // validate customer exists if provided
        if (o.customer != null && o.customer.id != null) {
            customerRepository.findById(o.customer.id).orElseThrow();
        }
        o.orderDate = java.time.LocalDateTime.now();
        o.status = "PLACED";
        return orderRepository.save(o);
    }
}
 
