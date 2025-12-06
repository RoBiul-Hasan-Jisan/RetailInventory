package com.retailinventory.util;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class DataBackup {
    
    public static void createBackup(String backupDir) throws IOException {
        String timestamp = LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String backupName = "backup_" + timestamp;
        String backupPath = backupDir + backupName;
        
        Files.createDirectories(Paths.get(backupPath));
        
        // Copy data files
        copyDirectory("data", backupPath);
        
        // Create zip archive
        createZipArchive(backupPath + ".zip", backupPath);
        
        // Delete temporary directory
        deleteDirectory(backupPath);
        
        System.out.println("Backup created: " + backupPath + ".zip");
    }
    
    public static void restoreBackup(String zipFile, String restoreDir) throws IOException {
        // Implementation for restoring from backup
        System.out.println("Restoring backup from: " + zipFile);
        // Actual implementation would extract zip and restore files
    }
    
    private static void copyDirectory(String sourceDir, String destDir) throws IOException {
        Path sourcePath = Paths.get(sourceDir);
        Path destPath = Paths.get(destDir);
        
        if (!Files.exists(sourcePath)) {
            return;
        }
        
        Files.walk(sourcePath)
            .forEach(source -> {
                try {
                    Path destination = destPath.resolve(sourcePath.relativize(source));
                    if (Files.isDirectory(source)) {
                        Files.createDirectories(destination);
                    } else {
                        Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    System.err.println("Error copying file: " + e.getMessage());
                }
            });
    }
    
    private static void createZipArchive(String zipFile, String sourceDir) throws IOException {
        Path sourcePath = Paths.get(sourceDir);
        
        try (ZipOutputStream zos = new ZipOutputStream(
                new FileOutputStream(zipFile))) {
            
            Files.walk(sourcePath)
                .filter(path -> !Files.isDirectory(path))
                .forEach(path -> {
                    try {
                        ZipEntry zipEntry = new ZipEntry(
                            sourcePath.relativize(path).toString());
                        zos.putNextEntry(zipEntry);
                        Files.copy(path, zos);
                        zos.closeEntry();
                    } catch (IOException e) {
                        System.err.println("Error adding to zip: " + e.getMessage());
                    }
                });
        }
    }
    
    private static void deleteDirectory(String dir) throws IOException {
        Path path = Paths.get(dir);
        if (Files.exists(path)) {
            Files.walk(path)
                .sorted((a, b) -> -a.compareTo(b))
                .forEach(p -> {
                    try {
                        Files.delete(p);
                    } catch (IOException e) {
                        System.err.println("Error deleting: " + p);
                    }
                });
        }
    }
    
    public static void scheduleDailyBackup() {
        // This would typically be implemented with a scheduler
        // For now, it's a manual operation
        System.out.println("Backup scheduling not implemented. Use createBackup() manually.");
    }
}