package com.ecommerce.order.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * 加入購物車請求
 */
public class AddToCartRequest {
    
    @NotBlank(message = "Product ID is required")
    private String productId;
    
    @NotBlank(message = "Product name is required")
    @Size(max = 200, message = "Product name cannot exceed 200 characters")
    private String productName;
    
    @NotNull(message = "Unit price is required")
    @Min(value = 0, message = "Unit price must be positive")
    private BigDecimal unitPrice;
    
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
    
    @Size(max = 1000, message = "Product specifications cannot exceed 1000 characters")
    private String productSpecifications;
    
    // Constructors
    public AddToCartRequest() {}
    
    public AddToCartRequest(String productId, String productName, BigDecimal unitPrice, 
                          Integer quantity, String productSpecifications) {
        this.productId = productId;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.productSpecifications = productSpecifications;
    }
    
    // Getters and Setters
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    
    public String getProductSpecifications() { return productSpecifications; }
    public void setProductSpecifications(String productSpecifications) { this.productSpecifications = productSpecifications; }
}