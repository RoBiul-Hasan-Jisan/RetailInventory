package com.retailinventory.exception;

public class InsufficientStockException extends InventoryException {
    private String productId;
    private int requested;
    private int available;
    
    public InsufficientStockException(String productId, int requested, int available) {
        super(String.format("Insufficient stock for product %s. Requested: %d, Available: %d", 
            productId, requested, available));
        this.productId = productId;
        this.requested = requested;
        this.available = available;
    }
    
    public String getProductId() { return productId; }
    public int getRequested() { return requested; }
    public int getAvailable() { return available; }
}