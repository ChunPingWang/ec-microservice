package com.ecommerce.order.application.dto;

import java.math.BigDecimal;

/**
 * 購物車項目資料傳輸物件
 */
public class CartItemDto {
    
    private String cartItemId;
    private String cartId;
    private String productId;
    private String productName;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal totalPrice;
    private String productSpecifications;
    
    // Constructors
    public CartItemDto() {}
    
    public CartItemDto(String cartItemId, String cartId, String productId, String productName,
                      BigDecimal unitPrice, Integer quantity, BigDecimal totalPrice, String productSpecifications) {
        this.cartItemId = cartItemId;
        this.cartId = cartId;
        this.productId = productId;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.productSpecifications = productSpecifications;
    }
    
    // Getters and Setters
    public String getCartItemId() { return cartItemId; }
    public void setCartItemId(String cartItemId) { this.cartItemId = cartItemId; }
    
    public String getCartId() { return cartId; }
    public void setCartId(String cartId) { this.cartId = cartId; }
    
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