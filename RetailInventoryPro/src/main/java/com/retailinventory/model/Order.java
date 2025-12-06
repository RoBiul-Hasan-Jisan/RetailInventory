package com.retailinventory.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Order {
    private String orderId;
    private String customerId;
    private LocalDateTime orderDate;
    private List<OrderItem> items;
    private double totalAmount;
    private double discount;
    private double tax;
    private double finalAmount;
    private String status; // PENDING, PROCESSING, COMPLETED, CANCELLED
    private String paymentMethod;
    private String notes;
    private LocalDateTime completionDate;
    
    public Order() {
        this.orderId = "ORD" + System.currentTimeMillis() % 10000;
        this.orderDate = LocalDateTime.now();
        this.items = new ArrayList<>();
        this.status = "PENDING";
        this.paymentMethod = "Cash";
    }
    
    public void addItem(OrderItem item) {
        items.add(item);
        calculateTotals();
    }
    
    public void removeItem(OrderItem item) {
        items.remove(item);
        calculateTotals();
    }
    
    public void calculateTotals() {
        totalAmount = items.stream()
            .mapToDouble(item -> item.getPrice() * item.getQuantity())
            .sum();
        
        tax = totalAmount * 0.08; // 8% tax
        finalAmount = totalAmount + tax - discount;
    }
    
    public int getTotalItems() {
        return items.stream()
            .mapToInt(OrderItem::getQuantity)
            .sum();
    }
    
    // Getters and Setters
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    
    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
    
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { 
        this.items = items; 
        calculateTotals();
    }
    
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    
    public double getDiscount() { return discount; }
    public void setDiscount(double discount) { 
        this.discount = discount; 
        calculateTotals();
    }
    
    public double getTax() { return tax; }
    public void setTax(double tax) { 
        this.tax = tax; 
        calculateTotals();
    }
    
    public double getFinalAmount() { return finalAmount; }
    public void setFinalAmount(double finalAmount) { this.finalAmount = finalAmount; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public LocalDateTime getCompletionDate() { return completionDate; }
    public void setCompletionDate(LocalDateTime completionDate) { this.completionDate = completionDate; }
    
    @Override
    public String toString() {
        return String.format("Order #%s - %s - $%.2f", orderId, orderDate, finalAmount);
    }
}