package com.inventory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.inventory.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
 
