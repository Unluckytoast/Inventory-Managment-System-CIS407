package com.inventory.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.inventory.model.PurchaseOrder;
import com.inventory.model.PurchaseOrderItem;
import com.inventory.repository.PurchaseOrderRepository;
import com.inventory.service.StockService;

@RestController
@RequestMapping("/api/purchase-orders")
@CrossOrigin(origins = "*")
public class PurchaseOrderController {
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final StockService stockService;

    public PurchaseOrderController(PurchaseOrderRepository purchaseOrderRepository, StockService stockService) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.stockService = stockService;
    }

    @GetMapping
    public ResponseEntity<List<PurchaseOrder>> getAllPurchaseOrders() {
        List<PurchaseOrder> orders = purchaseOrderRepository.findAll();
        return ResponseEntity.ok(orders);
    }

    @PostMapping
    public ResponseEntity<PurchaseOrder> createPurchaseOrder(@RequestBody PurchaseOrder purchaseOrder) {
        // Set the bidirectional relationship for items
        if (purchaseOrder.items != null) {
            for (PurchaseOrderItem item : purchaseOrder.items) {
                item.purchaseOrder = purchaseOrder;
            }
        }
        PurchaseOrder saved = purchaseOrderRepository.save(purchaseOrder);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/{id}/receive")
    public ResponseEntity<PurchaseOrder> receivePurchaseOrder(@PathVariable Long id) {
        PurchaseOrder order = purchaseOrderRepository.findById(id).orElseThrow();
        
        // Update stock for each item
        if (order.items != null) {
            for (PurchaseOrderItem item : order.items) {
                if (item.product != null && item.product.id != null && item.quantity != null) {
                    stockService.addStock(item.product.id, item.quantity);
                }
            }
        }
        
        // Update order status
        order.status = "RECEIVED";
        PurchaseOrder updated = purchaseOrderRepository.save(order);
        return ResponseEntity.ok(updated);
    }
}
