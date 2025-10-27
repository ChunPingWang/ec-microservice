package com.ecommerce.order.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 建立訂單請求 DTO
 */
public class CreateOrderRequest {
    
    @NotBlank(message = "Customer name is required")
    @Size(max = 100, message = "Customer name cannot exceed 100 characters")
    private String customerName;
    
    @NotBlank(message = "Customer email is required")
    @Email(message = "Invalid email format")
    private String customerEmail;
    
    @NotBlank(message = "Shipping address is required")
    @Size(max = 500, message = "Shipping address cannot exceed 500 characters")
    private String shippingAddress;
    
    @NotBlank(message = "Billing address is required")
    @Size(max = 500, message = "Billing address cannot exceed 500 characters")
    private String billingAddress;
    
    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;
    
    // Constructors
    public CreateOrderRequest() {}
    
    public CreateOrderRequest(String customerName, String customerEmail, 
                            String shippingAddress, String billingAddress) {
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.shippingAddress = shippingAddress;
        this.billingAddress = billingAddress;
    }
    
    // Getters and Setters
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    
    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
    
    public String getBillingAddress() { return billingAddress; }
    public void setBillingAddress(String billingAddress) { this.billingAddress = billingAddress; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}