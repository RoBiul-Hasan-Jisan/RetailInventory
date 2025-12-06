package com.retailinventory.util;

import com.retailinventory.model.Product;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class EmailNotifier {
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String USERNAME = "your-email@gmail.com";
    private static final String PASSWORD = "your-password";
    
    public static void sendLowStockAlert(Product product, String recipient) {
        String subject = "Low Stock Alert: " + product.getName();
        String body = String.format("""
            Product: %s
            Product ID: %s
            Current Stock: %d
            Minimum Stock Level: %d
            Category: %s
            
            This product needs to be reordered immediately.
            """,
            product.getName(),
            product.getProductId(),
            product.getQuantityInStock(),
            product.getMinStockLevel(),
            product.getCategory()
        );
        
        sendEmail(recipient, subject, body);
    }
    
    public static void sendDailyReport(String recipient, String reportContent) {
        String subject = "Daily Sales Report - " + java.time.LocalDate.now();
        sendEmail(recipient, subject, reportContent);
    }
    
    public static void sendOrderConfirmation(String recipient, String orderId, double amount) {
        String subject = "Order Confirmation: " + orderId;
        String body = String.format("""
            Thank you for your order!
            
            Order ID: %s
            Amount: $%.2f
            Date: %s
            
            Your order has been received and is being processed.
            """,
            orderId,
            amount,
            java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );
        
        sendEmail(recipient, subject, body);
    }
    
    private static void sendEmail(String recipient, String subject, String body) {
        // For security, credentials should be loaded from configuration
        // This is a simplified version
        
        System.out.println("=== EMAIL NOTIFICATION ===");
        System.out.println("To: " + recipient);
        System.out.println("Subject: " + subject);
        System.out.println("Body:\n" + body);
        System.out.println("==========================\n");
        
        // In production, uncomment and configure the actual email sending:
        /*
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        
        Session session = Session.getInstance(props,
            new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(USERNAME, PASSWORD);
                }
            });
        
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(USERNAME));
            message.setRecipients(Message.RecipientType.TO, 
                InternetAddress.parse(recipient));
            message.setSubject(subject);
            message.setText(body);
            
            Transport.send(message);
            System.out.println("Email sent successfully to " + recipient);
            
        } catch (MessagingException e) {
            System.err.println("Failed to send email: " + e.getMessage());
        }
        */
    }
}