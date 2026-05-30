package com.customerapp.service;

import com.customerapp.model.Customer;
import com.customerapp.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CustomerService {
    
    @Autowired
    private CustomerRepository customerRepository;
    
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }
    
    public Page<Customer> getAllCustomers(Pageable pageable) {
        return customerRepository.findAll(pageable);
    }
    
    public Page<Customer> searchCustomers(String search, Pageable pageable) {
        if (search == null || search.trim().isEmpty()) {
            return customerRepository.findAll(pageable);
        }
        return customerRepository.searchCustomers(search.trim(), pageable);
    }
    
    public Optional<Customer> getCustomerById(Integer id) {
        return customerRepository.findById(id);
    }
    
    public Customer createCustomer(Customer customer) {
        if (customerRepository.existsByEmail(customer.getEmail())) {
            throw new IllegalArgumentException("Customer with email " + customer.getEmail() + " already exists");
        }
        return customerRepository.save(customer);
    }
    
    public Customer updateCustomer(Integer id, Customer customerDetails) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with id: " + id));
        
        if (!customer.getEmail().equals(customerDetails.getEmail()) && 
            customerRepository.existsByEmail(customerDetails.getEmail())) {
            throw new IllegalArgumentException("Customer with email " + customerDetails.getEmail() + " already exists");
        }
        
        customer.setName(customerDetails.getName());
        customer.setEmail(customerDetails.getEmail());
        customer.setPhone(customerDetails.getPhone());
        
        return customerRepository.save(customer);
    }
    
    public void deleteCustomer(Integer id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with id: " + id));
        customerRepository.delete(customer);
    }
}
