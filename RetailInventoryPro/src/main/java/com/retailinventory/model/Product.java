package com.retailinventory.model;

import java.io.Serializable;
import java.time.LocalDate;

public class Product implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String productId;
    private String barcode;
    private String name;
    private String category;
    private String description;
    private double purchasePrice;
    private double sellingPrice;
    private int quantityInStock;
    private int minStockLevel;
    private int maxStockLevel;
    private String supplierId;
    private LocalDate expiryDate;
    private String location;
    private boolean isPerishable;
    private String unit;
    private double weight;
    private String imagePath;
    private LocalDate lastRestocked;
    private int quantitySold;
    
    public Product() {
        this.productId = "PROD" + System.currentTimeMillis() % 10000;
        this.barcode = "590" + String.format("%010d", (int)(Math.random() * 1000000000L));
        this.quantityInStock = 0;
        this.minStockLevel = 10;
        this.maxStockLevel = 100;
        this.quantitySold = 0;
    }
    
    public Product(String name, String category, double purchasePrice, double sellingPrice, int quantity) {
        this();
        this.name = name;
        this.category = category;
        this.purchasePrice = purchasePrice;
        this.sellingPrice = sellingPrice;
        this.quantityInStock = quantity;
    }
    
    // Business methods
    public boolean needsReorder() {
        return quantityInStock <= minStockLevel;
    }
    
    public double calculateProfit() {
        return sellingPrice - purchasePrice;
    }
    
    public double calculateTotalProfit() {
        return calculateProfit() * quantitySold;
    }
    
    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }
    
    public boolean willExpireSoon(int days) {
        if (expiryDate == null) return false;
        LocalDate threshold = LocalDate.now().plusDays(days);
        return expiryDate.isBefore(threshold) && !isExpired();
    }
    
    public double getStockValue() {
        return quantityInStock * purchasePrice;
    }
    
    public double getPotentialRevenue() {
        return quantityInStock * sellingPrice;
    }
    
    // Getters and Setters
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    
    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public double getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(double purchasePrice) { 
        if (purchasePrice < 0) throw new IllegalArgumentException("Price cannot be negative");
        this.purchasePrice = purchasePrice; 
    }
    
    public double getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(double sellingPrice) { 
        if (sellingPrice < purchasePrice) throw new IllegalArgumentException("Selling price must be >= purchase price");
        this.sellingPrice = sellingPrice; 
    }
    
    public int getQuantityInStock() { return quantityInStock; }
    public void setQuantityInStock(int quantityInStock) { 
        if (quantityInStock < 0) throw new IllegalArgumentException("Quantity cannot be negative");
        this.quantityInStock = quantityInStock; 
    }
    
    public int getMinStockLevel() { return minStockLevel; }
    public void setMinStockLevel(int minStockLevel) { this.minStockLevel = minStockLevel; }
    
    public int getMaxStockLevel() { return maxStockLevel; }
    public void setMaxStockLevel(int maxStockLevel) { this.maxStockLevel = maxStockLevel; }
    
    public String getSupplierId() { return supplierId; }
    public void setSupplierId(String supplierId) { this.supplierId = supplierId; }
    
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    public boolean isPerishable() { return isPerishable; }
    public void setPerishable(boolean perishable) { isPerishable = perishable; }
    
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    
    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }
    
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    
    public LocalDate getLastRestocked() { return lastRestocked; }
    public void setLastRestocked(LocalDate lastRestocked) { this.lastRestocked = lastRestocked; }
    
    public int getQuantitySold() { return quantitySold; }
    public void setQuantitySold(int quantitySold) { this.quantitySold = quantitySold; }
    
    @Override
    public String toString() {
        return String.format("%s - %s (Stock: %d, Price: $%.2f)", 
            productId, name, quantityInStock, sellingPrice);
    }
}