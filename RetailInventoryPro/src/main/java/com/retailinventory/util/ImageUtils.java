package com.retailinventory.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageUtils {
    
    public static BufferedImage resizeImage(File imageFile, int width, int height) 
            throws IOException {
        BufferedImage originalImage = ImageIO.read(imageFile);
        
        // Calculate scaling while maintaining aspect ratio
        double aspectRatio = (double) originalImage.getWidth() / originalImage.getHeight();
        int newWidth, newHeight;
        
        if (width / aspectRatio <= height) {
            newWidth = width;
            newHeight = (int) (width / aspectRatio);
        } else {
            newHeight = height;
            newWidth = (int) (height * aspectRatio);
        }
        
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, 
            originalImage.getType());
        Graphics2D g = resizedImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g.dispose();
        
        return resizedImage;
    }
    
    public static void saveImage(BufferedImage image, String filePath, String format) 
            throws IOException {
        File output = new File(filePath);
        ImageIO.write(image, format, output);
    }
    
    public static BufferedImage createPlaceholderImage(int width, int height, String text) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        
        // Background
        g.setColor(new Color(240, 240, 240));
        g.fillRect(0, 0, width, height);
        
        // Border
        g.setColor(Color.LIGHT_GRAY);
        g.drawRect(0, 0, width - 1, height - 1);
        
        // Text
        g.setColor(Color.DARK_GRAY);
        g.setFont(new Font("Arial", Font.PLAIN, 14));
        
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getHeight();
        
        int x = (width - textWidth) / 2;
        int y = (height - textHeight) / 2 + fm.getAscent();
        
        g.drawString(text, x, y);
        
        g.dispose();
        return image;
    }
    
    public static boolean isValidImageFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".jpeg") || 
               name.endsWith(".png") || name.endsWith(".gif") ||
               name.endsWith(".bmp");
    }
}