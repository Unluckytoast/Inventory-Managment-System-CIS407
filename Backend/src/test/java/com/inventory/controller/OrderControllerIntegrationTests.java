package com.inventory.controller;

import com.inventory.model.Order;
import com.inventory.model.OrderItem;
import com.inventory.model.Product;
import com.inventory.model.Stock;
import com.inventory.model.Customer;
import com.inventory.repository.OrderRepository;
import com.inventory.repository.OrderItemRepository;
import com.inventory.repository.ProductRepository;
import com.inventory.repository.StockRepository;
import com.inventory.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false",
        "spring.datasource.driver-class-name=org.h2.Driver"
    })
public class OrderControllerIntegrationTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Test
    void placeOrder_createsOrderItems_and_decreasesStock() {
        // create customer
        Customer c = new Customer();
        c.name = "OrderTestCust";
        customerRepository.save(c);

        // create product and stock
        Product p = new Product();
        p.sku = "ORD-SKU-1";
        p.name = "OrderProduct";
        productRepository.save(p);

        Stock s = new Stock();
        s.product = p;
        s.quantity = 30;
        stockRepository.save(s);

        // build a minimal JSON payload (no bidirectional links) to avoid Jackson recursion
        var itemPayload = new java.util.HashMap<String, Object>();
        var productRef = new java.util.HashMap<String, Object>();
        productRef.put("id", p.id);
        itemPayload.put("product", productRef);
        itemPayload.put("quantity", 5);
        var itemsList = new java.util.ArrayList<java.util.Map<String, Object>>();
        itemsList.add(itemPayload);

        var payload = new java.util.HashMap<String, Object>();
        var customerRef = new java.util.HashMap<String, Object>();
        customerRef.put("id", c.id);
        payload.put("customer", customerRef);
        payload.put("items", itemsList);

        ResponseEntity<String> resp = restTemplate.postForEntity("/api/orders", payload, String.class);
        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();

        // verify order persisted with item by checking repository
        var allOrders = orderRepository.findAll();
        assertThat(allOrders).isNotEmpty();

        // verify order_items persisted
        assertThat(orderItemRepository.findAll()).isNotEmpty();

        // verify stock decreased by 5
        var stockAfter = stockRepository.findByProductId(p.id).orElseThrow();
        assertThat(stockAfter.quantity).isEqualTo(25);
    }

    @Test
    void placeMultiItemOrder_createsMultipleOrderItems_and_decreasesEachStock() {
        // create customer
        Customer c = new Customer();
        c.name = "MultiItemCust";
        customerRepository.save(c);

        // create two products and stock
        Product p1 = new Product(); p1.sku = "MULTI-1"; p1.name = "MultiProd1"; productRepository.save(p1);
        Stock s1 = new Stock(); s1.product = p1; s1.quantity = 10; stockRepository.save(s1);

        Product p2 = new Product(); p2.sku = "MULTI-2"; p2.name = "MultiProd2"; productRepository.save(p2);
        Stock s2 = new Stock(); s2.product = p2; s2.quantity = 8; stockRepository.save(s2);

        // build payload with two items
        var item1 = new java.util.HashMap<String, Object>();
        var prodRef1 = new java.util.HashMap<String, Object>(); prodRef1.put("id", p1.id);
        item1.put("product", prodRef1); item1.put("quantity", 3);

        var item2 = new java.util.HashMap<String, Object>();
        var prodRef2 = new java.util.HashMap<String, Object>(); prodRef2.put("id", p2.id);
        item2.put("product", prodRef2); item2.put("quantity", 1);

        var items = new java.util.ArrayList<java.util.Map<String, Object>>();
        items.add(item1); items.add(item2);

        var payload = new java.util.HashMap<String, Object>();
        var customerRef = new java.util.HashMap<String, Object>(); customerRef.put("id", c.id);
        payload.put("customer", customerRef);
        payload.put("items", items);

        ResponseEntity<String> resp = restTemplate.postForEntity("/api/orders", payload, String.class);
        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();

        // verify one order persisted
        var allOrders = orderRepository.findAll();
        assertThat(allOrders).isNotEmpty();

        // verify two order_items persisted
        var allItems = orderItemRepository.findAll();
        assertThat(allItems.size()).isGreaterThanOrEqualTo(2);

        // verify stocks decreased accordingly
        var after1 = stockRepository.findByProductId(p1.id).orElseThrow();
        var after2 = stockRepository.findByProductId(p2.id).orElseThrow();
        assertThat(after1.quantity).isEqualTo(7); // 10 - 3
        assertThat(after2.quantity).isEqualTo(7); // 8 - 1
    }
}
