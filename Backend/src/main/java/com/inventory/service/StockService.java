package com.inventory.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.inventory.model.Product;
import com.inventory.model.PurchaseOrder;
import com.inventory.model.PurchaseOrderItem;
import com.inventory.model.Stock;
import com.inventory.repository.ProductRepository;
import com.inventory.repository.PurchaseOrderRepository;
import com.inventory.repository.StockRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Service
public class StockService {
    private final StockRepository stockRepository;
    private final ProductRepository productRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;

    public StockService(StockRepository stockRepository, ProductRepository productRepository,
                        PurchaseOrderRepository purchaseOrderRepository) {
        this.stockRepository = stockRepository;
        this.productRepository = productRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
    }

    @Transactional
    public Stock reduceStock(Long productId, int qty) {
        Product p = productRepository.findById(productId).orElseThrow();
        Stock s = stockRepository.findByProductId(productId).orElseGet(() -> {
            Stock ns = new Stock();
            ns.product = p;
            ns.quantity = 0;
            return ns;
        });
        s.quantity = Math.max(0, s.quantity - qty);
        Stock saved = stockRepository.save(s);
        checkAndReorderIfNeeded(p, saved);
        return saved;
    }

    private void checkAndReorderIfNeeded(Product p, Stock s) {
        if (p.reorderPoint != null && s.quantity < p.reorderPoint) {
            int toOrder = 0;
            if (p.targetStock != null) {
                toOrder = Math.max(0, p.targetStock - s.quantity);
            }
            if (toOrder > 0) {
                PurchaseOrder po = new PurchaseOrder();
                po.supplier = p.supplier;
                po.createdDate = LocalDateTime.now();
                po.status = "CREATED";
                PurchaseOrderItem poi = new PurchaseOrderItem();
                poi.product = p;
                poi.quantity = toOrder;
                poi.unitPrice = p.unitPrice;
                po.items = new ArrayList<>();
                po.items.add(poi);
                // persist PO (cascade will persist items)
                purchaseOrderRepository.save(po);
            }
        }
    }
}
 
