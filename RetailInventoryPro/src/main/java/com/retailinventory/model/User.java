package com.retailinventory.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class User {
    private String userId;
    private String username;
    private String password;
    private String fullName;
    private String email;
    private String phone;
    private String role; // ADMIN, MANAGER, CASHIER, STOCK_CLERK
    private LocalDate hireDate;
    private LocalDateTime lastLogin;
    private boolean isActive;
    private String department;
    private double salary;
    
    public User() {
        this.userId = "USER" + System.currentTimeMillis() % 10000;
        this.hireDate = LocalDate.now();
        this.isActive = true;
        this.role = "CASHIER";
    }
    
    public User(String username, String password, String fullName, String role) {
        this();
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
    }
    
    public boolean hasPermission(String permission) {
        switch (role) {
            case "ADMIN":
                return true;
            case "MANAGER":
                return !permission.equals("USER_MANAGEMENT");
            case "CASHIER":
                return permission.equals("SALES") || permission.equals("PRODUCT_VIEW");
            case "STOCK_CLERK":
                return permission.equals("INVENTORY") || permission.equals("PRODUCT_EDIT");
            default:
                return false;
        }
    }
    
    public void login() {
        this.lastLogin = LocalDateTime.now();
    }
    
    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }
    
    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    
    public double getSalary() { return salary; }
    public void setSalary(double salary) { this.salary = salary; }
    
    @Override
    public String toString() {
        return String.format("%s (%s)", fullName, role);
    }
}