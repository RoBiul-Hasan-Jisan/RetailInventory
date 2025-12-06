package com.retailinventory.util;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class CSVHandler {
    
    public static List<String[]> readCSV(String filePath) throws IOException {
        List<String[]> data = new ArrayList<>();
        Path path = Paths.get(filePath);
        
        if (!Files.exists(path)) {
            return data;
        }
        
        try (BufferedReader br = Files.newBufferedReader(path)) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] row = parseCSVLine(line);
                data.add(row);
            }
        }
        
        return data;
    }
    
    public static void writeCSV(String filePath, List<String[]> data, boolean append) 
            throws IOException {
        
        Path path = Paths.get(filePath);
        StandardOpenOption option = append ? 
            StandardOpenOption.APPEND : 
            StandardOpenOption.CREATE;
        
        try (BufferedWriter bw = Files.newBufferedWriter(path, option)) {
            for (String[] row : data) {
                String line = formatCSVLine(row);
                bw.write(line);
                bw.newLine();
            }
        }
    }
    
    private static String[] parseCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    field.append('"');
                    i++; // Skip next quote
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                fields.add(field.toString());
                field.setLength(0);
            } else {
                field.append(c);
            }
        }
        
        fields.add(field.toString());
        return fields.toArray(new String[0]);
    }
    
    private static String formatCSVLine(String[] fields) {
        StringBuilder line = new StringBuilder();
        
        for (int i = 0; i < fields.length; i++) {
            String field = fields[i];
            
            if (field == null) {
                field = "";
            }
            
            // Check if field needs quoting
            boolean needsQuotes = field.contains(",") || 
                                 field.contains("\"") || 
                                 field.contains("\n") || 
                                 field.contains("\r");
            
            if (needsQuotes) {
                field = field.replace("\"", "\"\"");
                field = "\"" + field + "\"";
            }
            
            line.append(field);
            if (i < fields.length - 1) {
                line.append(",");
            }
        }
        
        return line.toString();
    }
    
    public static void exportToExcel(List<Map<String, Object>> data, String filePath) 
            throws IOException {
        
        if (data.isEmpty()) {
            return;
        }
        
        List<String[]> rows = new ArrayList<>();
        
        // Header
        Set<String> headers = data.get(0).keySet();
        rows.add(headers.toArray(new String[0]));
        
        // Data
        for (Map<String, Object> row : data) {
            String[] values = new String[headers.size()];
            int i = 0;
            for (String header : headers) {
                Object value = row.get(header);
                values[i++] = value != null ? value.toString() : "";
            }
            rows.add(values);
        }
        
        writeCSV(filePath, rows, false);
    }
}