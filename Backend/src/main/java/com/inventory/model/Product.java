package com.inventory.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String sku;
    @NotBlank
    public String name;
    public String description;

    @ManyToOne
    @JoinColumn(name = "supplier_id")
    public Supplier supplier;

    public Double unitPrice;
    public Integer reorderPoint;
    public Integer targetStock;

    public Product() {}
}
 
