package com.inventory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.inventory.model.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
 
