package com.inventory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.inventory.model.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
