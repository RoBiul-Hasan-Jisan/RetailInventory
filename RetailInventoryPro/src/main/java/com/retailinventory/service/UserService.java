package com.retailinventory.service;

import com.retailinventory.model.User;
import com.retailinventory.exception.FileProcessingException;
import java.util.*;
import java.time.LocalDateTime;

public class UserService {
    private Map<String, User> users;
    private FileDataService fileDataService;
    private User currentUser;
    
    public UserService() {
        this.users = new HashMap<>();
        this.fileDataService = new FileDataService();
        loadUsers();
    }
    
    private void loadUsers() {
        try {
            List<User> userList = fileDataService.loadUsers();
            for (User user : userList) {
                users.put(user.getUsername(), user);
            }
        } catch (Exception e) {
            System.err.println("Error loading users: " + e.getMessage());
            createDefaultUsers();
        }
    }
    
    public User authenticate(String username, String password) {
        User user = users.get(username);
        if (user != null && user.isActive() && user.getPassword().equals(password)) {
            user.login();
            currentUser = user;
            
            try {
                fileDataService.saveUser(user);
            } catch (Exception e) {
                System.err.println("Failed to update login time: " + e.getMessage());
            }
            
            return user;
        }
        return null;
    }
    
    public void logout() {
        currentUser = null;
    }
    
    public User getCurrentUser() {
        return currentUser;
    }
    
    public boolean hasPermission(String permission) {
        return currentUser != null && currentUser.hasPermission(permission);
    }
    
    public void addUser(User user) throws Exception {
        if (users.containsKey(user.getUsername())) {
            throw new Exception("Username already exists: " + user.getUsername());
        }
        
        users.put(user.getUsername(), user);
        fileDataService.saveUser(user);
    }
    
    public void updateUser(User user) throws Exception {
        if (!users.containsKey(user.getUsername())) {
            throw new Exception("User not found: " + user.getUsername());
        }
        
        users.put(user.getUsername(), user);
        fileDataService.saveUser(user);
    }
    
    public void deleteUser(String username) throws Exception {
        if (!users.containsKey(username)) {
            throw new Exception("User not found: " + username);
        }
        
        if (username.equals(currentUser.getUsername())) {
            throw new Exception("Cannot delete currently logged in user");
        }
        
        users.remove(username);
        fileDataService.deleteUser(username);
    }
    
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }
    
    public User getUser(String username) {
        return users.get(username);
    }
    
    private void createDefaultUsers() {
        try {
            User admin = new User("admin", "admin123", "System Administrator", "ADMIN");
            admin.setEmail("admin@retailstore.com");
            
            User manager = new User("manager", "manager123", "Store Manager", "MANAGER");
            manager.setEmail("manager@retailstore.com");
            
            User cashier = new User("cashier", "cashier123", "Sales Cashier", "CASHIER");
            cashier.setEmail("cashier@retailstore.com");
            
            users.put(admin.getUsername(), admin);
            users.put(manager.getUsername(), manager);
            users.put(cashier.getUsername(), cashier);
            
            fileDataService.saveAllUsers(new ArrayList<>(users.values()));
            
        } catch (Exception e) {
            System.err.println("Failed to create default users: " + e.getMessage());
        }
    }
}