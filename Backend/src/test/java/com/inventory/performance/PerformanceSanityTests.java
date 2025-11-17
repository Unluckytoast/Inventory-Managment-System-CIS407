package com.inventory.performance;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.inventory.model.Customer;
import com.inventory.model.Order;
import com.inventory.model.Product;
import com.inventory.repository.CustomerRepository;
import com.inventory.repository.OrderRepository;
import com.inventory.repository.ProductRepository;

@SpringBootTest
public class PerformanceSanityTests {

    @Autowired
    ProductRepository productRepository;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    CustomerRepository customerRepository;

    @Test
    void insertManyProducts_sanity() {
        int count = 100; // reasonable sanity check between 50-200
        List<Product> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Product p = new Product();
            p.sku = "PERF-" + i;
            p.name = "Perf Product " + i;
            p.description = "Performance test product " + i;
            p.unitPrice = 9.99;
            list.add(p);
        }
        productRepository.saveAll(list);

        List<Product> saved = productRepository.findAll();
        assertThat(saved.size()).isGreaterThanOrEqualTo(count);
    }

    @Test
    void listAllProducts_sanity() {
        // create a few products
        Product a = new Product();
        a.sku = "LIST-A";
        a.name = "List A";
        productRepository.save(a);

        Product b = new Product();
        b.sku = "LIST-B";
        b.name = "List B";
        productRepository.save(b);

        List<Product> all = productRepository.findAll();
        assertThat(all.size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    void listAllOrders_sanity() {
        // create a customer and a few orders
        Customer c = new Customer();
        c.name = "Perf Cust";
        c.email = "perf@example.com";
        customerRepository.save(c);

        int ordersToCreate = 5;
        for (int i = 0; i < ordersToCreate; i++) {
            Order o = new Order();
            o.customer = c;
            o.status = "NEW";
            orderRepository.save(o);
        }

        List<Order> all = orderRepository.findAll();
        assertThat(all.size()).isGreaterThanOrEqualTo(ordersToCreate);
    }
}
