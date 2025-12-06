package com.retailinventory.model;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

public class Supplier {
    private String supplierId;
    private String companyName;
    private String contactPerson;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String state;
    private String zipCode;
    private String country;
    private List<String> productCategories;
    private double rating;
    private LocalDate partnershipDate;
    private String paymentTerms;
    private double totalPurchases;
    
    public Supplier() {
        this.supplierId = "SUPP" + System.currentTimeMillis() % 10000;
        this.productCategories = new ArrayList<>();
        this.partnershipDate = LocalDate.now();
        this.rating = 5.0;
    }
    
    public Supplier(String companyName, String contactPerson, String email, String phone) {
        this();
        this.companyName = companyName;
        this.contactPerson = contactPerson;
        this.email = email;
        this.phone = phone;
    }
    
    public void addCategory(String category) {
        if (!productCategories.contains(category)) {
            productCategories.add(category);
        }
    }
    
    public void updateRating(double newRating) {
        // Simple average rating update
        this.rating = (this.rating + newRating) / 2;
    }
    
    public void addPurchase(double amount) {
        this.totalPurchases += amount;
    }
    
    // Getters and Setters
    public String getSupplierId() { return supplierId; }
    public void setSupplierId(String supplierId) { this.supplierId = supplierId; }
    
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    
    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    
    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }
    
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    
    public List<String> getProductCategories() { return productCategories; }
    public void setProductCategories(List<String> productCategories) { 
        this.productCategories = productCategories; 
    }
    
    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
    
    public LocalDate getPartnershipDate() { return partnershipDate; }
    public void setPartnershipDate(LocalDate partnershipDate) { this.partnershipDate = partnershipDate; }
    
    public String getPaymentTerms() { return paymentTerms; }
    public void setPaymentTerms(String paymentTerms) { this.paymentTerms = paymentTerms; }
    
    public double getTotalPurchases() { return totalPurchases; }
    public void setTotalPurchases(double totalPurchases) { this.totalPurchases = totalPurchases; }
    
    @Override
    public String toString() {
        return String.format("%s (%s)", companyName, supplierId);
    }
}