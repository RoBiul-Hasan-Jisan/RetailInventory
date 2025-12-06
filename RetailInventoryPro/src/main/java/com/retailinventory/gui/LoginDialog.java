package com.retailinventory.gui;

import com.retailinventory.model.User;
import com.retailinventory.service.UserService;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LoginDialog extends JDialog {
    private UserService userService;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton, cancelButton;
    private boolean authenticated;
    private User authenticatedUser;
    
    public LoginDialog(JFrame parent) {
        super(parent, "Login - Retail Inventory Pro", true);
        this.userService = new UserService();
        this.authenticated = false;
        
        initializeUI();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setSize(400, 250);
        setResizable(false);
        setLocationRelativeTo(getParent());
        
        // Header panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(52, 152, 219));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JLabel titleLabel = new JLabel("RETAIL INVENTORY PRO");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 10, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Username
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Username:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        usernameField = new JTextField(15);
        usernameField.addActionListener(e -> passwordField.requestFocus());
        formPanel.add(usernameField, gbc);
        
        // Password
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        formPanel.add(new JLabel("Password:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        passwordField = new JPasswordField(15);
        passwordField.addActionListener(e -> attemptLogin());
        formPanel.add(passwordField, gbc);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        
        loginButton = new JButton("Login");
        cancelButton = new JButton("Cancel");
        
        loginButton.setBackground(new Color(46, 204, 113));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginButton.addActionListener(e -> attemptLogin());
        
        cancelButton.setBackground(new Color(231, 76, 60));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        cancelButton.addActionListener(e -> {
            authenticated = false;
            dispose();
        });
        
        buttonPanel.add(loginButton);
        buttonPanel.add(cancelButton);
        
        // Add components
        add(headerPanel, BorderLayout.NORTH);
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Set default button
        getRootPane().setDefaultButton(loginButton);
        
        // Set focus
        SwingUtilities.invokeLater(() -> usernameField.requestFocus());
    }
    
    private void attemptLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter both username and password.",
                "Login Error",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        User user = userService.authenticate(username, password);
        if (user != null) {
            authenticated = true;
            authenticatedUser = user;
            dispose();
            
            // Launch main application window
            SwingUtilities.invokeLater(() -> {
                MainWindow mainWindow = new MainWindow(user);
                mainWindow.setVisible(true);
            });
        } else {
            JOptionPane.showMessageDialog(this,
                "Invalid username or password. Please try again.",
                "Login Failed",
                JOptionPane.ERROR_MESSAGE);
            
            passwordField.setText("");
            passwordField.requestFocus();
        }
    }
    
    public boolean isAuthenticated() {
        return authenticated;
    }
    
    public User getAuthenticatedUser() {
        return authenticatedUser;
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginDialog loginDialog = new LoginDialog(null);
            loginDialog.setVisible(true);
            
            if (!loginDialog.isAuthenticated()) {
                System.exit(0);
            }
        });
    }
}