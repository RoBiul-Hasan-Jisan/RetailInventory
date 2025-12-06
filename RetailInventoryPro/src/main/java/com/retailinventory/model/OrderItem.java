package com.retailinventory.model;

public class OrderItem {
    private String productId;
    private String productName;
    private double price;
    private int quantity;
    private double discount;
    
    public OrderItem() {}
    
    public OrderItem(String productId, String productName, double price, int quantity) {
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
    }
    
    public double getSubtotal() {
        return price * quantity * (1 - discount);
    }
    
    // Getters and Setters
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    
    public double getDiscount() { return discount; }
    public void setDiscount(double discount) { this.discount = discount; }
    
    @Override
    public String toString() {
        return String.format("%s x%d @ $%.2f = $%.2f", 
            productName, quantity, price, getSubtotal());
    }
}