package com.inventory.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.inventory.model.Stock;
import com.inventory.service.StockService;

@RestController
@RequestMapping("/api/stock")
@CrossOrigin(origins = "*")
public class StockController {
    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping("/available")
    public ResponseEntity<List<Map<String, Object>>> getAvailableProducts() {
        List<Map<String, Object>> products = stockService.getProductsWithStock();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/below-reorder")
    public ResponseEntity<List<Map<String, Object>>> getProductsBelowReorder() {
        List<Map<String, Object>> products = stockService.getProductsBelowReorderPoint();
        return ResponseEntity.ok(products);
    }

    @PostMapping("/update")
    public ResponseEntity<Stock> updateStock(@RequestBody StockUpdateRequest request) {
        Stock stock = stockService.updateStock(request.productId, request.quantity);
        return ResponseEntity.ok(stock);
    }

    public static class StockUpdateRequest {
        public Long productId;
        public Integer quantity;
    }
}
