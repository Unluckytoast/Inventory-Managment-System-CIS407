package com.inventory.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.inventory.model.Customer;
import com.inventory.repository.CustomerRepository;

@Service
public class CustomerService {
    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Optional<Customer> loginByEmail(String email) {
        return customerRepository.findByEmail(email);
    }

    public Customer addOrUpdate(Customer customer) {
        return customerRepository.save(customer);
    }
}
