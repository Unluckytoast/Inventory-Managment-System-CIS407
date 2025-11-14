package com.inventory.model;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "purchase_orders")
public class PurchaseOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne
    @JoinColumn(name = "supplier_id")
    public Supplier supplier;

    public LocalDateTime createdDate;
    public String status;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "purchaseOrder")
    public List<PurchaseOrderItem> items;

    public PurchaseOrder() {}
}
 
