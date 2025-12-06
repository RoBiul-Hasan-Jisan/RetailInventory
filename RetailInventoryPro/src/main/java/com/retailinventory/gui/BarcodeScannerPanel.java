package com.retailinventory.gui;

import com.retailinventory.model.Product;
import com.retailinventory.service.InventoryService;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class BarcodeScannerPanel extends JPanel {
    private InventoryService inventoryService;
    private JTextArea outputArea;
    private JTextField barcodeField;
    private StringBuilder barcodeBuffer;
    private long lastKeyTime;
    
    public BarcodeScannerPanel(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
        this.barcodeBuffer = new StringBuilder();
        this.lastKeyTime = 0;
        
        initializeUI();
        setupBarcodeScanner();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Input panel
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        inputPanel.add(new JLabel("Barcode:"));
        barcodeField = new JTextField(20);
        inputPanel.add(barcodeField);
        
        JButton scanButton = new JButton("Manual Entry");
        scanButton.addActionListener(e -> manualLookup());
        inputPanel.add(scanButton);
        
        // Output area
        outputArea = new JTextArea(15, 50);
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Scan Results"));
        
        // Add components
        add(inputPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        
        // Info label
        JLabel infoLabel = new JLabel(
            "Tip: Connect a barcode scanner and scan products directly into the system.");
        infoLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        add(infoLabel, BorderLayout.SOUTH);
    }
    
    private void setupBarcodeScanner() {
        // Add key listener to capture barcode scanner input
        KeyListener keyListener = new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                // Not used
            }
            
            @Override
            public void keyPressed(KeyEvent e) {
                long currentTime = System.currentTimeMillis();
                
                // Check if enough time has passed since last key (scanner sends keys quickly)
                if (currentTime - lastKeyTime > 50) {
                    barcodeBuffer.setLength(0); // Start new barcode
                }
                lastKeyTime = currentTime;
            }
            
            @Override
            public void keyReleased(KeyEvent e) {
                char keyChar = e.getKeyChar();
                
                // Barcode scanners typically send Enter after the barcode
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (barcodeBuffer.length() > 0) {
                        String barcode = barcodeBuffer.toString();
                        processBarcode(barcode);
                        barcodeBuffer.setLength(0);
                    }
                } else if (Character.isDigit(keyChar) || 
                          (keyChar >= 'A' && keyChar <= 'Z') || 
                          (keyChar >= 'a' && keyChar <= 'z')) {
                    barcodeBuffer.append(keyChar);
                }
            }
        };
        
        // Add listener to all components
        addKeyListener(keyListener);
        barcodeField.addKeyListener(keyListener);
        outputArea.addKeyListener(keyListener);
        
        // Ensure panel is focusable
        setFocusable(true);
    }
    
    private void manualLookup() {
        String barcode = barcodeField.getText().trim();
        if (!barcode.isEmpty()) {
            processBarcode(barcode);
            barcodeField.setText("");
        }
    }
    
    private void processBarcode(String barcode) {
        SwingUtilities.invokeLater(() -> {
            try {
                Product product = inventoryService.getProductByBarcode(barcode);
                
                if (product != null) {
                    displayProductInfo(product);
                    outputArea.append("Scanned: " + product.getName() + 
                        " (Barcode: " + barcode + ")\n");
                } else {
                    outputArea.append("Product not found for barcode: " + barcode + "\n");
                    JOptionPane.showMessageDialog(this,
                        "Product not found! Barcode: " + barcode,
                        "Not Found",
                        JOptionPane.WARNING_MESSAGE);
                }
                
                // Scroll to bottom
                outputArea.setCaretPosition(outputArea.getDocument().getLength());
                
            } catch (Exception e) {
                outputArea.append("Error processing barcode: " + e.getMessage() + "\n");
            }
        });
    }
    
    private void displayProductInfo(Product product) {
        StringBuilder info = new StringBuilder();
        info.append("=== PRODUCT INFORMATION ===\n");
        info.append("Name: ").append(product.getName()).append("\n");
        info.append("ID: ").append(product.getProductId()).append("\n");
        info.append("Barcode: ").append(product.getBarcode()).append("\n");
        info.append("Category: ").append(product.getCategory()).append("\n");
        info.append("Price: $").append(String.format("%.2f", product.getSellingPrice())).append("\n");
        info.append("Stock: ").append(product.getQuantityInStock()).append("\n");
        info.append("Min Stock: ").append(product.getMinStockLevel()).append("\n");
        
        if (product.isExpired()) {
            info.append("Status: EXPIRED\n");
        } else if (product.needsReorder()) {
            info.append("Status: LOW STOCK\n");
        } else {
            info.append("Status: IN STOCK\n");
        }
        
        info.append("===========================\n\n");
        
        outputArea.append(info.toString());
    }
    
    public void clearOutput() {
        outputArea.setText("");
    }
}