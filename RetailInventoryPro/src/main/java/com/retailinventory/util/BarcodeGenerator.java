package com.retailinventory.util;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Font;

public class BarcodeGenerator {
    
    public static void generateBarcodeImage(String barcode, String productName, String filePath) {
        try {
            int width = 300;
            int height = 150;
            
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();
            
            // White background
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, width, height);
            
            // Draw barcode bars (simplified representation)
            g2d.setColor(Color.BLACK);
            int barWidth = 2;
            int x = 20;
            int y = 20;
            
            for (char c : barcode.toCharArray()) {
                int digit = Character.getNumericValue(c);
                int barHeight = 80 + (digit * 2);
                g2d.fillRect(x, y, barWidth, barHeight);
                x += barWidth + 1;
            }
            
            // Draw barcode text
            g2d.setFont(new Font("Monospaced", Font.PLAIN, 12));
            g2d.drawString(barcode, 20, 120);
            
            // Draw product name
            g2d.setFont(new Font("Arial", Font.BOLD, 14));
            g2d.drawString(productName, 20, 140);
            
            g2d.dispose();
            
            // Save image
            ImageIO.write(image, "PNG", new File(filePath));
            
        } catch (Exception e) {
            System.err.println("Failed to generate barcode image: " + e.getMessage());
        }
    }
    
    public static String generateEAN13(String productId) {
        // Generate a valid EAN-13 barcode
        String base = "590" + String.format("%09d", 
            Math.abs(productId.hashCode() % 1000000000));
        
        // Calculate check digit
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            int digit = Character.getNumericValue(base.charAt(i));
            sum += (i % 2 == 0) ? digit : digit * 3;
        }
        int checkDigit = (10 - (sum % 10)) % 10;
        
        return base + checkDigit;
    }
    
    public static boolean validateBarcode(String barcode) {
        if (barcode == null || barcode.length() != 13) {
            return false;
        }
        
        try {
            int sum = 0;
            for (int i = 0; i < 12; i++) {
                int digit = Character.getNumericValue(barcode.charAt(i));
                sum += (i % 2 == 0) ? digit : digit * 3;
            }
            int checkDigit = Character.getNumericValue(barcode.charAt(12));
            int calculatedCheckDigit = (10 - (sum % 10)) % 10;
            
            return checkDigit == calculatedCheckDigit;
        } catch (Exception e) {
            return false;
        }
    }
}