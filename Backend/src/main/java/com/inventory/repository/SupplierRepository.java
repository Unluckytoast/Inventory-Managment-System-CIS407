package com.inventory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.inventory.model.Supplier;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
}
 
