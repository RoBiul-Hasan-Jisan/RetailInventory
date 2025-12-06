package com.retailinventory.service;

import com.retailinventory.model.Customer;
import com.retailinventory.exception.FileProcessingException;
import java.util.*;
import java.util.stream.Collectors;

public class CustomerService {
    private Map<String, Customer> customers;
    private FileDataService fileDataService;
    
    public CustomerService() {
        this.customers = new HashMap<>();
        this.fileDataService = new FileDataService();
        loadCustomers();
    }
    
    private void loadCustomers() {
        try {
            List<Customer> customerList = fileDataService.loadCustomers();
            for (Customer customer : customerList) {
                customers.put(customer.getCustomerId(), customer);
            }
        } catch (Exception e) {
            System.err.println("Error loading customers: " + e.getMessage());
        }
    }
    
    public void addCustomer(Customer customer) throws Exception {
        if (customers.containsKey(customer.getCustomerId())) {
            throw new Exception("Customer ID already exists: " + customer.getCustomerId());
        }
        
        customers.put(customer.getCustomerId(), customer);
        fileDataService.saveCustomer(customer);
    }
    
    public void updateCustomer(Customer customer) throws Exception {
        if (!customers.containsKey(customer.getCustomerId())) {
            throw new Exception("Customer not found: " + customer.getCustomerId());
        }
        
        customers.put(customer.getCustomerId(), customer);
        fileDataService.saveCustomer(customer);
    }
    
    public void deleteCustomer(String customerId) throws Exception {
        if (!customers.containsKey(customerId)) {
            throw new Exception("Customer not found: " + customerId);
        }
        
        customers.remove(customerId);
        // Note: In production, you might want to archive instead of delete
    }
    
    public Customer getCustomer(String customerId) {
        return customers.get(customerId);
    }
    
    public List<Customer> getAllCustomers() {
        return new ArrayList<>(customers.values());
    }
    
    public List<Customer> searchCustomers(String keyword) {
        String searchTerm = keyword.toLowerCase();
        return customers.values().stream()
            .filter(c -> 
                c.getFirstName().toLowerCase().contains(searchTerm) ||
                c.getLastName().toLowerCase().contains(searchTerm) ||
                c.getEmail().toLowerCase().contains(searchTerm) ||
                c.getPhone().contains(searchTerm) ||
                (c.getAddress() != null && c.getAddress().toLowerCase().contains(searchTerm)))
            .sorted(Comparator.comparing(Customer::getLastName))
            .collect(Collectors.toList());
    }
    
    public List<Customer> getTopCustomers(int limit) {
        return customers.values().stream()
            .sorted((c1, c2) -> Double.compare(c2.getTotalPurchases(), c1.getTotalPurchases()))
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    public Map<String, Integer> getCustomerCountByType() {
        Map<String, Integer> countByType = new HashMap<>();
        
        customers.values().forEach(customer -> {
            String type = customer.getCustomerType();
            countByType.put(type, countByType.getOrDefault(type, 0) + 1);
        });
        
        return countByType;
    }
    
    public double getTotalCustomerSpending() {
        return customers.values().stream()
            .mapToDouble(Customer::getTotalPurchases)
            .sum();
    }
}