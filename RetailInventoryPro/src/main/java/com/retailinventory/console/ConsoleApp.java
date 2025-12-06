package com.retailinventory.console;

import com.retailinventory.model.*;
import com.retailinventory.service.*;
import java.util.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ConsoleApp {
    private static Scanner scanner = new Scanner(System.in);
    private static InventoryService inventoryService;
    private static OrderService orderService;
    private static CustomerService customerService;
    private static UserService userService;
    private static ReportService reportService;
    
    private static User currentUser;
    
    public static void main(String[] args) {
        System.out.println("=== Retail Inventory Pro - Console Version ===\n");
        
        // Initialize services
        inventoryService = new InventoryService();
        orderService = new OrderService();
        customerService = new CustomerService();
        userService = new UserService();
        reportService = new ReportService();
        
        // Login
        if (!login()) {
            System.out.println("Login failed. Exiting...");
            return;
        }
        
        // Main menu
        boolean running = true;
        while (running) {
            displayMainMenu();
            System.out.print("\nEnter your choice: ");
            
            try {
                int choice = Integer.parseInt(scanner.nextLine());
                
                switch (choice) {
                    case 1 -> productManagement();
                    case 2 -> inventoryManagement();
                    case 3 -> salesManagement();
                    case 4 -> customerManagement();
                    case 5 -> reportGeneration();
                    case 6 -> systemManagement();
                    case 0 -> {
                        System.out.println("Goodbye!");
                        running = false;
                    }
                    default -> System.out.println("Invalid choice. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
            
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
        }
    }
    
    private static boolean login() {
        System.out.println("=== LOGIN ===");
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        
        currentUser = userService.authenticate(username, password);
        if (currentUser != null) {
            System.out.println("\nWelcome, " + currentUser.getFullName() + "!");
            System.out.println("Role: " + currentUser.getRole());
            return true;
        } else {
            System.out.println("Invalid username or password.");
            return false;
        }
    }
    
    private static void displayMainMenu() {
        System.out.println("\n=== MAIN MENU ===");
        System.out.println("1. Product Management");
        System.out.println("2. Inventory Management");
        System.out.println("3. Sales Management");
        System.out.println("4. Customer Management");
        System.out.println("5. Report Generation");
        System.out.println("6. System Management");
        System.out.println("0. Exit");
        System.out.println("=================");
    }
    
    private static void productManagement() {
        boolean inMenu = true;
        while (inMenu) {
            System.out.println("\n=== PRODUCT MANAGEMENT ===");
            System.out.println("1. View All Products");
            System.out.println("2. Search Products");
            System.out.println("3. Add New Product");
            System.out.println("4. Update Product");
            System.out.println("5. Delete Product");
            System.out.println("6. View Product Details");
            System.out.println("0. Back to Main Menu");
            System.out.println("==========================");
            
            System.out.print("Enter choice: ");
            int choice = Integer.parseInt(scanner.nextLine());
            
            switch (choice) {
                case 1 -> viewAllProducts();
                case 2 -> searchProducts();
                case 3 -> addProduct();
                case 4 -> updateProduct();
                case 5 -> deleteProduct();
                case 6 -> viewProductDetails();
                case 0 -> inMenu = false;
                default -> System.out.println("Invalid choice.");
            }
        }
    }
    
    private static void viewAllProducts() {
        System.out.println("\n=== ALL PRODUCTS ===");
        List<Product> products = inventoryService.getAllProducts();
        
        if (products.isEmpty()) {
            System.out.println("No products found.");
            return;
        }
        
        System.out.printf("%-15s %-30s %-15s %-10s %-10s %-10s%n",
            "Product ID", "Name", "Category", "Price", "Stock", "Status");
        System.out.println("-".repeat(90));
        
        for (Product product : products) {
            String status;
            if (product.getQuantityInStock() <= 0) {
                status = "Out of Stock";
            } else if (product.needsReorder()) {
                status = "Low Stock";
            } else {
                status = "In Stock";
            }
            
            System.out.printf("%-15s %-30s %-15s $%-9.2f %-10d %-10s%n",
                product.getProductId(),
                product.getName().length() > 30 ? 
                    product.getName().substring(0, 27) + "..." : product.getName(),
                product.getCategory(),
                product.getSellingPrice(),
                product.getQuantityInStock(),
                status);
        }
    }
    
    private static void searchProducts() {
        System.out.print("\nEnter search keyword: ");
        String keyword = scanner.nextLine();
        
        List<Product> results = inventoryService.searchProducts(keyword);
        
        if (results.isEmpty()) {
            System.out.println("No products found.");
            return;
        }
        
        System.out.println("\n=== SEARCH RESULTS ===");
        for (Product product : results) {
            System.out.printf("%s - %s (Stock: %d, Price: $%.2f)%n",
                product.getProductId(), product.getName(),
                product.getQuantityInStock(), product.getSellingPrice());
        }
    }
    
    private static void addProduct() {
        System.out.println("\n=== ADD NEW PRODUCT ===");
        
        try {
            System.out.print("Product Name: ");
            String name = scanner.nextLine();
            
            System.out.print("Category: ");
            String category = scanner.nextLine();
            
            System.out.print("Purchase Price: ");
            double purchasePrice = Double.parseDouble(scanner.nextLine());
            
            System.out.print("Selling Price: ");
            double sellingPrice = Double.parseDouble(scanner.nextLine());
            
            System.out.print("Initial Quantity: ");
            int quantity = Integer.parseInt(scanner.nextLine());
            
            System.out.print("Minimum Stock Level: ");
            int minStock = Integer.parseInt(scanner.nextLine());
            
            System.out.print("Maximum Stock Level: ");
            int maxStock = Integer.parseInt(scanner.nextLine());
            
            Product product = new Product(name, category, purchasePrice, sellingPrice, quantity);
            product.setMinStockLevel(minStock);
            product.setMaxStockLevel(maxStock);
            
            inventoryService.addProduct(product);
            
            System.out.println("\nProduct added successfully!");
            System.out.println("Product ID: " + product.getProductId());
            System.out.println("Barcode: " + product.getBarcode());
            
        } catch (Exception e) {
            System.out.println("Error adding product: " + e.getMessage());
        }
    }
    
    private static void inventoryManagement() {
        boolean inMenu = true;
        while (inMenu) {
            System.out.println("\n=== INVENTORY MANAGEMENT ===");
            System.out.println("1. View Inventory Status");
            System.out.println("2. Add Stock");
            System.out.println("3. View Low Stock Items");
            System.out.println("4. View Expiring Products");
            System.out.println("5. Generate Reorder List");
            System.out.println("6. View Inventory Statistics");
            System.out.println("0. Back to Main Menu");
            System.out.println("=============================");
            
            System.out.print("Enter choice: ");
            int choice = Integer.parseInt(scanner.nextLine());
            
            switch (choice) {
                case 1 -> viewInventoryStatus();
                case 2 -> addStock();
                case 3 -> viewLowStockItems();
                case 4 -> viewExpiringProducts();
                case 5 -> generateReorderList();
                case 6 -> viewInventoryStatistics();
                case 0 -> inMenu = false;
                default -> System.out.println("Invalid choice.");
            }
        }
    }
    
    private static void viewInventoryStatus() {
        System.out.println("\n=== INVENTORY STATUS ===");
        
        Map<String, Object> stats = inventoryService.getInventoryStats();
        
        System.out.printf("Total Products: %d%n", stats.get("totalProducts"));
        System.out.printf("Total Inventory Value: $%.2f%n", stats.get("totalValue"));
        System.out.printf("Low Stock Items: %d%n", stats.get("lowStockCount"));
        System.out.printf("Expired Items: %d%n", stats.get("expiredCount"));
        
        System.out.println("\nCategory Summary:");
        var categorySummary = inventoryService.getCategorySummary();
        if (categorySummary.isEmpty()) {
            System.out.println("No categories found.");
        } else {
            System.out.printf("%-20s %-10s %-15s %-15s%n",
                "Category", "Products", "Total Stock", "Total Value");
            System.out.println("-".repeat(60));
            
            categorySummary.forEach((category, summary) -> {
                System.out.printf("%-20s %-10d %-15d $%-14.2f%n",
                    category,
                    summary.getProductCount(),
                    summary.getTotalStock(),
                    summary.getTotalValue());
            });
        }
    }
    
    private static void addStock() {
        System.out.println("\n=== ADD STOCK ===");
        
        try {
            System.out.print("Enter Product ID: ");
            String productId = scanner.nextLine();
            
            Product product = inventoryService.getProduct(productId);
            if (product == null) {
                System.out.println("Product not found.");
                return;
            }
            
            System.out.println("Product: " + product.getName());
            System.out.println("Current Stock: " + product.getQuantityInStock());
            System.out.println("Max Stock: " + product.getMaxStockLevel());
            
            System.out.print("Quantity to add: ");
            int quantity = Integer.parseInt(scanner.nextLine());
            
            System.out.print("Batch Number (optional): ");
            String batch = scanner.nextLine();
            
            LocalDate expiryDate = null;
            if (product.isPerishable()) {
                System.out.print("Expiry Date (YYYY-MM-DD, optional): ");
                String expiryStr = scanner.nextLine();
                if (!expiryStr.isEmpty()) {
                    expiryDate = LocalDate.parse(expiryStr);
                }
            }
            
            inventoryService.addStock(productId, quantity, batch, expiryDate);
            System.out.println("Stock added successfully!");
            
        } catch (Exception e) {
            System.out.println("Error adding stock: " + e.getMessage());
        }
    }
    
    private static void viewLowStockItems() {
        System.out.println("\n=== LOW STOCK ITEMS ===");
        
        List<Product> lowStockItems = inventoryService.getProductsNeedingReorder();
        
        if (lowStockItems.isEmpty()) {
            System.out.println("No products are low on stock.");
            return;
        }
        
        System.out.printf("%-15s %-30s %-15s %-10s %-10s%n",
            "Product ID", "Product Name", "Category", "Current", "Min Level");
        System.out.println("-".repeat(80));
        
        for (Product product : lowStockItems) {
            System.out.printf("%-15s %-30s %-15s %-10d %-10d%n",
                product.getProductId(),
                product.getName().length() > 30 ? 
                    product.getName().substring(0, 27) + "..." : product.getName(),
                product.getCategory(),
                product.getQuantityInStock(),
                product.getMinStockLevel());
        }
    }
    
    private static void salesManagement() {
        boolean inMenu = true;
        while (inMenu) {
            System.out.println("\n=== SALES MANAGEMENT ===");
            System.out.println("1. New Sale");
            System.out.println("2. View Sales History");
            System.out.println("3. Process Return");
            System.out.println("4. View Order Details");
            System.out.println("0. Back to Main Menu");
            System.out.println("========================");
            
            System.out.print("Enter choice: ");
            int choice = Integer.parseInt(scanner.nextLine());
            
            switch (choice) {
                case 1 -> newSale();
                case 2 -> viewSalesHistory();
                case 3 -> processReturn();
                case 4 -> viewOrderDetails();
                case 0 -> inMenu = false;
                default -> System.out.println("Invalid choice.");
            }
        }
    }
    
    private static void newSale() {
        System.out.println("\n=== NEW SALE ===");
        
        try {
            List<OrderItem> cartItems = new ArrayList<>();
            boolean addingItems = true;
            
            while (addingItems) {
                System.out.print("Enter Product ID (or 'done' to finish): ");
                String input = scanner.nextLine();
                
                if (input.equalsIgnoreCase("done")) {
                    addingItems = false;
                    continue;
                }
                
                Product product = inventoryService.getProduct(input);
                if (product == null) {
                    System.out.println("Product not found.");
                    continue;
                }
                
                System.out.println("Product: " + product.getName());
                System.out.println("Price: $" + product.getSellingPrice());
                System.out.println("Stock: " + product.getQuantityInStock());
                
                System.out.print("Quantity: ");
                int quantity = Integer.parseInt(scanner.nextLine());
                
                if (quantity > product.getQuantityInStock()) {
                    System.out.println("Insufficient stock!");
                    continue;
                }
                
                OrderItem item = new OrderItem(
                    product.getProductId(),
                    product.getName(),
                    product.getSellingPrice(),
                    quantity
                );
                cartItems.add(item);
                
                System.out.println("Added to cart: " + product.getName() + " x" + quantity);
            }
            
            if (cartItems.isEmpty()) {
                System.out.println("Sale cancelled - no items in cart.");
                return;
            }
            
            System.out.print("Customer ID (or 'WALK-IN'): ");
            String customerId = scanner.nextLine();
            
            System.out.print("Payment Method (Cash/Card): ");
            String paymentMethod = scanner.nextLine();
            
            // Create and process order
            Order order = orderService.createOrder(customerId, cartItems);
            order.setPaymentMethod(paymentMethod);
            orderService.processOrder(order.getOrderId());
            
            System.out.println("\n=== RECEIPT ===");
            System.out.println("Order #: " + order.getOrderId());
            System.out.println("Date: " + order.getOrderDate());
            System.out.println("Customer: " + order.getCustomerId());
            System.out.println("\nItems:");
            
            for (OrderItem item : order.getItems()) {
                System.out.printf("  %s x%d @ $%.2f = $%.2f%n",
                    item.getProductName(), item.getQuantity(),
                    item.getPrice(), item.getPrice() * item.getQuantity());
            }
            
            System.out.printf("%nSubtotal: $%.2f%n", order.getTotalAmount());
            System.out.printf("Tax: $%.2f%n", order.getTax());
            System.out.printf("TOTAL: $%.2f%n", order.getFinalAmount());
            System.out.println("Payment: " + order.getPaymentMethod());
            System.out.println("\nThank you for your purchase!");
            
        } catch (Exception e) {
            System.out.println("Error processing sale: " + e.getMessage());
        }
    }
    
    private static void viewSalesHistory() {
        System.out.println("\n=== SALES HISTORY ===");
        
        List<Order> orders = orderService.getAllOrders();
        
        if (orders.isEmpty()) {
            System.out.println("No sales history found.");
            return;
        }
        
        System.out.printf("%-15s %-12s %-15s %-10s %-15s%n",
            "Order ID", "Date", "Customer", "Items", "Total");
        System.out.println("-".repeat(70));
        
        for (Order order : orders) {
            String date = order.getOrderDate().format(
                DateTimeFormatter.ofPattern("MM/dd/yyyy"));
            
            System.out.printf("%-15s %-12s %-15s %-10d $%-14.2f%n",
                order.getOrderId(),
                date,
                order.getCustomerId(),
                order.getItems().size(),
                order.getFinalAmount());
        }
    }
    
    private static void customerManagement() {
        boolean inMenu = true;
        while (inMenu) {
            System.out.println("\n=== CUSTOMER MANAGEMENT ===");
            System.out.println("1. View All Customers");
            System.out.println("2. Search Customers");
            System.out.println("3. Add New Customer");
            System.out.println("4. View Customer Details");
            System.out.println("5. View Top Customers");
            System.out.println("0. Back to Main Menu");
            System.out.println("============================");
            
            System.out.print("Enter choice: ");
            int choice = Integer.parseInt(scanner.nextLine());
            
            switch (choice) {
                case 1 -> viewAllCustomers();
                case 2 -> searchCustomers();
                case 3 -> addCustomer();
                case 4 -> viewCustomerDetails();
                case 5 -> viewTopCustomers();
                case 0 -> inMenu = false;
                default -> System.out.println("Invalid choice.");
            }
        }
    }
    
    private static void viewAllCustomers() {
        System.out.println("\n=== ALL CUSTOMERS ===");
        
        List<Customer> customers = customerService.getAllCustomers();
        
        if (customers.isEmpty()) {
            System.out.println("No customers found.");
            return;
        }
        
        System.out.printf("%-15s %-20s %-20s %-15s %-10s%n",
            "Customer ID", "Name", "Email", "Phone", "Total Spent");
        System.out.println("-".repeat(80));
        
        for (Customer customer : customers) {
            System.out.printf("%-15s %-20s %-20s %-15s $%-9.2f%n",
                customer.getCustomerId(),
                customer.getFirstName() + " " + customer.getLastName(),
                customer.getEmail().length() > 20 ? 
                    customer.getEmail().substring(0, 17) + "..." : customer.getEmail(),
                customer.getPhone(),
                customer.getTotalPurchases());
        }
    }
    
    private static void searchCustomers() {
        System.out.print("\nEnter search keyword: ");
        String keyword = scanner.nextLine();
        
        List<Customer> results = customerService.searchCustomers(keyword);
        
        if (results.isEmpty()) {
            System.out.println("No customers found.");
            return;
        }
        
        System.out.println("\n=== SEARCH RESULTS ===");
        for (Customer customer : results) {
            System.out.printf("%s - %s %s (Spent: $%.2f)%n",
                customer.getCustomerId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getTotalPurchases());
        }
    }
    
    private static void addCustomer() {
        System.out.println("\n=== ADD NEW CUSTOMER ===");
        
        try {
            System.out.print("First Name: ");
            String firstName = scanner.nextLine();
            
            System.out.print("Last Name: ");
            String lastName = scanner.nextLine();
            
            System.out.print("Email: ");
            String email = scanner.nextLine();
            
            System.out.print("Phone: ");
            String phone = scanner.nextLine();
            
            System.out.print("Address (optional): ");
            String address = scanner.nextLine();
            
            Customer customer = new Customer(firstName, lastName, email, phone);
            if (!address.isEmpty()) {
                customer.setAddress(address);
            }
            
            customerService.addCustomer(customer);
            
            System.out.println("\nCustomer added successfully!");
            System.out.println("Customer ID: " + customer.getCustomerId());
            
        } catch (Exception e) {
            System.out.println("Error adding customer: " + e.getMessage());
        }
    }
    
    private static void reportGeneration() {
        boolean inMenu = true;
        while (inMenu) {
            System.out.println("\n=== REPORT GENERATION ===");
            System.out.println("1. Daily Sales Report");
            System.out.println("2. Inventory Report");
            System.out.println("3. Sales by Category");
            System.out.println("4. Profit & Loss Report");
            System.out.println("0. Back to Main Menu");
            System.out.println("=========================");
            
            System.out.print("Enter choice: ");
            int choice = Integer.parseInt(scanner.nextLine());
            
            switch (choice) {
                case 1 -> generateDailyReport();
                case 2 -> generateInventoryReport();
                case 3 -> generateSalesByCategoryReport();
                case 4 -> generateProfitLossReport();
                case 0 -> inMenu = false;
                default -> System.out.println("Invalid choice.");
            }
        }
    }
    
    private static void generateDailyReport() {
        try {
            reportService.generateDailyReport();
            System.out.println("Daily report generated successfully!");
            System.out.println("Check the data/reports/daily/ folder.");
        } catch (Exception e) {
            System.out.println("Error generating daily report: " + e.getMessage());
        }
    }
    
    private static void generateInventoryReport() {
        try {
            reportService.generateInventoryReport();
            System.out.println("Inventory report generated successfully!");
            System.out.println("Check the data/reports/inventory/ folder.");
        } catch (Exception e) {
            System.out.println("Error generating inventory report: " + e.getMessage());
        }
    }
    
    private static void generateSalesByCategoryReport() {
        System.out.println("\n=== SALES BY CATEGORY REPORT ===");
        
        try {
            System.out.print("Start Date (YYYY-MM-DD): ");
            LocalDate start = LocalDate.parse(scanner.nextLine());
            
            System.out.print("End Date (YYYY-MM-DD): ");
            LocalDate end = LocalDate.parse(scanner.nextLine());
            
            java.time.LocalDateTime startDateTime = start.atStartOfDay();
            java.time.LocalDateTime endDateTime = end.plusDays(1).atStartOfDay();
            
            var metrics = reportService.getSalesMetrics(startDateTime, endDateTime);
            
            System.out.println("\n=== REPORT SUMMARY ===");
            System.out.printf("Period: %s to %s%n", start, end);
            System.out.printf("Total Sales: $%.2f%n", metrics.get("totalSales"));
            System.out.printf("Total Transactions: %d%n", metrics.get("totalTransactions"));
            System.out.printf("Total Items Sold: %d%n", metrics.get("totalItems"));
            System.out.printf("Average Transaction: $%.2f%n", 
                metrics.get("averageTransaction"));
            
        } catch (Exception e) {
            System.out.println("Error generating report: " + e.getMessage());
        }
    }
    
    private static void generateProfitLossReport() {
        System.out.println("\n=== PROFIT & LOSS REPORT ===");
        
        try {
            System.out.print("Start Date (YYYY-MM-DD): ");
            LocalDate start = LocalDate.parse(scanner.nextLine());
            
            System.out.print("End Date (YYYY-MM-DD): ");
            LocalDate end = LocalDate.parse(scanner.nextLine());
            
            java.time.LocalDateTime startDateTime = start.atStartOfDay();
            java.time.LocalDateTime endDateTime = end.plusDays(1).atStartOfDay();
            
            String report = reportService.generateProfitLossReport(startDateTime, endDateTime);
            System.out.println("\n" + report);
            
        } catch (Exception e) {
            System.out.println("Error generating report: " + e.getMessage());
        }
    }
    
    private static void systemManagement() {
        boolean inMenu = true;
        while (inMenu) {
            System.out.println("\n=== SYSTEM MANAGEMENT ===");
            System.out.println("1. Backup Data");
            System.out.println("2. View System Info");
            System.out.println("3. User Management");
            System.out.println("0. Back to Main Menu");
            System.out.println("========================");
            
            System.out.print("Enter choice: ");
            int choice = Integer.parseInt(scanner.nextLine());
            
            switch (choice) {
                case 1 -> backupData();
                case 2 -> viewSystemInfo();
                case 3 -> userManagement();
                case 0 -> inMenu = false;
                default -> System.out.println("Invalid choice.");
            }
        }
    }
    
    private static void backupData() {
        System.out.println("\n=== DATA BACKUP ===");
        
        try {
            inventoryService.getFileService().createBackup();
            System.out.println("Backup created successfully!");
        } catch (Exception e) {
            System.out.println("Error creating backup: " + e.getMessage());
        }
    }
    
    private static void viewSystemInfo() {
        System.out.println("\n=== SYSTEM INFORMATION ===");
        System.out.println("Retail Inventory Pro v2.0.0");
        System.out.println("Java Version: " + System.getProperty("java.version"));
        System.out.println("OS: " + System.getProperty("os.name"));
        System.out.println("User: " + currentUser.getFullName());
        System.out.println("Role: " + currentUser.getRole());
        System.out.println("Current Time: " + java.time.LocalDateTime.now());
    }
    
    private static void userManagement() {
        System.out.println("\n=== USER MANAGEMENT ===");
        System.out.println("Note: Only administrators can manage users.");
        
        if (!currentUser.getRole().equals("ADMIN")) {
            System.out.println("Access denied. Administrator privileges required.");
            return;
        }
        
        // User management implementation would go here
        System.out.println("User management module coming soon...");
    }
    
    // Additional helper methods...
    private static void viewProductDetails() {
        System.out.print("\nEnter Product ID: ");
        String productId = scanner.nextLine();
        
        Product product = inventoryService.getProduct(productId);
        if (product == null) {
            System.out.println("Product not found.");
            return;
        }
        
        System.out.println("\n=== PRODUCT DETAILS ===");
        System.out.println("ID: " + product.getProductId());
        System.out.println("Name: " + product.getName());
        System.out.println("Barcode: " + product.getBarcode());
        System.out.println("Category: " + product.getCategory());
        System.out.println("Description: " + 
            (product.getDescription() != null ? product.getDescription() : "N/A"));
        System.out.println("Purchase Price: $" + product.getPurchasePrice());
        System.out.println("Selling Price: $" + product.getSellingPrice());
        System.out.println("Quantity in Stock: " + product.getQuantityInStock());
        System.out.println("Min Stock Level: " + product.getMinStockLevel());
        System.out.println("Max Stock Level: " + product.getMaxStockLevel());
        System.out.println("Quantity Sold: " + product.getQuantitySold());
        System.out.println("Supplier ID: " + 
            (product.getSupplierId() != null ? product.getSupplierId() : "N/A"));
        System.out.println("Location: " + 
            (product.getLocation() != null ? product.getLocation() : "N/A"));
        System.out.println("Perishable: " + (product.isPerishable() ? "Yes" : "No"));
        System.out.println("Expiry Date: " + 
            (product.getExpiryDate() != null ? product.getExpiryDate() : "N/A"));
        System.out.println("Last Restocked: " + 
            (product.getLastRestocked() != null ? product.getLastRestocked() : "N/A"));
        
        if (product.isExpired()) {
            System.out.println("STATUS: EXPIRED");
        } else if (product.needsReorder()) {
            System.out.println("STATUS: LOW STOCK");
        } else if (product.getQuantityInStock() <= 0) {
            System.out.println("STATUS: OUT OF STOCK");
        } else {
            System.out.println("STATUS: IN STOCK");
        }
    }
    
    private static void updateProduct() {
        System.out.print("\nEnter Product ID to update: ");
        String productId = scanner.nextLine();
        
        Product product = inventoryService.getProduct(productId);
        if (product == null) {
            System.out.println("Product not found.");
            return;
        }
        
        System.out.println("\nCurrent Product: " + product.getName());
        System.out.println("Leave field blank to keep current value.");
        
        try {
            System.out.print("New Name [" + product.getName() + "]: ");
            String name = scanner.nextLine();
            if (!name.isEmpty()) product.setName(name);
            
            System.out.print("New Category [" + product.getCategory() + "]: ");
            String category = scanner.nextLine();
            if (!category.isEmpty()) product.setCategory(category);
            
            System.out.print("New Purchase Price [" + product.getPurchasePrice() + "]: ");
            String purchasePriceStr = scanner.nextLine();
            if (!purchasePriceStr.isEmpty()) {
                product.setPurchasePrice(Double.parseDouble(purchasePriceStr));
            }
            
            System.out.print("New Selling Price [" + product.getSellingPrice() + "]: ");
            String sellingPriceStr = scanner.nextLine();
            if (!sellingPriceStr.isEmpty()) {
                product.setSellingPrice(Double.parseDouble(sellingPriceStr));
            }
            
            System.out.print("New Min Stock Level [" + product.getMinStockLevel() + "]: ");
            String minStockStr = scanner.nextLine();
            if (!minStockStr.isEmpty()) {
                product.setMinStockLevel(Integer.parseInt(minStockStr));
            }
            
            System.out.print("New Max Stock Level [" + product.getMaxStockLevel() + "]: ");
            String maxStockStr = scanner.nextLine();
            if (!maxStockStr.isEmpty()) {
                product.setMaxStockLevel(Integer.parseInt(maxStockStr));
            }
            
            inventoryService.updateProduct(product);
            System.out.println("Product updated successfully!");
            
        } catch (Exception e) {
            System.out.println("Error updating product: " + e.getMessage());
        }
    }
    
    private static void deleteProduct() {
        System.out.print("\nEnter Product ID to delete: ");
        String productId = scanner.nextLine();
        
        try {
            Product product = inventoryService.getProduct(productId);
            if (product == null) {
                System.out.println("Product not found.");
                return;
            }
            
            System.out.println("Are you sure you want to delete: " + product.getName() + "?");
            System.out.print("Type 'YES' to confirm: ");
            String confirmation = scanner.nextLine();
            
            if (confirmation.equalsIgnoreCase("YES")) {
                inventoryService.deleteProduct(productId);
                System.out.println("Product deleted successfully!");
            } else {
                System.out.println("Deletion cancelled.");
            }
            
        } catch (Exception e) {
            System.out.println("Error deleting product: " + e.getMessage());
        }
    }
    
    private static void viewExpiringProducts() {
        System.out.println("\n=== EXPIRING PRODUCTS (Next 30 Days) ===");
        
        List<Product> expiringProducts = inventoryService.getExpiringProducts(30);
        
        if (expiringProducts.isEmpty()) {
            System.out.println("No products are expiring within 30 days.");
            return;
        }
        
        System.out.printf("%-15s %-30s %-15s %-12s %-8s%n",
            "Product ID", "Product Name", "Category", "Expiry Date", "Days Left");
        System.out.println("-".repeat(85));
        
        for (Product product : expiringProducts) {
            long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(
                java.time.LocalDate.now(), product.getExpiryDate());
            
            System.out.printf("%-15s %-30s %-15s %-12s %-8d%n",
                product.getProductId(),
                product.getName().length() > 30 ? 
                    product.getName().substring(0, 27) + "..." : product.getName(),
                product.getCategory(),
                product.getExpiryDate(),
                daysLeft);
        }
    }
    
    private static void generateReorderList() {
        System.out.println("\n=== REORDER LIST ===");
        
        String reorderReport = inventoryService.generateReorderReport();
        System.out.println(reorderReport);
    }
    
    private static void viewInventoryStatistics() {
        System.out.println("\n=== INVENTORY STATISTICS ===");
        
        Map<String, Object> stats = inventoryService.getInventoryStats();
        
        System.out.printf("Total Products: %d%n", stats.get("totalProducts"));
        System.out.printf("Total Inventory Value: $%.2f%n", stats.get("totalValue"));
        System.out.printf("Total Potential Revenue: $%.2f%n", 
            stats.get("totalPotentialRevenue"));
        System.out.printf("Low Stock Items: %d%n", stats.get("lowStockCount"));
        System.out.printf("Expired Items: %d%n", stats.get("expiredCount"));
        System.out.printf("Last Updated: %s%n", stats.get("lastUpdated"));
        
        // Calculate additional statistics
        List<Product> products = inventoryService.getAllProducts();
        if (!products.isEmpty()) {
            double avgPrice = products.stream()
                .mapToDouble(Product::getSellingPrice)
                .average()
                .orElse(0.0);
            System.out.printf("Average Product Price: $%.2f%n", avgPrice);
            
            int totalStock = products.stream()
                .mapToInt(Product::getQuantityInStock)
                .sum();
            System.out.printf("Total Stock Units: %d%n", totalStock);
        }
    }
    
    private static void processReturn() {
        System.out.println("\n=== PROCESS RETURN ===");
        
        try {
            System.out.print("Enter Order ID: ");
            String orderId = scanner.nextLine();
            
            Order order = orderService.getOrder(orderId);
            if (order == null) {
                System.out.println("Order not found.");
                return;
            }
            
            System.out.println("Order Date: " + order.getOrderDate());
            System.out.println("Customer: " + order.getCustomerId());
            System.out.println("Total: $" + order.getFinalAmount());
            System.out.println("\nItems in order:");
            
            for (int i = 0; i < order.getItems().size(); i++) {
                OrderItem item = order.getItems().get(i);
                System.out.printf("%d. %s x%d @ $%.2f%n",
                    i + 1, item.getProductName(), item.getQuantity(), item.getPrice());
            }
            
            System.out.print("\nEnter item number to return (or 'all'): ");
            String input = scanner.nextLine();
            
            if (input.equalsIgnoreCase("all")) {
                System.out.print("Reason for return: ");
                String reason = scanner.nextLine();
                
                for (OrderItem item : order.getItems()) {
                    inventoryService.returnProduct(
                        item.getProductId(),
                        item.getQuantity(),
                        reason,
                        orderId
                    );
                }
                
                orderService.cancelOrder(orderId);
                System.out.println("Full return processed successfully!");
                
            } else {
                int itemIndex = Integer.parseInt(input) - 1;
                if (itemIndex < 0 || itemIndex >= order.getItems().size()) {
                    System.out.println("Invalid item number.");
                    return;
                }
                
                OrderItem item = order.getItems().get(itemIndex);
                System.out.println("Selected: " + item.getProductName() + " x" + item.getQuantity());
                
                System.out.print("Quantity to return: ");
                int quantity = Integer.parseInt(scanner.nextLine());
                
                if (quantity > item.getQuantity()) {
                    System.out.println("Cannot return more than purchased.");
                    return;
                }
                
                System.out.print("Reason for return: ");
                String reason = scanner.nextLine();
                
                inventoryService.returnProduct(
                    item.getProductId(),
                    quantity,
                    reason,
                    orderId
                );
                
                System.out.println("Return processed successfully!");
            }
            
        } catch (Exception e) {
            System.out.println("Error processing return: " + e.getMessage());
        }
    }
    
    private static void viewOrderDetails() {
        System.out.print("\nEnter Order ID: ");
        String orderId = scanner.nextLine();
        
        Order order = orderService.getOrder(orderId);
        if (order == null) {
            System.out.println("Order not found.");
            return;
        }
        
        System.out.println("\n=== ORDER DETAILS ===");
        System.out.println("Order ID: " + order.getOrderId());
        System.out.println("Customer ID: " + order.getCustomerId());
        System.out.println("Order Date: " + order.getOrderDate());
        System.out.println("Status: " + order.getStatus());
        System.out.println("Payment Method: " + order.getPaymentMethod());
        
        if (order.getCompletionDate() != null) {
            System.out.println("Completion Date: " + order.getCompletionDate());
        }
        
        System.out.println("\nItems:");
        System.out.println("-".repeat(60));
        
        for (OrderItem item : order.getItems()) {
            System.out.printf("%-30s %6d @ $%-8.2f $%-8.2f%n",
                item.getProductName(),
                item.getQuantity(),
                item.getPrice(),
                item.getPrice() * item.getQuantity());
        }
        
        System.out.println("-".repeat(60));
        System.out.printf("Subtotal: $%.2f%n", order.getTotalAmount());
        System.out.printf("Discount: $%.2f%n", order.getDiscount());
        System.out.printf("Tax: $%.2f%n", order.getTax());
        System.out.printf("TOTAL: $%.2f%n", order.getFinalAmount());
    }
    
    private static void viewCustomerDetails() {
        System.out.print("\nEnter Customer ID: ");
        String customerId = scanner.nextLine();
        
        Customer customer = customerService.getCustomer(customerId);
        if (customer == null) {
            System.out.println("Customer not found.");
            return;
        }
        
        System.out.println("\n=== CUSTOMER DETAILS ===");
        System.out.println("Customer ID: " + customer.getCustomerId());
        System.out.println("Name: " + customer.getFirstName() + " " + customer.getLastName());
        System.out.println("Email: " + customer.getEmail());
        System.out.println("Phone: " + customer.getPhone());
        System.out.println("Address: " + 
            (customer.getAddress() != null ? customer.getAddress() : "N/A"));
        System.out.println("Join Date: " + customer.getJoinDate());
        System.out.println("Customer Type: " + customer.getCustomerType());
        System.out.println("Loyalty Points: " + customer.getLoyaltyPoints());
        System.out.println("Total Purchases: $" + customer.getTotalPurchases());
        
        if (customer.getLastPurchase() != null) {
            System.out.println("Last Purchase: " + customer.getLastPurchase());
        }
        
        System.out.println("\nOrder History:");
        List<Order> orders = orderService.getOrdersByCustomer(customerId);
        
        if (orders.isEmpty()) {
            System.out.println("No orders found.");
        } else {
            System.out.printf("%-15s %-12s %-10s %-15s%n",
                "Order ID", "Date", "Items", "Total");
            System.out.println("-".repeat(55));
            
            for (Order order : orders) {
                String date = order.getOrderDate().format(
                    DateTimeFormatter.ofPattern("MM/dd/yyyy"));
                
                System.out.printf("%-15s %-12s %-10d $%-14.2f%n",
                    order.getOrderId(),
                    date,
                    order.getItems().size(),
                    order.getFinalAmount());
            }
        }
    }
    
    private static void viewTopCustomers() {
        System.out.println("\n=== TOP CUSTOMERS ===");
        
        List<Customer> topCustomers = customerService.getTopCustomers(10);
        
        if (topCustomers.isEmpty()) {
            System.out.println("No customers found.");
            return;
        }
        
        System.out.printf("%-5s %-20s %-20s %-15s %-15s%n",
            "Rank", "Name", "Email", "Total Spent", "Customer Type");
        System.out.println("-".repeat(75));
        
        for (int i = 0; i < topCustomers.size(); i++) {
            Customer customer = topCustomers.get(i);
            System.out.printf("%-5d %-20s %-20s $%-14.2f %-15s%n",
                i + 1,
                customer.getFirstName() + " " + customer.getLastName(),
                customer.getEmail().length() > 20 ? 
                    customer.getEmail().substring(0, 17) + "..." : customer.getEmail(),
                customer.getTotalPurchases(),
                customer.getCustomerType());
        }
    }
}