package com.inventory.service;

import com.inventory.model.Product;
import com.inventory.model.Stock;
import com.inventory.model.Supplier;
import com.inventory.repository.ProductRepository;
import com.inventory.repository.PurchaseOrderRepository;
import com.inventory.repository.StockRepository;
import com.inventory.repository.SupplierRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.NoSuchElementException;

@DataJpaTest(properties = {"spring.sql.init.mode=never", "spring.jpa.hibernate.ddl-auto=create-drop", "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"})
public class StockServiceTests {

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Test
    void addStockForProduct() {
        Product p = new Product();
        p.sku = "STK-1";
        p.name = "Good";
        productRepository.save(p);

        Stock s = new Stock();
        s.product = p;
        s.quantity = 15;
        Stock saved = stockRepository.save(s);

        var found = stockRepository.findByProductId(p.id);
        assertThat(found).isPresent();
        assertThat(found.get().quantity).isEqualTo(15);
        assertThat(saved.id).isNotNull();
    }

    @Test
    void increaseStockManually() {
        Product p = new Product(); p.sku = "STK-2"; p.name = "IncProd"; productRepository.save(p);
        Stock s = new Stock(); s.product = p; s.quantity = 5; stockRepository.save(s);

        Stock toUpdate = stockRepository.findByProductId(p.id).orElseThrow();
        toUpdate.quantity = toUpdate.quantity + 4;
        Stock updated = stockRepository.save(toUpdate);

        assertThat(updated.quantity).isEqualTo(9);
    }

    @Test
    void decreaseStockManuallyViaService() {
        Product p = new Product(); p.sku = "STK-3"; p.name = "DecProd"; productRepository.save(p);
        Stock s = new Stock(); s.product = p; s.quantity = 10; stockRepository.save(s);

        StockService stockService = new StockService(stockRepository, productRepository, purchaseOrderRepository);
        Stock after = stockService.reduceStock(p.id, 3);

        assertThat(after.quantity).isEqualTo(7);
    }

    @Test
    void getStockByProduct() {
        Product p = new Product(); p.sku = "STK-4"; p.name = "GetProd"; productRepository.save(p);
        Stock s = new Stock(); s.product = p; s.quantity = 2; stockRepository.save(s);

        var found = stockRepository.findByProductId(p.id);
        assertThat(found).isPresent();
        assertThat(found.get().product.id).isEqualTo(p.id);
        assertThat(found.get().quantity).isEqualTo(2);
    }

    @Test
    void ensureStockCannotGoNegative() {
        Product p = new Product(); p.sku = "STK-5"; p.name = "NoNeg"; productRepository.save(p);
        Stock s = new Stock(); s.product = p; s.quantity = 2; stockRepository.save(s);

        StockService stockService = new StockService(stockRepository, productRepository, purchaseOrderRepository);
        Stock after = stockService.reduceStock(p.id, 10);

        assertThat(after.quantity).isGreaterThanOrEqualTo(0);
        assertThat(after.quantity).isEqualTo(0);
    }

    @Test
    void reduceStock_withNegativeQty_increasesStock() {
        Product p = new Product(); p.sku = "STK-6"; p.name = "NegProd"; productRepository.save(p);
        Stock s = new Stock(); s.product = p; s.quantity = 5; stockRepository.save(s);

        StockService stockService = new StockService(stockRepository, productRepository, purchaseOrderRepository);
        Stock after = stockService.reduceStock(p.id, -3);

        assertThat(after.quantity).isEqualTo(8);
    }

    @Test
    void reduceStock_withZeroQty_isNoOp() {
        Product p = new Product(); p.sku = "STK-7"; p.name = "ZeroProd"; productRepository.save(p);
        Stock s = new Stock(); s.product = p; s.quantity = 7; stockRepository.save(s);

        StockService stockService = new StockService(stockRepository, productRepository, purchaseOrderRepository);
        Stock after = stockService.reduceStock(p.id, 0);

        assertThat(after.quantity).isEqualTo(7);
    }

    @Test
    void reduceStock_nonExistentProduct_throws() {
        StockService stockService = new StockService(stockRepository, productRepository, purchaseOrderRepository);
        assertThrows(NoSuchElementException.class, () -> stockService.reduceStock(-999L, 1));
    }

    @Test
    void reduceStock_triggersReorder_createsPurchaseOrder() {
        Supplier sup = new Supplier(); sup.name = "Reorder Supplier"; supplierRepository.save(sup);

        Product p = new Product(); p.sku = "STK-8"; p.name = "ReorderProd"; p.supplier = sup; p.unitPrice = 2.5; p.reorderPoint = 10; p.targetStock = 20;
        productRepository.save(p);

        Stock s = new Stock(); s.product = p; s.quantity = 12; stockRepository.save(s);

        StockService stockService = new StockService(stockRepository, productRepository, purchaseOrderRepository);
        Stock after = stockService.reduceStock(p.id, 5); // leaves quantity 7 -> below reorderPoint

        List<?> pos = purchaseOrderRepository.findAll();
        assertThat(pos).isNotEmpty();

        var created = purchaseOrderRepository.findAll().get(0);
        assertThat(created).isNotNull();
        // items should have been created to fill to targetStock (20 - 7 = 13)
        assertThat(((com.inventory.model.PurchaseOrder) created).items).isNotNull();
        assertThat(((com.inventory.model.PurchaseOrder) created).items.get(0).quantity).isEqualTo(13);
    }

    @Test
    void reduceStock_belowReorderThreshold_createsPO_forLowStockScenario() {
        Supplier sup = new Supplier(); sup.name = "LowStock Supplier"; supplierRepository.save(sup);

        Product p = new Product();
        p.sku = "LS-100";
        p.name = "LowStockProd";
        p.supplier = sup;
        p.unitPrice = 4.0;
        p.reorderPoint = 5; // threshold
        p.targetStock = 10; // desired target
        productRepository.save(p);

        Stock s = new Stock(); s.product = p; s.quantity = 3; stockRepository.save(s);

        StockService stockService = new StockService(stockRepository, productRepository, purchaseOrderRepository);
        Stock after = stockService.reduceStock(p.id, 1); // leaves quantity 2 -> below reorderPoint

        var pos = purchaseOrderRepository.findAll();
        assertThat(pos).isNotEmpty();

        var created = purchaseOrderRepository.findAll().get(0);
        assertThat(created).isNotNull();
        assertThat(((com.inventory.model.PurchaseOrder) created).items).isNotNull();
        // expected order quantity = targetStock - resulting stock (10 - 2 = 8)
        assertThat(((com.inventory.model.PurchaseOrder) created).items.get(0).quantity).isEqualTo(8);
    }
}
