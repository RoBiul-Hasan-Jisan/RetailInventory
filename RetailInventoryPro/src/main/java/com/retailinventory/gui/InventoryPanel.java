package com.retailinventory.gui;

import com.retailinventory.model.Product;
import com.retailinventory.service.InventoryService;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class InventoryPanel extends JPanel {
    private InventoryService inventoryService;
    private JTable inventoryTable;
    private InventoryTableModel tableModel;
    
    public InventoryPanel(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
        initializeUI();
        loadInventory();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create table
        createInventoryTable();
        
        // Create button panel
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void createInventoryTable() {
        tableModel = new InventoryTableModel();
        inventoryTable = new JTable(tableModel);
        inventoryTable.setRowHeight(30);
        inventoryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Custom renderer for stock levels
        inventoryTable.setDefaultRenderer(Integer.class, new StockLevelRenderer());
        
        JScrollPane scrollPane = new JScrollPane(inventoryTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Inventory Status"));
        
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        
        JButton refreshButton = new JButton("Refresh");
        JButton lowStockButton = new JButton("Show Low Stock");
        JButton expiringButton = new JButton("Show Expiring Soon");
        JButton statsButton = new JButton("Show Statistics");
        
        refreshButton.addActionListener(e -> loadInventory());
        lowStockButton.addActionListener(e -> showLowStock());
        expiringButton.addActionListener(e -> showExpiringSoon());
        statsButton.addActionListener(e -> showStatistics());
        
        panel.add(refreshButton);
        panel.add(lowStockButton);
        panel.add(expiringButton);
        panel.add(statsButton);
        
        return panel;
    }
    
    private void loadInventory() {
        try {
            List<Product> products = inventoryService.getAllProducts();
            tableModel.setProducts(products);
            tableModel.fireTableDataChanged();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error loading inventory: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showLowStock() {
        try {
            List<Product> lowStockProducts = inventoryService.getProductsNeedingReorder();
            
            if (lowStockProducts.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "No products are low on stock.",
                    "Low Stock Alert",
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                StringBuilder message = new StringBuilder();
                message.append("Low Stock Products:\n\n");
                
                for (Product product : lowStockProducts) {
                    message.append(String.format("%s - %s (Stock: %d, Min: %d)\n",
                        product.getProductId(), product.getName(),
                        product.getQuantityInStock(), product.getMinStockLevel()));
                }
                
                JTextArea textArea = new JTextArea(message.toString(), 10, 50);
                textArea.setEditable(false);
                JScrollPane scrollPane = new JScrollPane(textArea);
                
                JOptionPane.showMessageDialog(this, scrollPane,
                    "Low Stock Alert", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error checking low stock: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showExpiringSoon() {
        try {
            List<Product> expiringProducts = inventoryService.getExpiringProducts(30);
            
            if (expiringProducts.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "No products are expiring within 30 days.",
                    "Expiring Products",
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                StringBuilder message = new StringBuilder();
                message.append("Products Expiring Within 30 Days:\n\n");
                
                for (Product product : expiringProducts) {
                    long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(
                        java.time.LocalDate.now(), product.getExpiryDate());
                    message.append(String.format("%s - %s (Expires: %s, Days Left: %d)\n",
                        product.getProductId(), product.getName(),
                        product.getExpiryDate(), daysLeft));
                }
                
                JTextArea textArea = new JTextArea(message.toString(), 10, 60);
                textArea.setEditable(false);
                JScrollPane scrollPane = new JScrollPane(textArea);
                
                JOptionPane.showMessageDialog(this, scrollPane,
                    "Expiring Products", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error checking expiring products: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showStatistics() {
        try {
            Map<String, Object> stats = inventoryService.getInventoryStats();
            
            StringBuilder message = new StringBuilder();
            message.append("=== INVENTORY STATISTICS ===\n\n");
            message.append(String.format("Total Products: %d\n", stats.get("totalProducts")));
            message.append(String.format("Total Inventory Value: $%.2f\n", stats.get("totalValue")));
            message.append(String.format("Total Potential Revenue: $%.2f\n", 
                stats.get("totalPotentialRevenue")));
            message.append(String.format("Low Stock Items: %d\n", stats.get("lowStockCount")));
            message.append(String.format("Expired Items: %d\n", stats.get("expiredCount")));
            message.append(String.format("Last Updated: %s\n", stats.get("lastUpdated")));
            
            JOptionPane.showMessageDialog(this, message.toString(),
                "Inventory Statistics", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error loading statistics: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // Inner TableModel class
    private class InventoryTableModel extends AbstractTableModel {
        private List<Product> products;
        private String[] columnNames = {
            "ID", "Name", "Category", "Price", "Stock", "Min Stock", 
            "Value", "Status", "Last Restocked"
        };
        
        public void setProducts(List<Product> products) {
            this.products = products;
        }
        
        @Override
        public int getRowCount() {
            return products != null ? products.size() : 0;
        }
        
        @Override
        public int getColumnCount() {
            return columnNames.length;
        }
        
        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }
        
        @Override
        public Object getValueAt(int row, int column) {
            Product product = products.get(row);
            
            switch (column) {
                case 0: return product.getProductId();
                case 1: return product.getName();
                case 2: return product.getCategory();
                case 3: return String.format("$%.2f", product.getSellingPrice());
                case 4: return product.getQuantityInStock();
                case 5: return product.getMinStockLevel();
                case 6: return String.format("$%.2f", product.getStockValue());
                case 7: return getStockStatus(product);
                case 8: return product.getLastRestocked() != null ? 
                    product.getLastRestocked().toString() : "N/A";
                default: return null;
            }
        }
        
        @Override
        public Class<?> getColumnClass(int column) {
            switch (column) {
                case 4:
                case 5: return Integer.class;
                default: return String.class;
            }
        }
        
        private String getStockStatus(Product product) {
            if (product.getQuantityInStock() <= 0) {
                return "Out of Stock";
            } else if (product.needsReorder()) {
                return "Low Stock";
            } else if (product.isExpired()) {
                return "Expired";
            } else {
                return "In Stock";
            }
        }
    }
    
    // Custom renderer for stock levels
    private class StockLevelRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            Component c = super.getTableCellRendererComponent(table, value, 
                isSelected, hasFocus, row, column);
            
            if (column == 4) { // Stock column
                int stock = (Integer) value;
                Product product = ((InventoryTableModel)table.getModel()).products.get(row);
                
                if (stock <= 0) {
                    c.setBackground(new Color(255, 200, 200));
                    c.setForeground(Color.RED);
                } else if (product.needsReorder()) {
                    c.setBackground(new Color(255, 255, 200));
                    c.setForeground(Color.ORANGE);
                } else {
                    c.setBackground(Color.WHITE);
                    c.setForeground(Color.BLACK);
                }
                
                if (isSelected) {
                    c.setBackground(table.getSelectionBackground());
                    c.setForeground(table.getSelectionForeground());
                }
            }
            
            return c;
        }
    }
}