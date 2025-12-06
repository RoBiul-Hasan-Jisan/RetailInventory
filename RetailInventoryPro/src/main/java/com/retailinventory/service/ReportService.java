package com.retailinventory.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.List;
import java.util.HashMap;

public class ReportService {
    private InventoryService inventoryService;
    private OrderService orderService;
    private FileDataService fileDataService;
    
    public ReportService() {
        this.inventoryService = new InventoryService();
        this.orderService = new OrderService();
        this.fileDataService = new FileDataService();
    }
    
    public void generateDailyReport() throws Exception {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();
        
        StringBuilder report = new StringBuilder();
        report.append("=== DAILY SALES REPORT ===\n");
        report.append("Date: ").append(today).append("\n");
        report.append("Generated: ").append(LocalDateTime.now()).append("\n\n");
        
        // Sales Summary
        double totalSales = orderService.getTotalSales(startOfDay, endOfDay);
        int totalItems = orderService.getTotalItemsSold(startOfDay, endOfDay);
        
        report.append("SALES SUMMARY:\n");
        report.append(String.format("Total Sales: $%.2f\n", totalSales));
        report.append(String.format("Items Sold: %d\n", totalItems));
        report.append(String.format("Average Transaction: $%.2f\n\n", 
            totalItems > 0 ? totalSales / totalItems : 0));
        
        // Category Breakdown
        Map<String, Double> salesByCategory = orderService.getSalesByCategory(startOfDay, endOfDay);
        report.append("SALES BY CATEGORY:\n");
        report.append("-".repeat(40)).append("\n");
        salesByCategory.forEach((category, amount) -> {
            double percentage = totalSales > 0 ? (amount / totalSales) * 100 : 0;
            report.append(String.format("%-20s $%-10.2f (%.1f%%)\n", 
                category, amount, percentage));
        });
        report.append("\n");
        
        // Inventory Status
        Map<String, Object> inventoryStats = inventoryService.getInventoryStats();
        report.append("INVENTORY STATUS:\n");
        report.append("-".repeat(40)).append("\n");
        report.append(String.format("Total Products: %d\n", inventoryStats.get("totalProducts")));
        report.append(String.format("Inventory Value: $%.2f\n", inventoryStats.get("totalValue")));
        report.append(String.format("Low Stock Items: %d\n", inventoryStats.get("lowStockCount")));
        report.append(String.format("Expired Items: %d\n", inventoryStats.get("expiredCount")));
        
        // Save report
        String reportFile = "data/reports/daily/report_" + 
            today.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".txt";
        java.nio.file.Files.writeString(
            java.nio.file.Paths.get(reportFile), 
            report.toString()
        );
    }
    
