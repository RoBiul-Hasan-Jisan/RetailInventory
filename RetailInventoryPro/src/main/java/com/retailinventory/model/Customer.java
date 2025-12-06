package com.retailinventory.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Customer {
    private String customerId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
    private LocalDate joinDate;
    private LocalDateTime lastPurchase;
    private double totalPurchases;
    private int loyaltyPoints;
    private String customerType; // Regular, VIP, Wholesale
    
    public Customer() {
        this.customerId = "CUST" + System.currentTimeMillis() % 10000;
        this.joinDate = LocalDate.now();
        this.customerType = "Regular";
        this.loyaltyPoints = 0;
    }
    
    public Customer(String firstName, String lastName, String email, String phone) {
        this();
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
    }
    
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    public void addPurchase(double amount) {
        this.totalPurchases += amount;
        this.lastPurchase = LocalDateTime.now();
        this.loyaltyPoints += (int)(amount / 10); // 1 point per $10
        updateCustomerType();
    }
    
    private void updateCustomerType() {
        if (totalPurchases >= 10000) {
            customerType = "VIP";
        } else if (totalPurchases >= 5000) {
            customerType = "Premium";
        } else if (totalPurchases >= 1000) {
            customerType = "Regular";
        }
    }
    
    public double getDiscountRate() {
        switch (customerType) {
            case "VIP": return 0.15;
            case "Premium": return 0.10;
            case "Regular": return 0.05;
            default: return 0.0;
        }
    }
    
    // Getters and Setters
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public LocalDate getJoinDate() { return joinDate; }
    public void setJoinDate(LocalDate joinDate) { this.joinDate = joinDate; }
    
    public LocalDateTime getLastPurchase() { return lastPurchase; }
    public void setLastPurchase(LocalDateTime lastPurchase) { this.lastPurchase = lastPurchase; }
    
    public double getTotalPurchases() { return totalPurchases; }
    public void setTotalPurchases(double totalPurchases) { this.totalPurchases = totalPurchases; }
    
    public int getLoyaltyPoints() { return loyaltyPoints; }
    public void setLoyaltyPoints(int loyaltyPoints) { this.loyaltyPoints = loyaltyPoints; }
    
    public String getCustomerType() { return customerType; }
    public void setCustomerType(String customerType) { this.customerType = customerType; }
    
    @Override
    public String toString() {
        return String.format("%s %s (%s)", firstName, lastName, customerId);
    }
}