package com.retailinventory.service;

import com.retailinventory.model.Product;
import com.retailinventory.model.Order;
import com.retailinventory.model.OrderItem;
import com.retailinventory.model.Customer;
import com.retailinventory.model.Supplier;
import com.retailinventory.model.User;
import com.retailinventory.exception.FileProcessingException;
import com.retailinventory.util.CSVHandler;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class FileDataService {
    private static final String BASE_DIR = "data/";
    private static final String INVENTORY_DIR = BASE_DIR + "inventory/";
    private static final String PRODUCTS_FILE = INVENTORY_DIR + "products.csv";
    private static final String MOVEMENT_LOG = INVENTORY_DIR + "stock_movement.csv";
    private static final String LOW_STOCK_FILE = INVENTORY_DIR + "low_stock_alerts.csv";
    
    private static final String ORDERS_DIR = BASE_DIR + "orders/";
    private static final String ORDERS_FILE = ORDERS_DIR + "orders.csv";
    
    private static final String CUSTOMERS_DIR = BASE_DIR + "customers/";
    private static final String CUSTOMERS_FILE = CUSTOMERS_DIR + "customers.csv";
    
    private static final String SUPPLIERS_DIR = BASE_DIR + "suppliers/";
    private static final String SUPPLIERS_FILE = SUPPLIERS_DIR + "suppliers.csv";
    
    private static final String USERS_DIR = BASE_DIR + "users/";
    private static final String USERS_FILE = USERS_DIR + "users.csv";
    
    private static final String BACKUP_DIR = BASE_DIR + "backups/";
    private static final String REPORTS_DIR = BASE_DIR + "reports/";
    private static final String LOGS_DIR = BASE_DIR + "logs/";
    
    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    static {
        try {
            createDirectories();
            createSampleDataIfNeeded();
        } catch (IOException e) {
            System.err.println("Failed to initialize file system: " + e.getMessage());
        }
    }
    
    private static void createDirectories() throws IOException {
        String[] dirs = {
            BASE_DIR,
            INVENTORY_DIR,
            ORDERS_DIR,
            CUSTOMERS_DIR,
            SUPPLIERS_DIR,
            USERS_DIR,
            BACKUP_DIR,
            REPORTS_DIR,
            REPORTS_DIR + "daily/",
            REPORTS_DIR + "monthly/",
            REPORTS_DIR + "yearly/",
            LOGS_DIR,
            ORDERS_DIR + "receipts/",
            INVENTORY_DIR + "archived/"
        };
        
        for (String dir : dirs) {
            Path path = Paths.get(dir);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
        }
    }
    
    // Product operations
    public List<Product> loadProducts() throws FileProcessingException {
        List<Product> products = new ArrayList<>();
        
        try {
            if (!Files.exists(Paths.get(PRODUCTS_FILE))) {
                return products;
            }
            
            List<String[]> rows = CSVHandler.readCSV(PRODUCTS_FILE);
            
            for (int i = 1; i < rows.size(); i++) { // Skip header
                String[] row = rows.get(i);
                if (row.length < 11) continue;
                
                try {
                    Product product = new Product();
                    product.setProductId(row[0]);
                    product.setBarcode(row[1]);
                    product.setName(row[2]);
                    product.setCategory(row[3]);
                    product.setDescription(row.length > 4 ? row[4] : "");
                    product.setPurchasePrice(Double.parseDouble(row[5]));
                    product.setSellingPrice(Double.parseDouble(row[6]));
                    product.setQuantityInStock(Integer.parseInt(row[7]));
                    product.setMinStockLevel(Integer.parseInt(row[8]));
                    product.setMaxStockLevel(Integer.parseInt(row[9]));
                    product.setSupplierId(row[10]);
                    
                    if (row.length > 11 && !row[11].isEmpty()) {
                        product.setLocation(row[11]);
                    }
                    if (row.length > 12 && !row[12].isEmpty()) {
                        product.setExpiryDate(LocalDate.parse(row[12], DATE_FORMATTER));
                    }
                    if (row.length > 13 && !row[13].isEmpty()) {
                        product.setPerishable(Boolean.parseBoolean(row[13]));
                    }
                    if (row.length > 14 && !row[14].isEmpty()) {
                        product.setUnit(row[14]);
                    }
                    if (row.length > 15 && !row[15].isEmpty()) {
                        product.setQuantitySold(Integer.parseInt(row[15]));
                    }
                    if (row.length > 16 && !row[16].isEmpty()) {
                        product.setLastRestocked(LocalDate.parse(row[16], DATE_FORMATTER));
                    }
                    
                    products.add(product);
                } catch (Exception e) {
                    System.err.println("Error parsing product row: " + String.join(",", row));
                }
            }
            
        } catch (IOException e) {
            throw new FileProcessingException("Failed to load products", e);
        }
        
        return products;
    }
    
    public void saveProduct(Product product) throws FileProcessingException {
        try {
            List<Product> products = loadProducts();
            boolean found = false;
            
            for (int i = 0; i < products.size(); i++) {
                if (products.get(i).getProductId().equals(product.getProductId())) {
                    products.set(i, product);
                    found = true;
                    break;
                }
            }
            
            if (!found) {
                products.add(product);
            }
            
            saveAllProducts(products);
            
        } catch (Exception e) {
            throw new FileProcessingException("Failed to save product", e);
        }
    }
    
    public void saveAllProducts(List<Product> products) throws FileProcessingException {
        try {
            List<String[]> rows = new ArrayList<>();
            
            // Header
            rows.add(new String[]{
                "ProductID", "Barcode", "Name", "Category", "Description",
                "PurchasePrice", "SellingPrice", "QuantityInStock", "MinStockLevel",
                "MaxStockLevel", "SupplierID", "Location", "ExpiryDate", 
                "IsPerishable", "Unit", "QuantitySold", "LastRestocked"
            });
            
            // Data rows
            for (Product product : products) {
                String[] row = new String[17];
                row[0] = product.getProductId();
                row[1] = product.getBarcode();
                row[2] = product.getName();
                row[3] = product.getCategory();
                row[4] = product.getDescription() != null ? product.getDescription() : "";
                row[5] = String.format("%.2f", product.getPurchasePrice());
                row[6] = String.format("%.2f", product.getSellingPrice());
                row[7] = String.valueOf(product.getQuantityInStock());
                row[8] = String.valueOf(product.getMinStockLevel());
                row[9] = String.valueOf(product.getMaxStockLevel());
                row[10] = product.getSupplierId() != null ? product.getSupplierId() : "";
                row[11] = product.getLocation() != null ? product.getLocation() : "";
                row[12] = product.getExpiryDate() != null ? 
                    product.getExpiryDate().format(DATE_FORMATTER) : "";
                row[13] = String.valueOf(product.isPerishable());
                row[14] = product.getUnit() != null ? product.getUnit() : "";
                row[15] = String.valueOf(product.getQuantitySold());
                row[16] = product.getLastRestocked() != null ? 
                    product.getLastRestocked().format(DATE_FORMATTER) : "";
                
                rows.add(row);
            }
            
            CSVHandler.writeCSV(PRODUCTS_FILE, rows, false);
            
        } catch (IOException e) {
            throw new FileProcessingException("Failed to save products", e);
        }
    }
    
    public void deleteProduct(String productId) throws FileProcessingException {
        try {
            List<Product> products = loadProducts();
            products.removeIf(p -> p.getProductId().equals(productId));
            saveAllProducts(products);
        } catch (Exception e) {
            throw new FileProcessingException("Failed to delete product", e);
        }
    }
    
    public void logStockMovement(String productId, String movementType, 
                                int quantityChange, int newQuantity, String reference) 
            throws FileProcessingException {
        try {
            String logEntry = String.format("%s,%s,%s,%d,%d,%s",
                LocalDateTime.now().format(DATETIME_FORMATTER),
                productId,
                movementType,
                quantityChange,
                newQuantity,
                reference);
            
            Path logFile = Paths.get(MOVEMENT_LOG);
            Files.writeString(logFile, logEntry + System.lineSeparator(), 
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            
        } catch (IOException e) {
            throw new FileProcessingException("Failed to log movement", e);
        }
    }
    
    public void saveLowStockAlert(Product product) throws FileProcessingException {
        try {
            String alert = String.format("%s,%s,%s,%d,%d,%s",
                LocalDateTime.now().format(DATETIME_FORMATTER),
                product.getProductId(),
                product.getName(),
                product.getQuantityInStock(),
                product.getMinStockLevel(),
                product.getCategory());
            
            Path alertFile = Paths.get(LOW_STOCK_FILE);
            Files.writeString(alertFile, alert + System.lineSeparator(), 
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            
        } catch (IOException e) {
            throw new FileProcessingException("Failed to save alert", e);
        }
    }
    
    // Order operations
    public List<Order> loadOrders() throws FileProcessingException {
        List<Order> orders = new ArrayList<>();
        
        try {
            if (!Files.exists(Paths.get(ORDERS_FILE))) {
                return orders;
            }
            
            List<String[]> rows = CSVHandler.readCSV(ORDERS_FILE);
            
            for (int i = 1; i < rows.size(); i++) {
                String[] row = rows.get(i);
                if (row.length < 9) continue;
                
                try {
                    Order order = new Order();
                    order.setOrderId(row[0]);
                    order.setCustomerId(row[1]);
                    order.setOrderDate(LocalDateTime.parse(row[2], DATETIME_FORMATTER));
                    order.setTotalAmount(Double.parseDouble(row[3]));
                    order.setDiscount(Double.parseDouble(row[4]));
                    order.setTax(Double.parseDouble(row[5]));
                    order.setFinalAmount(Double.parseDouble(row[6]));
                    order.setStatus(row[7]);
                    order.setPaymentMethod(row[8]);
                    
                    if (row.length > 9 && !row[9].isEmpty()) {
                        order.setNotes(row[9]);
                    }
                    if (row.length > 10 && !row[10].isEmpty()) {
                        order.setCompletionDate(LocalDateTime.parse(row[10], DATETIME_FORMATTER));
                    }
                    
                    orders.add(order);
                } catch (Exception e) {
                    System.err.println("Error parsing order row: " + String.join(",", row));
                }
            }
            
        } catch (IOException e) {
            throw new FileProcessingException("Failed to load orders", e);
        }
        
        return orders;
    }
    
    public void saveOrder(Order order) throws FileProcessingException {
        try {
            List<Order> orders = loadOrders();
            orders.add(order);
            saveAllOrders(orders);
            
            // Save order items
            saveOrderItems(order);
            
        } catch (Exception e) {
            throw new FileProcessingException("Failed to save order", e);
        }
    }
    
    public void updateOrder(Order order) throws FileProcessingException {
        try {
            List<Order> orders = loadOrders();
            for (int i = 0; i < orders.size(); i++) {
                if (orders.get(i).getOrderId().equals(order.getOrderId())) {
                    orders.set(i, order);
                    break;
                }
            }
            saveAllOrders(orders);
            
        } catch (Exception e) {
            throw new FileProcessingException("Failed to update order", e);
        }
    }
    
    private void saveAllOrders(List<Order> orders) throws FileProcessingException {
        try {
            List<String[]> rows = new ArrayList<>();
            
            // Header
            rows.add(new String[]{
                "OrderID", "CustomerID", "OrderDate", "TotalAmount", "Discount",
                "Tax", "FinalAmount", "Status", "PaymentMethod", "Notes", "CompletionDate"
            });
            
            // Data rows
            for (Order order : orders) {
                String[] row = new String[11];
                row[0] = order.getOrderId();
                row[1] = order.getCustomerId();
                row[2] = order.getOrderDate().format(DATETIME_FORMATTER);
                row[3] = String.format("%.2f", order.getTotalAmount());
                row[4] = String.format("%.2f", order.getDiscount());
                row[5] = String.format("%.2f", order.getTax());
                row[6] = String.format("%.2f", order.getFinalAmount());
                row[7] = order.getStatus();
                row[8] = order.getPaymentMethod();
                row[9] = order.getNotes() != null ? order.getNotes() : "";
                row[10] = order.getCompletionDate() != null ? 
                    order.getCompletionDate().format(DATETIME_FORMATTER) : "";
                
                rows.add(row);
            }
            
            CSVHandler.writeCSV(ORDERS_FILE, rows, false);
            
        } catch (IOException e) {
            throw new FileProcessingException("Failed to save orders", e);
        }
    }
    
    private void saveOrderItems(Order order) throws FileProcessingException {
        try {
            String itemsFile = ORDERS_DIR + "items_" + order.getOrderId() + ".csv";
            List<String[]> rows = new ArrayList<>();
            
            // Header
            rows.add(new String[]{
                "OrderID", "ProductID", "ProductName", "Price", "Quantity", "Discount"
            });
            
            // Data rows
            for (OrderItem item : order.getItems()) {
                String[] row = new String[6];
                row[0] = order.getOrderId();
                row[1] = item.getProductId();
                row[2] = item.getProductName();
                row[3] = String.format("%.2f", item.getPrice());
                row[4] = String.valueOf(item.getQuantity());
                row[5] = String.format("%.2f", item.getDiscount());
                
                rows.add(row);
            }
            
            CSVHandler.writeCSV(itemsFile, rows, false);
            
        } catch (IOException e) {
            throw new FileProcessingException("Failed to save order items", e);
        }
    }
    
    // Customer operations
    public List<Customer> loadCustomers() throws FileProcessingException {
        List<Customer> customers = new ArrayList<>();
        
        try {
            if (!Files.exists(Paths.get(CUSTOMERS_FILE))) {
                return customers;
            }
            
            List<String[]> rows = CSVHandler.readCSV(CUSTOMERS_FILE);
            
            for (int i = 1; i < rows.size(); i++) {
                String[] row = rows.get(i);
                if (row.length < 8) continue;
                
                try {
                    Customer customer = new Customer();
                    customer.setCustomerId(row[0]);
                    customer.setFirstName(row[1]);
                    customer.setLastName(row[2]);
                    customer.setEmail(row[3]);
                    customer.setPhone(row[4]);
                    customer.setAddress(row[5]);
                    customer.setJoinDate(LocalDate.parse(row[6], DATE_FORMATTER));
                    customer.setTotalPurchases(Double.parseDouble(row[7]));
                    
                    if (row.length > 8 && !row[8].isEmpty()) {
                        customer.setLoyaltyPoints(Integer.parseInt(row[8]));
                    }
                    if (row.length > 9 && !row[9].isEmpty()) {
                        customer.setCustomerType(row[9]);
                    }
                    if (row.length > 10 && !row[10].isEmpty()) {
                        customer.setLastPurchase(LocalDateTime.parse(row[10], DATETIME_FORMATTER));
                    }
                    
                    customers.add(customer);
                } catch (Exception e) {
                    System.err.println("Error parsing customer row: " + String.join(",", row));
                }
            }
            
        } catch (IOException e) {
            throw new FileProcessingException("Failed to load customers", e);
        }
        
        return customers;
    }
    
    public void saveCustomer(Customer customer) throws FileProcessingException {
        try {
            List<Customer> customers = loadCustomers();
            boolean found = false;
            
            for (int i = 0; i < customers.size(); i++) {
                if (customers.get(i).getCustomerId().equals(customer.getCustomerId())) {
                    customers.set(i, customer);
                    found = true;
                    break;
                }
            }
            
            if (!found) {
                customers.add(customer);
            }
            
            saveAllCustomers(customers);
            
        } catch (Exception e) {
            throw new FileProcessingException("Failed to save customer", e);
        }
    }
    
    private void saveAllCustomers(List<Customer> customers) throws FileProcessingException {
        try {
            List<String[]> rows = new ArrayList<>();
            
            // Header
            rows.add(new String[]{
                "CustomerID", "FirstName", "LastName", "Email", "Phone", "Address",
                "JoinDate", "TotalPurchases", "LoyaltyPoints", "CustomerType", "LastPurchase"
            });
            
            // Data rows
            for (Customer customer : customers) {
                String[] row = new String[11];
                row[0] = customer.getCustomerId();
                row[1] = customer.getFirstName();
                row[2] = customer.getLastName();
                row[3] = customer.getEmail();
                row[4] = customer.getPhone();
                row[5] = customer.getAddress() != null ? customer.getAddress() : "";
                row[6] = customer.getJoinDate().format(DATE_FORMATTER);
                row[7] = String.format("%.2f", customer.getTotalPurchases());
                row[8] = String.valueOf(customer.getLoyaltyPoints());
                row[9] = customer.getCustomerType();
                row[10] = customer.getLastPurchase() != null ? 
                    customer.getLastPurchase().format(DATETIME_FORMATTER) : "";
                
                rows.add(row);
            }
            
            CSVHandler.writeCSV(CUSTOMERS_FILE, rows, false);
            
        } catch (IOException e) {
            throw new FileProcessingException("Failed to save customers", e);
        }
    }
    
    // Backup operations
    public void createBackup() throws FileProcessingException {
        try {
            String timestamp = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String backupDir = BACKUP_DIR + "backup_" + timestamp + "/";
            Files.createDirectories(Paths.get(backupDir));
            
            // Copy all important files
            copyFileToBackup(PRODUCTS_FILE, backupDir);
            copyFileToBackup(ORDERS_FILE, backupDir);
            copyFileToBackup(CUSTOMERS_FILE, backupDir);
            copyFileToBackup(SUPPLIERS_FILE, backupDir);
            copyFileToBackup(USERS_FILE, backupDir);
            
            // Create backup info file
            String info = "Backup created: " + LocalDateTime.now() + "\n" +
                         "Files backed up: 5\n" +
                         "System: RetailInventoryPro v2.0.0\n";
            
            Files.writeString(Paths.get(backupDir + "backup_info.txt"), info);
            
            System.out.println("Backup created: " + backupDir);
            
        } catch (IOException e) {
            throw new FileProcessingException("Failed to create backup", e);
        }
    }
    
    private void copyFileToBackup(String sourceFile, String backupDir) throws IOException {
        Path source = Paths.get(sourceFile);
        if (Files.exists(source)) {
            String fileName = source.getFileName().toString();
            Path target = Paths.get(backupDir + fileName);
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }
    
    // Report generation
    public void generateDailyReport() throws FileProcessingException {
        try {
            LocalDate today = LocalDate.now();
            String reportFile = REPORTS_DIR + "daily/report_" + 
                today.format(DATE_FORMATTER) + ".txt";
            
            StringBuilder report = new StringBuilder();
            report.append("=== DAILY INVENTORY REPORT ===\n");
            report.append("Date: ").append(today).append("\n");
            report.append("Generated: ").append(LocalDateTime.now()).append("\n\n");
            
            // Load data for report
            List<Product> products = loadProducts();
            List<Order> orders = loadOrders();
            
            // Summary
            report.append("SUMMARY:\n");
            report.append(String.format("Total Products: %d\n", products.size()));
            
            int lowStockCount = (int) products.stream()
                .filter(Product::needsReorder)
                .count();
            report.append(String.format("Low Stock Items: %d\n", lowStockCount));
            
            double totalValue = products.stream()
                .mapToDouble(Product::getStockValue)
                .sum();
            report.append(String.format("Total Inventory Value: $%.2f\n", totalValue));
            
            // Daily sales
            LocalDateTime startOfDay = today.atStartOfDay();
            LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();
            
            double dailySales = orders.stream()
                .filter(o -> o.getOrderDate().isAfter(startOfDay) && 
                           o.getOrderDate().isBefore(endOfDay) &&
                           o.getStatus().equals("COMPLETED"))
                .mapToDouble(Order::getFinalAmount)
                .sum();
            report.append(String.format("Daily Sales: $%.2f\n\n", dailySales));
            
            Files.writeString(Paths.get(reportFile), report.toString());
            
        } catch (IOException e) {
            throw new FileProcessingException("Failed to generate daily report", e);
        }
    }
    
    private static void createSampleDataIfNeeded() {
        try {
            if (!Files.exists(Paths.get(PRODUCTS_FILE))) {
                System.out.println("Creating sample data...");
                
                // Create sample products
                List<String[]> sampleProducts = new ArrayList<>();
                sampleProducts.add(new String[]{
                    "ProductID", "Barcode", "Name", "Category", "Description",
                    "PurchasePrice", "SellingPrice", "QuantityInStock", "MinStockLevel",
                    "MaxStockLevel", "SupplierID", "Location", "ExpiryDate", 
                    "IsPerishable", "Unit", "QuantitySold", "LastRestocked"
                });
                
                LocalDate today = LocalDate.now();
                
                sampleProducts.add(new String[]{
                    "PROD001", "5901234123457", "Coca-Cola 330ml", "Beverage", "Carbonated drink",
                    "0.45", "0.99", "120", "50", "200", "SUPP001", "Aisle 3, Shelf B", 
                    today.plusMonths(12).format(DATE_FORMATTER), "false", "can", "150", 
                    today.minusDays(5).format(DATE_FORMATTER)
                });
                
                sampleProducts.add(new String[]{
                    "PROD002", "5901234123458", "Lays Classic Chips 150g", "Snacks", "Potato chips",
                    "0.85", "1.99", "80", "30", "150", "SUPP002", "Aisle 5, Shelf A",
                    today.plusMonths(6).format(DATE_FORMATTER), "true", "bag", "200",
                    today.minusDays(10).format(DATE_FORMATTER)
                });
                
                sampleProducts.add(new String[]{
                    "PROD003", "5901234123459", "Dove Soap 100g", "Personal Care", "Beauty bar soap",
                    "1.20", "2.49", "45", "20", "100", "SUPP003", "Aisle 7, Shelf C",
                    today.plusMonths(24).format(DATE_FORMATTER), "false", "bar", "85",
                    today.minusDays(15).format(DATE_FORMATTER)
                });
                
                CSVHandler.writeCSV(PRODUCTS_FILE, sampleProducts, false);
                
                // Create sample customers
                List<String[]> sampleCustomers = new ArrayList<>();
                sampleCustomers.add(new String[]{
                    "CustomerID", "FirstName", "LastName", "Email", "Phone", "Address",
                    "JoinDate", "TotalPurchases", "LoyaltyPoints", "CustomerType", "LastPurchase"
                });
                
                sampleCustomers.add(new String[]{
                    "CUST001", "John", "Doe", "john.doe@email.com", "555-0101", 
                    "123 Main St", today.minusMonths(6).format(DATE_FORMATTER),
                    "1250.50", "125", "Premium", today.minusDays(2).format(DATETIME_FORMATTER)
                });
                
                sampleCustomers.add(new String[]{
                    "CUST002", "Jane", "Smith", "jane.smith@email.com", "555-0102",
                    "456 Oak Ave", today.minusMonths(3).format(DATE_FORMATTER),
                    "450.75", "45", "Regular", today.minusDays(5).format(DATETIME_FORMATTER)
                });
                
                CSVHandler.writeCSV(CUSTOMERS_FILE, sampleCustomers, false);
                
                // Create sample users
                List<String[]> sampleUsers = new ArrayList<>();
                sampleUsers.add(new String[]{
                    "UserID", "Username", "Password", "FullName", "Email", "Phone",
                    "Role", "HireDate", "LastLogin", "IsActive", "Department", "Salary"
                });
                
                sampleUsers.add(new String[]{
                    "USER001", "admin", "admin123", "System Administrator", 
                    "admin@retailstore.com", "555-0001", "ADMIN",
                    today.minusYears(1).format(DATE_FORMATTER),
                    today.atStartOfDay().format(DATETIME_FORMATTER),
                    "true", "Management", "75000.00"
                });
                
                sampleUsers.add(new String[]{
                    "USER002", "manager", "manager123", "Store Manager",
                    "manager@retailstore.com", "555-0002", "MANAGER",
                    today.minusMonths(6).format(DATE_FORMATTER),
                    today.atStartOfDay().minusHours(2).format(DATETIME_FORMATTER),
                    "true", "Management", "50000.00"
                });
                
                sampleUsers.add(new String[]{
                    "USER003", "cashier", "cashier123", "Sales Cashier",
                    "cashier@retailstore.com", "555-0003", "CASHIER",
                    today.minusMonths(2).format(DATE_FORMATTER),
                    today.atStartOfDay().minusHours(4).format(DATETIME_FORMATTER),
                    "true", "Sales", "30000.00"
                });
                
                CSVHandler.writeCSV(USERS_FILE, sampleUsers, false);
                
                System.out.println("Sample data created successfully.");
            }
            
        } catch (IOException e) {
            System.err.println("Failed to create sample data: " + e.getMessage());
        }
    }
}