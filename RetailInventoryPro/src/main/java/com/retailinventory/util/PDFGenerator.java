package com.retailinventory.util;

import com.retailinventory.model.Order;
import com.retailinventory.model.OrderItem;
import java.io.FileOutputStream;
import java.time.format.DateTimeFormatter;

public class PDFGenerator {
    
    public static void generateReceipt(Order order, String filePath) {
        try {
            // For simplicity, we'll generate a text receipt
            // In a real application, you would use iText or another PDF library
            
            StringBuilder receipt = new StringBuilder();
            receipt.append("========================================\n");
            receipt.append("           QUICKMART STORE\n");
            receipt.append("       123 Main Street, City\n");
            receipt.append("         Phone: (123) 456-7890\n");
            receipt.append("========================================\n");
            receipt.append("Receipt #: ").append(order.getOrderId()).append("\n");
            receipt.append("Date: ").append(
                order.getOrderDate().format(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
            receipt.append("Customer: ").append(order.getCustomerId()).append("\n");
            receipt.append("Status: ").append(order.getStatus()).append("\n");
            receipt.append("----------------------------------------\n");
            receipt.append("ITEMS:\n");
            
            for (OrderItem item : order.getItems()) {
                receipt.append(String.format("%-20s %6d @ $%-8.2f $%-8.2f\n",
                    item.getProductName(),
                    item.getQuantity(),
                    item.getPrice(),
                    item.getPrice() * item.getQuantity()));
            }
            
            receipt.append("----------------------------------------\n");
            receipt.append(String.format("Subtotal: $%.2f\n", order.getTotalAmount()));
            receipt.append(String.format("Discount: $%.2f\n", order.getDiscount()));
            receipt.append(String.format("Tax: $%.2f\n", order.getTax()));
            receipt.append(String.format("TOTAL: $%.2f\n", order.getFinalAmount()));
            receipt.append("Payment: ").append(order.getPaymentMethod()).append("\n");
            receipt.append("========================================\n");
            receipt.append("Thank you for shopping with us!\n");
            
            // Write to file
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                fos.write(receipt.toString().getBytes());
            }
            
        } catch (Exception e) {
            System.err.println("Failed to generate receipt: " + e.getMessage());
        }
    }
    
    public static void generateReport(String title, String content, String filePath) {
        try {
            StringBuilder report = new StringBuilder();
            report.append("=== ").append(title).append(" ===\n");
            report.append("Generated: ").append(
                java.time.LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n\n");
            report.append(content);
            
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                fos.write(report.toString().getBytes());
            }
            
        } catch (Exception e) {
            System.err.println("Failed to generate report: " + e.getMessage());
        }
    }
}