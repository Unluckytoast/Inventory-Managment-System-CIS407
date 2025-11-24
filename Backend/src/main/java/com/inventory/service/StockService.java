package com.inventory.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.inventory.model.Product;
import com.inventory.model.PurchaseOrder;
import com.inventory.model.PurchaseOrderItem;
import com.inventory.model.Stock;
import com.inventory.repository.ProductRepository;
import com.inventory.repository.PurchaseOrderRepository;
import com.inventory.repository.StockRepository;

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

    public List<Map<String, Object>> getProductsWithStock() {
        List<Product> products = productRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Product p : products) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", p.id);
            item.put("name", p.name);
            item.put("description", p.description);
            item.put("sku", p.sku);
            item.put("unitPrice", p.unitPrice);
            item.put("reorderPoint", p.reorderPoint);
            item.put("targetStock", p.targetStock);
            item.put("supplier", p.supplier);
            
            Stock stock = stockRepository.findByProductId(p.id).orElse(null);
            item.put("stockQuantity", stock != null ? stock.quantity : 0);
            
            result.add(item);
        }
        
        return result;
    }

    public List<Map<String, Object>> getProductsBelowReorderPoint() {
        List<Product> products = productRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Product p : products) {
            if (p.reorderPoint != null) {
                Stock stock = stockRepository.findByProductId(p.id).orElse(null);
                int currentStock = stock != null ? stock.quantity : 0;
                
                if (currentStock < p.reorderPoint) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", p.id);
                    item.put("name", p.name);
                    item.put("sku", p.sku);
                    item.put("unitPrice", p.unitPrice);
                    item.put("reorderPoint", p.reorderPoint);
                    item.put("targetStock", p.targetStock);
                    item.put("currentStock", currentStock);
                    item.put("supplier", p.supplier);
                    item.put("recommendedOrder", p.targetStock != null ? Math.max(0, p.targetStock - currentStock) : 0);
                    
                    result.add(item);
                }
            }
        }
        
        return result;
    }

    @Transactional
    public Stock updateStock(Long productId, Integer quantity) {
        Product p = productRepository.findById(productId).orElseThrow();
        Stock s = stockRepository.findByProductId(productId).orElseGet(() -> {
            Stock ns = new Stock();
            ns.product = p;
            ns.quantity = 0;
            return ns;
        });
        s.quantity = quantity;
        return stockRepository.save(s);
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

    @Transactional
    public Stock addStock(Long productId, int qty) {
        Product p = productRepository.findById(productId).orElseThrow();
        Stock s = stockRepository.findByProductId(productId).orElseGet(() -> {
            Stock ns = new Stock();
            ns.product = p;
            ns.quantity = 0;
            return ns;
        });
        s.quantity = s.quantity + qty;
        return stockRepository.save(s);
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
 
