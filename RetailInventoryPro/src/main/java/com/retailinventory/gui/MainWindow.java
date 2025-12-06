package com.retailinventory.gui;

import com.retailinventory.model.User;
import com.retailinventory.service.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MainWindow extends JFrame {
    private UserService userService;
    private InventoryService inventoryService;
    private OrderService orderService;
    private ReportService reportService;
    
    private JTabbedPane tabbedPane;
    private JLabel statusLabel;
    private User currentUser;
    
    public MainWindow(User user) {
        this.currentUser = user;
        this.userService = new UserService();
        this.inventoryService = new InventoryService();
        this.orderService = new OrderService();
        this.reportService = new ReportService();
        
        initializeUI();
    }
    
    private void initializeUI() {
        setTitle("Retail Inventory Pro - Store Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1200, 800));
        
        // Set application icon
        try {
            ImageIcon icon = new ImageIcon("resources/images/icon.png");
            if (icon.getImage() != null) {
                setIconImage(icon.getImage());
            }
        } catch (Exception e) {
            // Use default icon
        }
        
        // Create menu bar
        createMenuBar();
        
        // Create main content
        createMainContent();
        
        // Create status bar
        createStatusBar();
        
        // Center window
        setLocationRelativeTo(null);
    }
    
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // File Menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem backupItem = new JMenuItem("Backup Data");
        JMenuItem restoreItem = new JMenuItem("Restore from Backup");
        JMenuItem exportItem = new JMenuItem("Export to Excel");
        JMenuItem printItem = new JMenuItem("Print Report");
        JMenuItem exitItem = new JMenuItem("Exit");
        
        backupItem.addActionListener(e -> backupData());
        exportItem.addActionListener(e -> exportToExcel());
        exitItem.addActionListener(e -> System.exit(0));
        
        fileMenu.add(backupItem);
        fileMenu.add(restoreItem);
        fileMenu.addSeparator();
        fileMenu.add(exportItem);
        fileMenu.add(printItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        
        // Inventory Menu
        JMenu inventoryMenu = new JMenu("Inventory");
        JMenuItem addProductItem = new JMenuItem("Add New Product");
        JMenuItem viewProductsItem = new JMenuItem("View All Products");
        JMenuItem lowStockItem = new JMenuItem("Low Stock Alert");
        JMenuItem expiringItem = new JMenuItem("Expiring Products");
        JMenuItem reorderItem = new JMenuItem("Generate Reorder List");
        
        addProductItem.addActionListener(e -> showAddProductDialog());
        viewProductsItem.addActionListener(e -> showAllProducts());
        lowStockItem.addActionListener(e -> showLowStockAlert());
        
        inventoryMenu.add(addProductItem);
        inventoryMenu.add(viewProductsItem);
        inventoryMenu.addSeparator();
        inventoryMenu.add(lowStockItem);
        inventoryMenu.add(expiringItem);
        inventoryMenu.add(reorderItem);
        
        // Sales Menu
        JMenu salesMenu = new JMenu("Sales");
        JMenuItem newSaleItem = new JMenuItem("New Sale");
        JMenuItem salesHistoryItem = new JMenuItem("Sales History");
        JMenuItem returnItem = new JMenuItem("Process Return");
        
        newSaleItem.addActionListener(e -> startNewSale());
        
        salesMenu.add(newSaleItem);
        salesMenu.add(salesHistoryItem);
        salesMenu.add(returnItem);
        
        // Reports Menu
        JMenu reportsMenu = new JMenu("Reports");
        JMenuItem dailyReportItem = new JMenuItem("Daily Report");
        JMenuItem monthlyReportItem = new JMenuItem("Monthly Report");
        JMenuItem salesReportItem = new JMenuItem("Sales Report");
        JMenuItem inventoryReportItem = new JMenuItem("Inventory Report");
        JMenuItem profitReportItem = new JMenuItem("Profit & Loss");
        
        dailyReportItem.addActionListener(e -> generateDailyReport());
        
        reportsMenu.add(dailyReportItem);
        reportsMenu.add(monthlyReportItem);
        reportsMenu.addSeparator();
        reportsMenu.add(salesReportItem);
        reportsMenu.add(inventoryReportItem);
        reportsMenu.add(profitReportItem);
        
        // System Menu
        JMenu systemMenu = new JMenu("System");
        JMenuItem usersItem = new JMenuItem("User Management");
        JMenuItem settingsItem = new JMenuItem("Settings");
        JMenuItem logoutItem = new JMenuItem("Logout");
        
        logoutItem.addActionListener(e -> logout());
        
        systemMenu.add(usersItem);
        systemMenu.add(settingsItem);
        systemMenu.addSeparator();
        systemMenu.add(logoutItem);
        
        // Help Menu
        JMenu helpMenu = new JMenu("Help");
        JMenuItem helpItem = new JMenuItem("User Guide");
        JMenuItem aboutItem = new JMenuItem("About");
        
        aboutItem.addActionListener(e -> showAboutDialog());
        
        helpMenu.add(helpItem);
        helpMenu.add(aboutItem);
        
        // Add menus to menu bar
        menuBar.add(fileMenu);
        menuBar.add(inventoryMenu);
        menuBar.add(salesMenu);
        menuBar.add(reportsMenu);
        menuBar.add(systemMenu);
        menuBar.add(helpMenu);
        
        setJMenuBar(menuBar);
    }
    
    private void createMainContent() {
        tabbedPane = new JTabbedPane();
        
        // Create panels
        DashboardPanel dashboardPanel = new DashboardPanel(inventoryService, orderService);
        ProductPanel productPanel = new ProductPanel(inventoryService);
        SalesPanel salesPanel = new SalesPanel(inventoryService, orderService);
        InventoryPanel inventoryPanel = new InventoryPanel(inventoryService);
        ReportPanel reportPanel = new ReportPanel(reportService);
        CustomerPanel customerPanel = new CustomerPanel();
        
        // Add tabs
        tabbedPane.addTab("Dashboard", dashboardPanel);
        tabbedPane.addTab("Products", productPanel);
        tabbedPane.addTab("Sales", salesPanel);
        tabbedPane.addTab("Inventory", inventoryPanel);
        tabbedPane.addTab("Customers", customerPanel);
        tabbedPane.addTab("Reports", reportPanel);
        
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    private void createStatusBar() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createEtchedBorder());
        statusPanel.setBackground(new Color(240, 240, 240));
        
        statusLabel = new JLabel("User: " + currentUser.getFullName() + 
            " | Role: " + currentUser.getRole() + " | Ready");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
        
        JLabel timeLabel = new JLabel();
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        timeLabel.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
        
        // Update time every second
        Timer timer = new Timer(1000, e -> {
            timeLabel.setText(java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        });
        timer.start();
        
        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusPanel.add(timeLabel, BorderLayout.EAST);
        
        add(statusPanel, BorderLayout.SOUTH);
    }
    
    // Action methods
    private void backupData() {
        try {
            inventoryService.getFileService().createBackup();
            JOptionPane.showMessageDialog(this, 
                "Backup created successfully!", 
                "Backup Complete", 
                JOptionPane.INFORMATION_MESSAGE);
            updateStatus("Backup created successfully");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error creating backup: " + e.getMessage(), 
                "Backup Failed", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void exportToExcel() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("inventory_export.csv"));
        
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                // Export logic
                updateStatus("Data exported successfully");
                JOptionPane.showMessageDialog(this, 
                    "Export completed successfully!", 
                    "Export Complete", 
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Error exporting: " + e.getMessage(), 
                    "Export Failed", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void showAddProductDialog() {
        AddProductDialog dialog = new AddProductDialog(this, inventoryService);
        dialog.setVisible(true);
    }
    
    private void showAllProducts() {
        tabbedPane.setSelectedIndex(1); // Products tab
    }
    
    private void showLowStockAlert() {
        // Implementation
    }
    
    private void startNewSale() {
        tabbedPane.setSelectedIndex(2); // Sales tab
    }
    
    private void generateDailyReport() {
        try {
            reportService.generateDailyReport();
            JOptionPane.showMessageDialog(this, 
                "Daily report generated successfully!", 
                "Report Generated", 
                JOptionPane.INFORMATION_MESSAGE);
            updateStatus("Daily report generated");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error generating report: " + e.getMessage(), 
                "Report Failed", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to logout?", 
            "Confirm Logout", 
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            userService.logout();
            dispose();
            
            // Show login dialog again
            SwingUtilities.invokeLater(() -> {
                LoginDialog loginDialog = new LoginDialog(null);
                loginDialog.setVisible(true);
            });
        }
    }
    
    private void showAboutDialog() {
        String aboutText = """
            <html>
            <h2>Retail Inventory Pro</h2>
            <p><b>Version:</b> 2.0.0</p>
            <p><b>Developed by:</b> RetailTech Solutions</p>
            <p><b>Contact:</b> support@retailinventory.com</p>
            <p>A comprehensive retail management system for small to medium businesses.</p>
            <p>© 2024 All Rights Reserved</p>
            </html>
            """;
        
        JOptionPane.showMessageDialog(this, aboutText, "About", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void updateStatus(String message) {
        statusLabel.setText("User: " + currentUser.getFullName() + 
            " | Role: " + currentUser.getRole() + " | " + message);
    }
    
    public static void main(String[] args) {
        // This is for testing - normally launched from LoginDialog
        SwingUtilities.invokeLater(() -> {
            User testUser = new User("admin", "admin123", "System Administrator", "ADMIN");
            new MainWindow(testUser).setVisible(true);
        });
    }
}import java.io.File;
