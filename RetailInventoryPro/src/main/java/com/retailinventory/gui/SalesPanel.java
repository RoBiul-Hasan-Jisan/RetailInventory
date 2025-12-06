package com.retailinventory.gui;

import com.retailinventory.model.Product;
import com.retailinventory.model.Order;
import com.retailinventory.model.OrderItem;
import com.retailinventory.service.InventoryService;
import com.retailinventory.service.OrderService;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.*;

public class SalesPanel extends JPanel {
    private InventoryService inventoryService;
    private OrderService orderService;
    
    private DefaultTableModel cartModel;
    private JTable cartTable;
    private JTextField barcodeField;
    private JTextField quantityField;
    private JLabel totalLabel;
    private JComboBox<String> paymentMethod;
    
    private java.util.List<OrderItem> cartItems;
    private double cartTotal;
    
    public SalesPanel(InventoryService inventoryService, OrderService orderService) {
        this.inventoryService = inventoryService;
        this.orderService = orderService;
        this.cartItems = new ArrayList<>();
        this.cartTotal = 0.0;
        
        initializeUI();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(400);
        splitPane.setLeftComponent(createProductPanel());
        splitPane.setRightComponent(createCartPanel());
        
        add(splitPane, BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);
    }
    
    private JPanel createProductPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Product Entry"));
        
        // Input panel
        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        
        inputPanel.add(new JLabel("Barcode:"));
        barcodeField = new JTextField();
        barcodeField.addActionListener(e -> lookupProduct());
        inputPanel.add(barcodeField);
        
        inputPanel.add(new JLabel("Quantity:"));
        quantityField = new JTextField("1");
        inputPanel.add(quantityField);
        
        JButton scanButton = new JButton("Scan");
        JButton addButton = new JButton("Add to Cart");
        
        scanButton.addActionListener(e -> simulateScan());
        addButton.addActionListener(e -> addToCart());
        
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        buttonPanel.add(scanButton);
        buttonPanel.add(addButton);
        
        inputPanel.add(scanButton);
        inputPanel.add(addButton);
        
        // Product info display
        JTextArea productInfo = new JTextArea(10, 30);
        productInfo.setEditable(false);
        productInfo.setBorder(BorderFactory.createTitledBorder("Product Information"));
        JScrollPane scrollPane = new JScrollPane(productInfo);
        
        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createCartPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Shopping Cart"));
        
        // Cart table
        String[] columns = {"Product", "Price", "Qty", "Subtotal"};
        cartModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        cartTable = new JTable(cartModel);
        cartTable.setRowHeight(30);
        
        JScrollPane scrollPane = new JScrollPane(cartTable);
        
        // Total display
        totalLabel = new JLabel("Total: $0.00", SwingConstants.RIGHT);
        totalLabel.setFont(new Font("Arial", Font.BOLD, 16));
        totalLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(totalLabel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        
        paymentMethod = new JComboBox<>(new String[]{
            "Cash", "Credit Card", "Debit Card", "Mobile Payment"
        });
        
        JButton checkoutButton = new JButton("Checkout");
        JButton clearButton = new JButton("Clear Cart");
        
        checkoutButton.addActionListener(e -> checkout());
        clearButton.addActionListener(e -> clearCart());
        
        checkoutButton.setBackground(new Color(46, 204, 113));
        checkoutButton.setForeground(Color.WHITE);
        checkoutButton.setFont(new Font("Arial", Font.BOLD, 14));
        
        panel.add(new JLabel("Payment Method:"));
        panel.add(paymentMethod);
        panel.add(clearButton);
        panel.add(checkoutButton);
        
        return panel;
    }
    
    private void simulateScan() {
        // Generate random barcode for simulation
        barcodeField.setText("590123412345" + 
            String.format("%02d", (int)(Math.random() * 100)));
        lookupProduct();
    }
    
