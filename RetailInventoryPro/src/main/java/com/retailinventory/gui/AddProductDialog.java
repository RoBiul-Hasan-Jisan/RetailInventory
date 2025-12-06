package com.retailinventory.gui;

import com.retailinventory.model.Product;
import com.retailinventory.service.InventoryService;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

public class AddProductDialog extends JDialog {
    private InventoryService inventoryService;
    private boolean productAdded;
    
    private JTextField nameField;
    private JTextField categoryField;
    private JTextField purchasePriceField;
    private JTextField sellingPriceField;
    private JTextField quantityField;
    private JTextField minStockField;
    private JTextField maxStockField;
    private JCheckBox perishableCheckBox;
    
    public AddProductDialog(JFrame parent, InventoryService inventoryService) {
        super(parent, "Add New Product", true);
        this.inventoryService = inventoryService;
        this.productAdded = false;
        
        initializeUI();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setSize(500, 400);
        setLocationRelativeTo(getParent());
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        int row = 0;
        
        // Name
        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(new JLabel("Product Name:*"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = row++;
        gbc.weightx = 1.0;
        nameField = new JTextField(20);
        formPanel.add(nameField, gbc);
        
        // Category
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.0;
        formPanel.add(new JLabel("Category:*"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = row++;
        gbc.weightx = 1.0;
        categoryField = new JTextField(20);
        formPanel.add(categoryField, gbc);
        
        // Purchase Price
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.0;
        formPanel.add(new JLabel("Purchase Price:*"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = row++;
        gbc.weightx = 1.0;
        purchasePriceField = new JTextField(10);
        formPanel.add(purchasePriceField, gbc);
        
        // Selling Price
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.0;
        formPanel.add(new JLabel("Selling Price:*"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = row++;
        gbc.weightx = 1.0;
        sellingPriceField = new JTextField(10);
        formPanel.add(sellingPriceField, gbc);
        
        // Quantity
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.0;
        formPanel.add(new JLabel("Initial Quantity:*"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = row++;
        gbc.weightx = 1.0;
        quantityField = new JTextField(10);
        quantityField.setText("0");
        formPanel.add(quantityField, gbc);
        
        // Min Stock Level
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.0;
        formPanel.add(new JLabel("Min Stock Level:*"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = row++;
        gbc.weightx = 1.0;
        minStockField = new JTextField(10);
        minStockField.setText("10");
        formPanel.add(minStockField, gbc);
        
        // Max Stock Level
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.0;
        formPanel.add(new JLabel("Max Stock Level:*"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = row++;
        gbc.weightx = 1.0;
        maxStockField = new JTextField(10);
        maxStockField.setText("100");
        formPanel.add(maxStockField, gbc);
        
        // Perishable
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.0;
        formPanel.add(new JLabel("Perishable:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = row++;
        gbc.weightx = 1.0;
        perishableCheckBox = new JCheckBox();
        formPanel.add(perishableCheckBox, gbc);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        
        saveButton.addActionListener(e -> saveProduct());
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        // Add components
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Set default button
        getRootPane().setDefaultButton(saveButton);
    }
    
    private void saveProduct() {
        try {
            // Validate inputs
            if (nameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Product name is required.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
                nameField.requestFocus();
                return;
            }
            
            if (categoryField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Category is required.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
                categoryField.requestFocus();
                return;
            }
            
            double purchasePrice = Double.parseDouble(purchasePriceField.getText());
            double sellingPrice = Double.parseDouble(sellingPriceField.getText());
            int quantity = Integer.parseInt(quantityField.getText());
            int minStock = Integer.parseInt(minStockField.getText());
            int maxStock = Integer.parseInt(maxStockField.getText());
            
            if (purchasePrice < 0 || sellingPrice < 0) {
                JOptionPane.showMessageDialog(this,
                    "Prices cannot be negative.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            if (sellingPrice < purchasePrice) {
                JOptionPane.showMessageDialog(this,
                    "Selling price must be greater than or equal to purchase price.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            if (quantity < 0 || minStock < 0 || maxStock <= 0) {
                JOptionPane.showMessageDialog(this,
                    "Stock values must be positive.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            if (minStock >= maxStock) {
                JOptionPane.showMessageDialog(this,
                    "Minimum stock level must be less than maximum stock level.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Create product
            Product product = new Product(
                nameField.getText().trim(),
                categoryField.getText().trim(),
                purchasePrice,
                sellingPrice,
                quantity
            );
            
            product.setMinStockLevel(minStock);
            product.setMaxStockLevel(maxStock);
            product.setPerishable(perishableCheckBox.isSelected());
            product.setLastRestocked(LocalDate.now());
            
            // Save product
            inventoryService.addProduct(product);
            
            productAdded = true;
            
            JOptionPane.showMessageDialog(this,
                "Product added successfully!\nProduct ID: " + product.getProductId() +
                "\nBarcode: " + product.getBarcode(),
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
            
            dispose();
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                "Please enter valid numbers for price and quantity fields.",
                "Invalid Input",
                JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error saving product: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public boolean isProductAdded() {
        return productAdded;
    }
}