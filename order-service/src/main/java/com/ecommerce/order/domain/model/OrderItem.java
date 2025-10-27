package com.ecommerce.order.domain.model;

import com.ecommerce.common.architecture.BaseEntity;
import com.ecommerce.common.exception.ValidationException;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * 訂單項目實體
 * 代表訂單中的單一商品項目
 */
public class OrderItem extends BaseEntity {
    
    private String orderItemId;
    private String orderId;
    private String productId;
    private String productName;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal totalPrice;
    private String productSpecifications;
    
    // Private constructor for JPA
    protected OrderItem() {}
    
    // Factory method for creating order items
    public static OrderItem create(String productId, String productName, BigDecimal unitPrice, 
                                 Integer quantity, String productSpecifications) {
        OrderItem orderItem = new OrderItem();
        orderItem.orderItemId = generateOrderItemId();
        orderItem.setProductId(productId);
        orderItem.setProductName(productName);
        orderItem.setUnitPrice(unitPrice);
        orderItem.setQuantity(quantity);
        orderItem.setProductSpecifications(productSpecifications);
        orderItem.calculateTotalPrice();
        return orderItem;
    }
    
    // Business methods
    public void updateQuantity(Integer newQuantity) {
        setQuantity(newQuantity);
        calculateTotalPrice();
    }
    
    public void updatePrice(BigDecimal newUnitPrice) {
        setUnitPrice(newUnitPrice);
        calculateTotalPrice();
    }
    
    private void calculateTotalPrice() {
        if (unitPrice != null && quantity != null) {
            this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }
    
    // Validation methods
    private void setProductId(String productId) {
        if (productId == null || productId.trim().isEmpty()) {
            throw new ValidationException("Product ID is required");
        }
        this.productId = productId.trim();
    }
    
    private void setProductName(String productName) {
        if (productName == null || productName.trim().isEmpty()) {
            throw new ValidationException("Product name is required");
        }
        if (productName.length() > 200) {
            throw new ValidationException("Product name cannot exceed 200 characters");
        }
        this.productName = productName.trim();
    }
    
    private void setUnitPrice(BigDecimal unitPrice) {
        if (unitPrice == null) {
            throw new ValidationException("Unit price is required");
        }
        if (unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Unit price must be greater than zero");
        }
        if (unitPrice.scale() > 2) {
            throw new ValidationException("Unit price cannot have more than 2 decimal places");
        }
        this.unitPrice = unitPrice;
    }
    
    private void setQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new ValidationException("Quantity must be positive");
        }
        if (quantity > 999) {
            throw new ValidationException("Quantity cannot exceed 999");
        }
        this.quantity = quantity;
    }
    
    private void setProductSpecifications(String productSpecifications) {
        if (productSpecifications != null && productSpecifications.length() > 1000) {
            throw new ValidationException("Product specifications cannot exceed 1000 characters");
        }
        this.productSpecifications = productSpecifications;
    }
    
    private static String generateOrderItemId() {
        return "ITEM-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
    }
    
    // Getters
    public String getOrderItemId() { return orderItemId; }
    public String getOrderId() { return orderId; }
    public String getProductId() { return productId; }
    public String getProductName() { return productName; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public Integer getQuantity() { return quantity; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public String getProductSpecifications() { return productSpecifications; }
    
    // Package-private setter for Order entity
    void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderItem orderItem = (OrderItem) o;
        return Objects.equals(orderItemId, orderItem.orderItemId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(orderItemId);
    }
    
    @Override
    public String toString() {
        return "OrderItem{" +
                "orderItemId='" + orderItemId + '\'' +
                ", productId='" + productId + '\'' +
                ", productName='" + productName + '\'' +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", totalPrice=" + totalPrice +
                '}';
    }
}