    public void generateInventoryReport() throws Exception {
        StringBuilder report = new StringBuilder();
        report.append("=== INVENTORY DETAILED REPORT ===\n");
        report.append("Generated: ").append(LocalDateTime.now()).append("\n\n");
        
        // Category Summary
        Map<String, InventoryService.CategorySummary> categorySummary = 
            inventoryService.getCategorySummary();
        
        report.append("CATEGORY SUMMARY:\n");
        report.append("-".repeat(80)).append("\n");
        report.append(String.format("%-20s %-10s %-15s %-15s %-15s\n", 
            "Category", "Products", "Total Stock", "Total Value", "Avg Price"));
        
        categorySummary.forEach((category, summary) -> {
            report.append(String.format("%-20s %-10d %-15d $%-14.2f $%-14.2f\n",
                category,
                summary.getProductCount(),
                summary.getTotalStock(),
                summary.getTotalValue(),
                summary.getAveragePrice()));
        });
        report.append("\n");
        
        // Low Stock Items
        List<Product> lowStockItems = inventoryService.getProductsNeedingReorder();
        if (!lowStockItems.isEmpty()) {
            report.append("LOW STOCK ITEMS (Need Reorder):\n");
            report.append("-".repeat(80)).append("\n");
            report.append(String.format("%-15s %-30s %-15s %-10s %-10s\n", 
                "Product ID", "Product Name", "Category", "Current", "Min Level"));
            
            for (Product product : lowStockItems) {
                report.append(String.format("%-15s %-30s %-15s %-10d %-10d\n",
                    product.getProductId(),
                    product.getName().length() > 30 ? 
                        product.getName().substring(0, 27) + "..." : product.getName(),
                    product.getCategory(),
                    product.getQuantityInStock(),
                    product.getMinStockLevel()));
            }
            report.append("\n");
        }
        
        // Expiring Items
        List<Product> expiringItems = inventoryService.getExpiringProducts(30);
        if (!expiringItems.isEmpty()) {
            report.append("ITEMS EXPIRING WITHIN 30 DAYS:\n");
            report.append("-".repeat(80)).append("\n");
            report.append(String.format("%-15s %-30s %-15s %-12s %-8s\n", 
                "Product ID", "Product Name", "Category", "Expiry Date", "Days Left"));
            
            for (Product product : expiringItems) {
                long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(
                    LocalDate.now(), product.getExpiryDate());
                report.append(String.format("%-15s %-30s %-15s %-12s %-8d\n",
                    product.getProductId(),
                    product.getName().length() > 30 ? 
                        product.getName().substring(0, 27) + "..." : product.getName(),
                    product.getCategory(),
                    product.getExpiryDate().format(
                        DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    daysLeft));
            }
        }
        
        // Save report
        String reportFile = "data/reports/inventory/inventory_report_" + 
            LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".txt";
        java.nio.file.Files.writeString(
            java.nio.file.Paths.get(reportFile), 
            report.toString()
        );
    }
    
    public Map<String, Object> getSalesMetrics(LocalDateTime start, LocalDateTime end) {
        Map<String, Object> metrics = new HashMap<>();
        
        double totalSales = orderService.getTotalSales(start, end);
        int totalTransactions = orderService.getOrdersByDateRange(start, end)
            .stream()
            .filter(o -> o.getStatus().equals("COMPLETED"))
            .mapToInt(o -> 1)
            .sum();
        int totalItems = orderService.getTotalItemsSold(start, end);
        
        metrics.put("totalSales", totalSales);
        metrics.put("totalTransactions", totalTransactions);
        metrics.put("totalItems", totalItems);
        metrics.put("averageTransaction", totalTransactions > 0 ? totalSales / totalTransactions : 0);
        metrics.put("averageItemsPerTransaction", totalTransactions > 0 ? (double)totalItems / totalTransactions : 0);
        
        return metrics;
    }
    
    public String generateProfitLossReport(LocalDateTime start, LocalDateTime end) {
        StringBuilder report = new StringBuilder();
        report.append("=== PROFIT & LOSS REPORT ===\n");
        report.append("Period: ").append(start).append(" to ").append(end).append("\n");
        report.append("Generated: ").append(LocalDateTime.now()).append("\n\n");
        
        // Revenue
        double revenue = orderService.getTotalSales(start, end);
        report.append("REVENUE:\n");
        report.append(String.format("Total Sales Revenue: $%.2f\n\n", revenue));
        
        // Cost of Goods Sold (COGS)
        // This would require tracking purchase prices for sold items
        // For simplicity, we'll estimate at 60% of revenue
        double estimatedCOGS = revenue * 0.6;
        report.append("COST OF GOODS SOLD:\n");
        report.append(String.format("Estimated COGS: $%.2f\n\n", estimatedCOGS));
        
        // Gross Profit
        double grossProfit = revenue - estimatedCOGS;
        report.append("GROSS PROFIT:\n");
        report.append(String.format("Gross Profit: $%.2f\n", grossProfit));
        report.append(String.format("Gross Margin: %.1f%%\n\n", 
            revenue > 0 ? (grossProfit / revenue) * 100 : 0));
        
        return report.toString();
    }
}import java.util.stream.Collectors;
