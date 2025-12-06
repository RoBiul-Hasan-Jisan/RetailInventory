package com.retailinventory.service;

import com.retailinventory.model.Product;
import com.retailinventory.exception.InventoryException;
import com.retailinventory.exception.InsufficientStockException;
import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class InventoryService {
    private Map<String, Product> inventory;
    private FileDataService fileDataService;
    
    public InventoryService() {
        this.inventory = new HashMap<>();
        this.fileDataService = new FileDataService();
        loadInventory();
    }
    
    private void loadInventory() {
        try {
            List<Product> products = fileDataService.loadProducts();
            for (Product product : products) {
                inventory.put(product.getProductId(), product);
            }
            System.out.println("Loaded " + inventory.size() + " products from file.");
        } catch (Exception e) {
            System.err.println("Error loading inventory: " + e.getMessage());
            createSampleInventory();
        }
    }
    
    public void addProduct(Product product) throws InventoryException {
        validateProduct(product);
        
        if (inventory.containsKey(product.getProductId())) {
            throw new InventoryException("Product with ID " + product.getProductId() + " already exists.");
        }
        
        inventory.put(product.getProductId(), product);
        product.setLastRestocked(LocalDate.now());
        
        try {
            fileDataService.saveProduct(product);
        } catch (Exception e) {
            inventory.remove(product.getProductId());
            throw new InventoryException("Failed to save product to file", e);
        }
    }
    
    public void updateProduct(Product product) throws InventoryException {
        validateProduct(product);
        
        if (!inventory.containsKey(product.getProductId())) {
            throw new InventoryException("Product not found: " + product.getProductId());
        }
        
        inventory.put(product.getProductId(), product);
        
        try {
            fileDataService.saveProduct(product);
        } catch (Exception e) {
            throw new InventoryException("Failed to update product in file", e);
        }
    }
    
    public void deleteProduct(String productId) throws InventoryException {
        if (!inventory.containsKey(productId)) {
            throw new InventoryException("Product not found: " + productId);
        }
        
        Product removed = inventory.remove(productId);
        
        try {
            fileDataService.deleteProduct(productId);
        } catch (Exception e) {
            // Rollback
            inventory.put(productId, removed);
            throw new InventoryException("Failed to delete product from file", e);
        }
    }
    
    public Product getProduct(String productId) {
        return inventory.get(productId);
    }
    
    public Product getProductByBarcode(String barcode) {
        return inventory.values().stream()
            .filter(p -> p.getBarcode().equals(barcode))
            .findFirst()
            .orElse(null);
    }
    
    public List<Product> getAllProducts() {
        return new ArrayList<>(inventory.values());
    }
    
    public List<Product> searchProducts(String keyword) {
        String searchTerm = keyword.toLowerCase();
        return inventory.values().stream()
            .filter(p -> 
                p.getName().toLowerCase().contains(searchTerm) ||
                p.getProductId().toLowerCase().contains(searchTerm) ||
                p.getBarcode().contains(searchTerm) ||
                p.getCategory().toLowerCase().contains(searchTerm) ||
                (p.getDescription() != null && p.getDescription().toLowerCase().contains(searchTerm)))
            .collect(Collectors.toList());
    }
    
    public List<Product> getProductsByCategory(String category) {
        return inventory.values().stream()
            .filter(p -> p.getCategory().equalsIgnoreCase(category))
            .collect(Collectors.toList());
    }
    
    public void addStock(String productId, int quantity, String batchNumber, LocalDate expiryDate) 
            throws InventoryException {
        
        Product product = inventory.get(productId);
        if (product == null) {
            throw new InventoryException("Product not found: " + productId);
        }
        
        if (quantity <= 0) {
            throw new InventoryException("Quantity must be positive");
        }
        
        int newQuantity = product.getQuantityInStock() + quantity;
        if (newQuantity > product.getMaxStockLevel()) {
            throw new InventoryException(
                String.format("Exceeds maximum stock level (%d). Current: %d, Adding: %d",
                    product.getMaxStockLevel(), product.getQuantityInStock(), quantity));
        }
        
        product.setQuantityInStock(newQuantity);
        product.setLastRestocked(LocalDate.now());
        
        if (expiryDate != null && product.isPerishable()) {
            product.setExpiryDate(expiryDate);
        }
        
        try {
            fileDataService.saveProduct(product);
            
            // Log stock movement
            fileDataService.logStockMovement(productId, "RESTOCK", quantity, 
                product.getQuantityInStock(), batchNumber);
            
        } catch (Exception e) {
            // Rollback
            product.setQuantityInStock(product.getQuantityInStock() - quantity);
            throw new InventoryException("Failed to update stock", e);
        }
        
        if (product.needsReorder()) {
            sendLowStockAlert(product);
        }
    }
    
    public void sellProduct(String productId, int quantity, String customerId, String transactionId) 
            throws InventoryException {
        
        Product product = inventory.get(productId);
        if (product == null) {
            throw new InventoryException("Product not found: " + productId);
        }
        
        if (quantity <= 0) {
            throw new InventoryException("Quantity must be positive");
        }
        
        if (product.getQuantityInStock() < quantity) {
            throw new InsufficientStockException(productId, quantity, product.getQuantityInStock());
        }
        
        if (product.isExpired()) {
            throw new InventoryException("Cannot sell expired product: " + product.getName());
        }
        
        int oldQuantity = product.getQuantityInStock();
        product.setQuantityInStock(oldQuantity - quantity);
        product.setQuantitySold(product.getQuantitySold() + quantity);
        
        try {
            fileDataService.saveProduct(product);
            
            // Log stock movement
            fileDataService.logStockMovement(productId, "SALE", -quantity, 
                product.getQuantityInStock(), transactionId);
            
        } catch (Exception e) {
            // Rollback
            product.setQuantityInStock(oldQuantity);
            product.setQuantitySold(product.getQuantitySold() - quantity);
            throw new InventoryException("Failed to process sale", e);
        }
        
        if (product.needsReorder()) {
            sendLowStockAlert(product);
        }
    }
    
    public void returnProduct(String productId, int quantity, String reason, String originalTransactionId) 
            throws InventoryException {
        
        Product product = inventory.get(productId);
        if (product == null) {
            throw new InventoryException("Product not found: " + productId);
        }
        
        product.setQuantityInStock(product.getQuantityInStock() + quantity);
        
        try {
            fileDataService.saveProduct(product);
            
            // Log stock movement
            fileDataService.logStockMovement(productId, "RETURN", quantity, 
                product.getQuantityInStock(), originalTransactionId);
            
        } catch (Exception e) {
            // Rollback
            product.setQuantityInStock(product.getQuantityInStock() - quantity);
            throw new InventoryException("Failed to process return", e);
        }
    }
    
    public List<Product> getProductsNeedingReorder() {
        return inventory.values().stream()
            .filter(Product::needsReorder)
            .sorted(Comparator.comparing(Product::getQuantityInStock))
            .collect(Collectors.toList());
    }
    
    public List<Product> getExpiringProducts(int daysThreshold) {
        return inventory.values().stream()
            .filter(p -> p.isPerishable() && p.willExpireSoon(daysThreshold))
            .sorted(Comparator.comparing(Product::getExpiryDate))
            .collect(Collectors.toList());
    }
    
    public Map<String, Object> getInventoryStats() {
        Map<String, Object> stats = new HashMap<>();
        
        double totalValue = inventory.values().stream()
            .mapToDouble(Product::getStockValue)
            .sum();
        
        double totalPotentialRevenue = inventory.values().stream()
            .mapToDouble(Product::getPotentialRevenue)
            .sum();
        
        long lowStockCount = inventory.values().stream()
            .filter(Product::needsReorder)
            .count();
        
        long expiredCount = inventory.values().stream()
            .filter(Product::isExpired)
            .count();
        
        stats.put("totalProducts", inventory.size());
        stats.put("totalValue", totalValue);
        stats.put("totalPotentialRevenue", totalPotentialRevenue);
        stats.put("lowStockCount", lowStockCount);
        stats.put("expiredCount", expiredCount);
        stats.put("lastUpdated", LocalDateTime.now());
        
        return stats;
    }
    
    public Map<String, CategorySummary> getCategorySummary() {
        Map<String, CategorySummary> summary = new HashMap<>();
        
        inventory.values().forEach(product -> {
            String category = product.getCategory();
            CategorySummary catSummary = summary.getOrDefault(category, 
                new CategorySummary(category));
            
            catSummary.addProduct(product);
            summary.put(category, catSummary);
        });
        
        return summary;
    }
    
    public String generateReorderReport() {
        List<Product> reorderList = getProductsNeedingReorder();
        
        StringBuilder report = new StringBuilder();
        report.append("=== REORDER REPORT ===\n");
        report.append("Generated: ").append(LocalDateTime.now()).append("\n\n");
        
        if (reorderList.isEmpty()) {
            report.append("No products need reordering at this time.\n");
        } else {
            report.append(String.format("%-15s %-30s %-15s %-10s %-10s\n", 
                "Product ID", "Product Name", "Category", "Current", "Min Level"));
            report.append("-".repeat(80)).append("\n");
            
            for (Product p : reorderList) {
                report.append(String.format("%-15s %-30s %-15s %-10d %-10d\n",
                    p.getProductId(), 
                    p.getName().length() > 30 ? p.getName().substring(0, 27) + "..." : p.getName(),
                    p.getCategory(),
                    p.getQuantityInStock(),
                    p.getMinStockLevel()));
            }
            
            report.append("\nTotal items needing reorder: ").append(reorderList.size()).append("\n");
        }
        
        return report.toString();
    }
    
    private void validateProduct(Product product) throws InventoryException {
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new InventoryException("Product name is required");
        }
        if (product.getPurchasePrice() < 0) {
            throw new InventoryException("Purchase price cannot be negative");
        }
        if (product.getSellingPrice() < product.getPurchasePrice()) {
            throw new InventoryException("Selling price must be >= purchase price");
        }
        if (product.getQuantityInStock() < 0) {
            throw new InventoryException("Quantity cannot be negative");
        }
        if (product.getMinStockLevel() < 0 || product.getMaxStockLevel() <= 0) {
            throw new InventoryException("Stock levels must be positive");
        }
        if (product.getMinStockLevel() >= product.getMaxStockLevel()) {
            throw new InventoryException("Min stock level must be less than max level");
        }
    }
    
    private void sendLowStockAlert(Product product) {
        System.out.println("ALERT: Low stock for product: " + product.getName() + 
                          " (Current: " + product.getQuantityInStock() + 
                          ", Min: " + product.getMinStockLevel() + ")");
        
        try {
            fileDataService.saveLowStockAlert(product);
        } catch (Exception e) {
            System.err.println("Failed to save low stock alert: " + e.getMessage());
        }
    }
    
    private void createSampleInventory() {
        try {
            Product p1 = new Product("Coca-Cola 330ml", "Beverage", 0.45, 0.99, 120);
            p1.setMinStockLevel(50);
            p1.setMaxStockLevel(200);
            p1.setSupplierId("SUPP001");
            p1.setLocation("Aisle 3, Shelf B");
            
            Product p2 = new Product("Lays Classic Chips 150g", "Snacks", 0.85, 1.99, 80);
            p2.setMinStockLevel(30);
            p2.setMaxStockLevel(150);
            p2.setSupplierId("SUPP002");
            p2.setLocation("Aisle 5, Shelf A");
            p2.setExpiryDate(LocalDate.now().plusMonths(6));
            p2.setPerishable(true);
            
            Product p3 = new Product("Dove Soap 100g", "Personal Care", 1.20, 2.49, 45);
            p3.setMinStockLevel(20);
            p3.setMaxStockLevel(100);
            p3.setSupplierId("SUPP003");
            p3.setLocation("Aisle 7, Shelf C");
            
            inventory.put(p1.getProductId(), p1);
            inventory.put(p2.getProductId(), p2);
            inventory.put(p3.getProductId(), p3);
            
            fileDataService.saveAllProducts(new ArrayList<>(inventory.values()));
            
        } catch (Exception e) {
            System.err.println("Error creating sample inventory: " + e.getMessage());
        }
    }
    
    public FileDataService getFileService() {
        return fileDataService;
    }
    
    // Inner class for category summary
    public static class CategorySummary {
        private String category;
        private int productCount;
        private int totalStock;
        private double totalValue;
        private double totalPotentialRevenue;
        
        public CategorySummary(String category) {
            this.category = category;
        }
        
        public void addProduct(Product product) {
            productCount++;
            totalStock += product.getQuantityInStock();
            totalValue += product.getStockValue();
            totalPotentialRevenue += product.getPotentialRevenue();
        }
        
        public String getCategory() { return category; }
        public int getProductCount() { return productCount; }
        public int getTotalStock() { return totalStock; }
        public double getTotalValue() { return totalValue; }
        public double getTotalPotentialRevenue() { return totalPotentialRevenue; }
        
        public double getAveragePrice() {
            return productCount > 0 ? totalPotentialRevenue / totalStock : 0;
        }
    }
}