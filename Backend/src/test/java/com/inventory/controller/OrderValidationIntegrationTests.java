package com.inventory.controller;

import com.inventory.model.Product;
import com.inventory.model.Stock;
import com.inventory.model.Customer;
import com.inventory.repository.ProductRepository;
import com.inventory.repository.StockRepository;
import com.inventory.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false",
        "spring.datasource.driver-class-name=org.h2.Driver"
    })
public class OrderValidationIntegrationTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StockRepository stockRepository;

    @Test
    void orderWithInvalidProduct_returns404() {
        Customer c = new Customer(); c.name = "ValCust"; customerRepository.save(c);

        var item = new java.util.HashMap<String,Object>();
        var prodRef = new java.util.HashMap<String,Object>(); prodRef.put("id", 9999);
        item.put("product", prodRef); item.put("quantity", 1);
        var items = new java.util.ArrayList<java.util.Map<String,Object>>(); items.add(item);

        var payload = new java.util.HashMap<String,Object>();
        var custRef = new java.util.HashMap<String,Object>(); custRef.put("id", c.id);
        payload.put("customer", custRef); payload.put("items", items);

        ResponseEntity<String> resp = restTemplate.postForEntity("/api/orders", payload, String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void orderWithInvalidCustomer_returns404() {
        Product p = new Product(); p.sku = "VAL-1"; p.name = "ValProd"; productRepository.save(p);
        Stock s = new Stock(); s.product = p; s.quantity = 5; stockRepository.save(s);

        var item = new java.util.HashMap<String,Object>();
        var prodRef = new java.util.HashMap<String,Object>(); prodRef.put("id", p.id);
        item.put("product", prodRef); item.put("quantity", 1);
        var items = new java.util.ArrayList<java.util.Map<String,Object>>(); items.add(item);

        var payload = new java.util.HashMap<String,Object>();
        var custRef = new java.util.HashMap<String,Object>(); custRef.put("id", 9999);
        payload.put("customer", custRef); payload.put("items", items);

        ResponseEntity<String> resp = restTemplate.postForEntity("/api/orders", payload, String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void orderWithNegativeQuantity_returns400() {
        Customer c = new Customer(); c.name = "NegQtyCust"; customerRepository.save(c);
        Product p = new Product(); p.sku = "NEG-1"; p.name = "NegProd"; productRepository.save(p);
        Stock s = new Stock(); s.product = p; s.quantity = 5; stockRepository.save(s);

        var item = new java.util.HashMap<String,Object>();
        var prodRef = new java.util.HashMap<String,Object>(); prodRef.put("id", p.id);
        item.put("product", prodRef); item.put("quantity", -1);
        var items = new java.util.ArrayList<java.util.Map<String,Object>>(); items.add(item);

        var payload = new java.util.HashMap<String,Object>();
        var custRef = new java.util.HashMap<String,Object>(); custRef.put("id", c.id);
        payload.put("customer", custRef); payload.put("items", items);

        ResponseEntity<String> resp = restTemplate.postForEntity("/api/orders", payload, String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void addProductMissingName_returns400() {
        var payload = new java.util.HashMap<String,Object>();
        payload.put("name", "");

        ResponseEntity<String> resp = restTemplate.postForEntity("/api/products", payload, String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
