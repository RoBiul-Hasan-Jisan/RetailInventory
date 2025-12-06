package com.retailinventory.gui;

import com.retailinventory.service.InventoryService;
import com.retailinventory.service.OrderService;
import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class DashboardPanel extends JPanel {
    private InventoryService inventoryService;
    private OrderService orderService;
    
    private JLabel totalProductsLabel;
    private JLabel inventoryValueLabel;
    private JLabel lowStockLabel;
    private JLabel dailySalesLabel;
    
    public DashboardPanel(InventoryService inventoryService, OrderService orderService) {
        this.inventoryService = inventoryService;
        this.orderService = orderService;
        
        initializeUI();
        updateDashboard();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel titleLabel = new JLabel("Store Dashboard");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> updateDashboard());
        headerPanel.add(refreshButton, BorderLayout.EAST);
        
        // Stats Panel
        JPanel statsPanel = createStatsPanel();
        
        // Quick Actions Panel
        JPanel actionsPanel = createQuickActionsPanel();
        
        // Add components
        add(headerPanel, BorderLayout.NORTH);
        add(statsPanel, BorderLayout.CENTER);
        add(actionsPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 15, 15));
        panel.setBorder(BorderFactory.createTitledBorder("Store Statistics"));
        
        totalProductsLabel = createStatCard("Total Products", "0", Color.BLUE);
        inventoryValueLabel = createStatCard("Inventory Value", "$0.00", Color.GREEN);
        lowStockLabel = createStatCard("Low Stock Items", "0", Color.ORANGE);
        dailySalesLabel = createStatCard("Today's Sales", "$0.00", Color.RED);
        
        panel.add(totalProductsLabel);
        panel.add(inventoryValueLabel);
        panel.add(lowStockLabel);
        panel.add(dailySalesLabel);
        
        return panel;
    }
    
    private JLabel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        card.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titleLabel.setForeground(Color.DARK_GRAY);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(color);
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        JLabel container = new JLabel();
        container.setLayout(new BorderLayout());
        container.add(card, BorderLayout.CENTER);
        
        return container;
    }
    
    private JPanel createQuickActionsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 15, 15));
        panel.setBorder(BorderFactory.createTitledBorder("Quick Actions"));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        
        panel.add(createActionButton("New Sale", "Start a new sales transaction"));
        panel.add(createActionButton("Add Product", "Add new product to inventory"));
        panel.add(createActionButton("View Reports", "Generate sales and inventory reports"));
        panel.add(createActionButton("Backup Data", "Create system backup"));
        
        return panel;
    }
    
    private JButton createActionButton(String text, String tooltip) {
        JButton button = new JButton(text);
        button.setToolTipText(tooltip);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBackground(Color.WHITE);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        return button;
    }
    
    private void updateDashboard() {
        try {
            Map<String, Object> stats = inventoryService.getInventoryStats();
            
            totalProductsLabel.getComponent(0).getComponent(0).setFont(
                new Font("Segoe UI", Font.BOLD, 24));
            ((JLabel)((JPanel)totalProductsLabel.getComponent(0)).getComponent(1))
                .setText(stats.get("totalProducts").toString());
            
            ((JLabel)((JPanel)inventoryValueLabel.getComponent(0)).getComponent(1))
                .setText(String.format("$%.2f", (Double)stats.get("totalValue")));
            
            ((JLabel)((JPanel)lowStockLabel.getComponent(0)).getComponent(1))
                .setText(stats.get("lowStockCount").toString());
            
            // Calculate today's sales
            java.time.LocalDateTime startOfDay = java.time.LocalDate.now().atStartOfDay();
            java.time.LocalDateTime endOfDay = startOfDay.plusDays(1);
            double dailySales = orderService.getTotalSales(startOfDay, endOfDay);
            
            ((JLabel)((JPanel)dailySalesLabel.getComponent(0)).getComponent(1))
                .setText(String.format("$%.2f", dailySales));
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error updating dashboard: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}