package com.retailinventory.service;

import com.retailinventory.model.Order;
import com.retailinventory.model.OrderItem;
import com.retailinventory.model.Customer;
import com.retailinventory.exception.InventoryException;
import java.util.*;
import java.time.LocalDateTime;

public class OrderService {
    private Map<String, Order> orders;
    private InventoryService inventoryService;
    private FileDataService fileDataService;
    private CustomerService customerService;
    
    public OrderService() {
        this.orders = new HashMap<>();
        this.inventoryService = new InventoryService();
        this.fileDataService = new FileDataService();
        this.customerService = new CustomerService();
        loadOrders();
    }
    
    private void loadOrders() {
        try {
            List<Order> orderList = fileDataService.loadOrders();
            for (Order order : orderList) {
                orders.put(order.getOrderId(), order);
            }
        } catch (Exception e) {
            System.err.println("Error loading orders: " + e.getMessage());
        }
    }
    
    public Order createOrder(String customerId, List<OrderItem> items) throws InventoryException {
        Order order = new Order();
        order.setOrderId(generateOrderId());
        order.setCustomerId(customerId);
        order.setItems(new ArrayList<>(items));
        order.calculateTotals();
        order.setStatus("PENDING");
        
        // Validate stock availability
        for (OrderItem item : items) {
            Product product = inventoryService.getProduct(item.getProductId());
            if (product == null) {
                throw new InventoryException("Product not found: " + item.getProductId());
            }
            if (product.getQuantityInStock() < item.getQuantity()) {
                throw new InventoryException("Insufficient stock for: " + product.getName());
            }
        }
        
        // Update customer if exists
        Customer customer = customerService.getCustomer(customerId);
        if (customer != null) {
            customer.addPurchase(order.getFinalAmount());
            customerService.updateCustomer(customer);
        }
        
        orders.put(order.getOrderId(), order);
        
        try {
            fileDataService.saveOrder(order);
        } catch (Exception e) {
            orders.remove(order.getOrderId());
            throw new InventoryException("Failed to save order", e);
        }
        
        return order;
    }
    
    public void processOrder(String orderId) throws InventoryException {
        Order order = orders.get(orderId);
        if (order == null) {
            throw new InventoryException("Order not found: " + orderId);
        }
        
        if (!order.getStatus().equals("PENDING")) {
            throw new InventoryException("Order cannot be processed. Current status: " + order.getStatus());
        }
        
        // Process each item
        for (OrderItem item : order.getItems()) {
            inventoryService.sellProduct(
                item.getProductId(),
                item.getQuantity(),
                order.getCustomerId(),
                orderId
            );
        }
        
        order.setStatus("COMPLETED");
        order.setCompletionDate(LocalDateTime.now());
        
        try {
            fileDataService.updateOrder(order);
        } catch (Exception e) {
            throw new InventoryException("Failed to update order status", e);
        }
    }
    
    public void cancelOrder(String orderId) throws InventoryException {
        Order order = orders.get(orderId);
        if (order == null) {
            throw new InventoryException("Order not found: " + orderId);
        }
        
        if (order.getStatus().equals("COMPLETED")) {
            // Return items to inventory
            for (OrderItem item : order.getItems()) {
                inventoryService.returnProduct(
                    item.getProductId(),
                    item.getQuantity(),
                    "Order cancellation",
                    orderId
                );
            }
            
            // Update customer
            Customer customer = customerService.getCustomer(order.getCustomerId());
            if (customer != null) {
                customer.setTotalPurchases(customer.getTotalPurchases() - order.getFinalAmount());
                customerService.updateCustomer(customer);
            }
        }
        
        order.setStatus("CANCELLED");
        
        try {
            fileDataService.updateOrder(order);
        } catch (Exception e) {
            throw new InventoryException("Failed to cancel order", e);
        }
    }
    
    public Order getOrder(String orderId) {
        return orders.get(orderId);
    }
    
    public List<Order> getAllOrders() {
        return new ArrayList<>(orders.values());
    }
    
    public List<Order> getOrdersByCustomer(String customerId) {
        return orders.values().stream()
            .filter(order -> order.getCustomerId().equals(customerId))
            .sorted((o1, o2) -> o2.getOrderDate().compareTo(o1.getOrderDate()))
            .collect(Collectors.toList());
    }
    
    public List<Order> getOrdersByDateRange(LocalDateTime start, LocalDateTime end) {
        return orders.values().stream()
            .filter(order -> !order.getOrderDate().isBefore(start) && 
                            !order.getOrderDate().isAfter(end))
            .sorted((o1, o2) -> o2.getOrderDate().compareTo(o1.getOrderDate()))
            .collect(Collectors.toList());
    }
    
    public double getTotalSales(LocalDateTime start, LocalDateTime end) {
        return getOrdersByDateRange(start, end).stream()
            .filter(order -> order.getStatus().equals("COMPLETED"))
            .mapToDouble(Order::getFinalAmount)
            .sum();
    }
    
    public int getTotalItemsSold(LocalDateTime start, LocalDateTime end) {
        return getOrdersByDateRange(start, end).stream()
            .filter(order -> order.getStatus().equals("COMPLETED"))
            .mapToInt(Order::getTotalItems)
            .sum();
    }
    
    public Map<String, Double> getSalesByCategory(LocalDateTime start, LocalDateTime end) {
        Map<String, Double> salesByCategory = new HashMap<>();
        
        getOrdersByDateRange(start, end).stream()
            .filter(order -> order.getStatus().equals("COMPLETED"))
            .forEach(order -> {
                for (OrderItem item : order.getItems()) {
                    Product product = inventoryService.getProduct(item.getProductId());
                    if (product != null) {
                        String category = product.getCategory();
                        double amount = item.getSubtotal();
                        salesByCategory.put(category, 
                            salesByCategory.getOrDefault(category, 0.0) + amount);
                    }
                }
            });
        
        return salesByCategory;
    }
    
    private String generateOrderId() {
        return "ORD" + LocalDateTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + 
            String.format("%03d", new Random().nextInt(1000));
    }
}import java.util.stream.Collectors;
