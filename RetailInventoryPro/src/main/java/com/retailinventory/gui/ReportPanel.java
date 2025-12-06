package com.retailinventory.gui;

import com.retailinventory.service.ReportService;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ReportPanel extends JPanel {
    private ReportService reportService;
    
    private JComboBox<String> reportTypeCombo;
    private JSpinner startDateSpinner;
    private JSpinner endDateSpinner;
    private JTextArea reportTextArea;
    
    public ReportPanel(ReportService reportService) {
        this.reportService = reportService;
        initializeUI();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Control panel
        JPanel controlPanel = createControlPanel();
        add(controlPanel, BorderLayout.NORTH);
        
        // Report display area
        reportTextArea = new JTextArea(20, 60);
        reportTextArea.setEditable(false);
        reportTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(reportTextArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Report Output"));
        
        add(scrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Report Parameters"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        int row = 0;
        
        // Report Type
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel("Report Type:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = row++;
        reportTypeCombo = new JComboBox<>(new String[]{
            "Daily Sales Report",
            "Inventory Report",
            "Sales by Category",
            "Profit & Loss"
        });
        panel.add(reportTypeCombo, gbc);
        
        // Start Date
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel("Start Date:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = row++;
        startDateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor startEditor = new JSpinner.DateEditor(startDateSpinner, "yyyy-MM-dd");
        startDateSpinner.setEditor(startEditor);
        startDateSpinner.setValue(java.util.Calendar.getInstance().getTime());
        panel.add(startDateSpinner, gbc);
        
        // End Date
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel("End Date:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = row++;
        endDateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor endEditor = new JSpinner.DateEditor(endDateSpinner, "yyyy-MM-dd");
        endDateSpinner.setEditor(endEditor);
        endDateSpinner.setValue(java.util.Calendar.getInstance().getTime());
        panel.add(endDateSpinner, gbc);
        
        return panel;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        
        JButton generateButton = new JButton("Generate Report");
        JButton saveButton = new JButton("Save to File");
        JButton printButton = new JButton("Print");
        JButton clearButton = new JButton("Clear");
        
        generateButton.addActionListener(e -> generateReport());
        saveButton.addActionListener(e -> saveReport());
        clearButton.addActionListener(e -> reportTextArea.setText(""));
        
        panel.add(generateButton);
        panel.add(saveButton);
        panel.add(printButton);
        panel.add(clearButton);
        
        return panel;
    }
    
    private void generateReport() {
        try {
            String reportType = (String) reportTypeCombo.getSelectedItem();
            
            java.util.Date startDate = (java.util.Date) startDateSpinner.getValue();
            java.util.Date endDate = (java.util.Date) endDateSpinner.getValue();
            
            LocalDate start = startDate.toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate();
            LocalDate end = endDate.toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate();
            
            String reportContent = "";
            
            switch (reportType) {
                case "Daily Sales Report":
                    reportService.generateDailyReport();
                    reportContent = "Daily report generated. Check the reports folder.";
                    break;
                    
                case "Inventory Report":
                    reportService.generateInventoryReport();
                    reportContent = "Inventory report generated. Check the reports folder.";
                    break;
                    
                case "Sales by Category":
                    // Implement sales by category report
                    reportContent = generateSalesByCategoryReport(start, end);
                    break;
                    
                case "Profit & Loss":
                    reportContent = reportService.generateProfitLossReport(
                        start.atStartOfDay(),
                        end.plusDays(1).atStartOfDay()
                    );
                    break;
            }
            
            reportTextArea.setText(reportContent);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error generating report: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private String generateSalesByCategoryReport(LocalDate start, LocalDate end) {
        try {
            java.time.LocalDateTime startDateTime = start.atStartOfDay();
            java.time.LocalDateTime endDateTime = end.plusDays(1).atStartOfDay();
            
            var salesMetrics = reportService.getSalesMetrics(startDateTime, endDateTime);
            
            StringBuilder report = new StringBuilder();
            report.append("=== SALES BY CATEGORY REPORT ===\n");
            report.append("Period: ").append(start).append(" to ").append(end).append("\n");
            report.append("Generated: ").append(java.time.LocalDateTime.now()).append("\n\n");
            
            report.append("SUMMARY:\n");
            report.append(String.format("Total Sales: $%.2f\n", 
                (Double)salesMetrics.get("totalSales")));
            report.append(String.format("Total Transactions: %d\n", 
                salesMetrics.get("totalTransactions")));
            report.append(String.format("Average Transaction: $%.2f\n\n", 
                (Double)salesMetrics.get("averageTransaction")));
            
            return report.toString();
            
        } catch (Exception e) {
            return "Error generating sales report: " + e.getMessage();
        }
    }
    
    private void saveReport() {
        if (reportTextArea.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "No report content to save.",
                "Warning",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File(
            "report_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".txt"));
        
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                java.nio.file.Files.writeString(
                    fileChooser.getSelectedFile().toPath(),
                    reportTextArea.getText()
                );
                
                JOptionPane.showMessageDialog(this,
                    "Report saved successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
                    
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Error saving report: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}import java.io.File;
