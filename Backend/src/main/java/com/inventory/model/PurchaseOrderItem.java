package com.inventory.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "purchase_order_items")
public class PurchaseOrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne
    @JoinColumn(name = "purchase_order_id")
    public PurchaseOrder purchaseOrder;

    @ManyToOne
    @JoinColumn(name = "product_id")
    public Product product;

    public Integer quantity;
    public Double unitPrice;

    public PurchaseOrderItem() {}
}
 
