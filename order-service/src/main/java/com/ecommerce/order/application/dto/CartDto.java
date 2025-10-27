package com.ecommerce.order.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 購物車 DTO
 */
public class CartDto {
    
    private String cartId;
    private String customerId;
    private List<CartItemDto> cartItems;
    private BigDecimal totalAmount;
    private Integer totalItemCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime expiresAt;
    
    // Constructors
    public CartDto() {}
    
    // Getters and Setters
    public String getCartId() { return cartId; }
    public void setCartId(String cartId) { this.cartId = cartId; }
    
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    
    public List<CartItemDto> getCartItems() { return cartItems; }
    public void setCartItems(List<CartItemDto> cartItems) { this.cartItems = cartItems; }
    
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    
    public Integer getTotalItemCount() { return totalItemCount; }
    public void setTotalItemCount(Integer totalItemCount) { this.totalItemCount = totalItemCount; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    
    /**
     * 購物車項目 DTO
     */
    public static class CartItemDto {
        private String productId;
        private String productName;
        private BigDecimal unitPrice;
        private Integer quantity;
        private BigDecimal totalPrice;
        private String productSpecifications;
        
        // Constructors
        public CartItemDto() {}
        
        // Getters and Setters
        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }
        
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        
        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
        
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        
        public BigDecimal getTotalPrice() { return totalPrice; }
        public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
        
        public String getProductSpecifications() { return productSpecifications; }
        public void setProductSpecifications(String productSpecifications) { this.productSpecifications = productSpecifications; }
    }
}