    private void lookupProduct() {
        String barcode = barcodeField.getText().trim();
        if (barcode.isEmpty()) {
            return;
        }
        
        try {
            Product product = inventoryService.getProductByBarcode(barcode);
            if (product != null) {
                // Display product info
                JOptionPane.showMessageDialog(this,
                    "Product found: " + product.getName() + 
                    "\nPrice: $" + product.getSellingPrice() +
                    "\nStock: " + product.getQuantityInStock(),
                    "Product Info",
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Product not found for barcode: " + barcode,
                    "Not Found",
                    JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error looking up product: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void addToCart() {
        String barcode = barcodeField.getText().trim();
        if (barcode.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter or scan a barcode",
                "Input Required",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int quantity;
        try {
            quantity = Integer.parseInt(quantityField.getText().trim());
            if (quantity <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                "Please enter a valid quantity",
                "Invalid Input",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            Product product = inventoryService.getProductByBarcode(barcode);
            if (product == null) {
                JOptionPane.showMessageDialog(this,
                    "Product not found!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (product.getQuantityInStock() < quantity) {
                JOptionPane.showMessageDialog(this,
                    "Insufficient stock! Available: " + product.getQuantityInStock(),
                    "Stock Error",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Add to cart
            OrderItem item = new OrderItem(
                product.getProductId(),
                product.getName(),
                product.getSellingPrice(),
                quantity
            );
            
            cartItems.add(item);
            updateCartDisplay();
            
            // Clear input
            barcodeField.setText("");
            quantityField.setText("1");
            barcodeField.requestFocus();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error adding to cart: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateCartDisplay() {
        cartModel.setRowCount(0);
        cartTotal = 0;
        
        for (OrderItem item : cartItems) {
            double subtotal = item.getPrice() * item.getQuantity();
            cartTotal += subtotal;
            
            cartModel.addRow(new Object[]{
                item.getProductName(),
                String.format("$%.2f", item.getPrice()),
                item.getQuantity(),
                String.format("$%.2f", subtotal)
            });
        }
        
        totalLabel.setText(String.format("Total: $%.2f", cartTotal));
    }
    
    private void clearCart() {
        if (cartItems.isEmpty()) {
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Clear the entire cart?",
            "Confirm Clear",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            cartItems.clear();
            updateCartDisplay();
        }
    }
    
    private void checkout() {
        if (cartItems.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Cart is empty!",
                "Empty Cart",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            // Create order
            Order order = orderService.createOrder("WALK-IN", cartItems);
            order.setPaymentMethod((String) paymentMethod.getSelectedItem());
            
            // Process order
            orderService.processOrder(order.getOrderId());
            
            // Show receipt
            showReceipt(order);
            
            // Clear cart
            cartItems.clear();
            updateCartDisplay();
            
            JOptionPane.showMessageDialog(this,
                "Checkout successful! Order #" + order.getOrderId(),
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Checkout failed: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showReceipt(Order order) {
        StringBuilder receipt = new StringBuilder();
        receipt.append("=== RECEIPT ===\n");
        receipt.append("Order #: ").append(order.getOrderId()).append("\n");
        receipt.append("Date: ").append(order.getOrderDate()).append("\n");
        receipt.append("Items:\n");
        
        for (OrderItem item : order.getItems()) {
            receipt.append(String.format("  %s x%d @ $%.2f = $%.2f\n",
                item.getProductName(), item.getQuantity(), 
                item.getPrice(), item.getPrice() * item.getQuantity()));
        }
        
        receipt.append("Total: $").append(order.getFinalAmount()).append("\n");
        receipt.append("Payment: ").append(order.getPaymentMethod()).append("\n");
        receipt.append("Thank you!\n");
        
        JTextArea textArea = new JTextArea(receipt.toString(), 15, 40);
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        
        JOptionPane.showMessageDialog(this, scrollPane, "Receipt", 
            JOptionPane.INFORMATION_MESSAGE);
    }
}