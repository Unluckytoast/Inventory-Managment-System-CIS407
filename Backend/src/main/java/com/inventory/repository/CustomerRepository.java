package com.inventory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.inventory.model.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
 
