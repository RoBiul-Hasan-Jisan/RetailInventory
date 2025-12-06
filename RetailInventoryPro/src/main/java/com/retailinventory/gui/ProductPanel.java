package com.retailinventory.gui;

import com.retailinventory.model.Product;
import com.retailinventory.service.InventoryService;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class ProductPanel extends JPanel {
    private InventoryService inventoryService;
    private JTable productTable;
    private ProductTableModel tableModel;
    private JTextField searchField;
    
    public ProductPanel(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
        initializeUI();
        loadProducts();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Toolbar
        JPanel toolbar = createToolbar();
        add(toolbar, BorderLayout.NORTH);
        
        // Table
        createProductTable();
        
        // Button panel
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createToolbar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        
        searchField = new JTextField(20);
        searchField.setToolTipText("Search by name, ID, or barcode");
        
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> searchProducts());
        
        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> {
            searchField.setText("");
            loadProducts();
        });
        
        panel.add(new JLabel("Search:"));
        panel.add(searchField);
        panel.add(searchButton);
        panel.add(clearButton);
        
        return panel;
    }
    
    private void createProductTable() {
        tableModel = new ProductTableModel();
        productTable = new JTable(tableModel);
        productTable.setRowHeight(30);
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(productTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Products"));
        
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        
        JButton addButton = new JButton("Add Product");
        JButton editButton = new JButton("Edit Product");
        JButton deleteButton = new JButton("Delete Product");
        JButton refreshButton = new JButton("Refresh");
        
        addButton.addActionListener(e -> addProduct());
        editButton.addActionListener(e -> editProduct());
        deleteButton.addActionListener(e -> deleteProduct());
        refreshButton.addActionListener(e -> loadProducts());
        
        panel.add(addButton);
        panel.add(editButton);
        panel.add(deleteButton);
        panel.add(refreshButton);
        
        return panel;
    }
    
    private void loadProducts() {
        try {
            List<Product> products = inventoryService.getAllProducts();
            tableModel.setProducts(products);
            tableModel.fireTableDataChanged();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error loading products: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void searchProducts() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadProducts();
            return;
        }
        
        try {
            List<Product> results = inventoryService.searchProducts(keyword);
            tableModel.setProducts(results);
            tableModel.fireTableDataChanged();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error searching products: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void addProduct() {
        AddProductDialog dialog = new AddProductDialog(
            SwingUtilities.getWindowAncestor(this), inventoryService);
        dialog.setVisible(true);
        
        if (dialog.isProductAdded()) {
            loadProducts();
        }
    }
    
    private void editProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select a product to edit.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Product product = tableModel.getProductAt(selectedRow);
        EditProductDialog dialog = new EditProductDialog(
            SwingUtilities.getWindowAncestor(this), inventoryService, product);
        dialog.setVisible(true);
        
        if (dialog.isProductUpdated()) {
            loadProducts();
        }
    }
    
    private void deleteProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select a product to delete.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Product product = tableModel.getProductAt(selectedRow);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete product: " + product.getName() + "?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                inventoryService.deleteProduct(product.getProductId());
                loadProducts();
                JOptionPane.showMessageDialog(this,
                    "Product deleted successfully.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Error deleting product: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    // Inner TableModel class
    private class ProductTableModel extends AbstractTableModel {
        private List<Product> products;
        private String[] columnNames = {
            "ID", "Name", "Category", "Price", "Stock", "Min Stock", "Status", "Barcode"
        };
        
        public void setProducts(List<Product> products) {
            this.products = products;
        }
        
        public Product getProductAt(int row) {
            return products.get(row);
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
                case 6: return getStockStatus(product);
                case 7: return product.getBarcode();
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
}