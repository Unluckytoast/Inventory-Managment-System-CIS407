package com.inventory.repository;

import com.inventory.model.Customer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {"spring.sql.init.mode=never", "spring.jpa.hibernate.ddl-auto=create-drop", "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"})
public class CustomerRepositoryTests {

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void createCustomer() {
        Customer c = new Customer();
        c.name = "Alice";
        c.email = "alice@example.com";
        c.phone = "555-0001";
        c.address = "123 Main St";

        Customer saved = customerRepository.save(c);
        assertThat(saved).isNotNull();
        assertThat(saved.id).isNotNull();
    }

    @Test
    void getCustomerById() {
        Customer c = new Customer();
        c.name = "Bob";
        c.email = "bob@example.com";
        Customer saved = customerRepository.save(c);

        var found = customerRepository.findById(saved.id);
        assertThat(found).isPresent();
        assertThat(found.get().name).isEqualTo("Bob");
    }

    @Test
    void getAllCustomers() {
        customerRepository.deleteAll();
        Customer a = new Customer(); a.name = "C1"; customerRepository.save(a);
        Customer b = new Customer(); b.name = "C2"; customerRepository.save(b);

        List<Customer> all = customerRepository.findAll();
        assertThat(all).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void updateCustomer() {
        Customer c = new Customer();
        c.name = "Dave";
        c.email = "dave@old.com";
        Customer saved = customerRepository.save(c);

        saved.email = "dave@new.com";
        Customer updated = customerRepository.save(saved);
        assertThat(updated.email).isEqualTo("dave@new.com");
    }

    @Test
    void deleteCustomer() {
        Customer c = new Customer();
        c.name = "Eve";
        Customer saved = customerRepository.save(c);

        customerRepository.deleteById(saved.id);
        var found = customerRepository.findById(saved.id);
        assertThat(found).isNotPresent();
    }
}
