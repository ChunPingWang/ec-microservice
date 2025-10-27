package com.ecommerce.order.application.dto;

import java.math.BigDecimal;

/**
 * 訂單項目資料傳輸物件
 */
public class OrderItemDto {
    
    private String orderItemId;
    private String orderId;
    private String productId;
    private String productName;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal totalPrice;
    private String productSpecifications;
    
    // Constructors
    public OrderItemDto() {}
    
    public OrderItemDto(String orderItemId, String orderId, String productId, String productName,
                       BigDecimal unitPrice, Integer quantity, BigDecimal totalPrice, String productSpecifications) {
        this.orderItemId = orderItemId;
        this.orderId = orderId;
        this.productId = productId;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.productSpecifications = productSpecifications;
    }
    
    // Getters and Setters
    public String getOrderItemId() { return orderItemId; }
    public void setOrderItemId(String orderItemId) { this.orderItemId = orderItemId; }
    
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    
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