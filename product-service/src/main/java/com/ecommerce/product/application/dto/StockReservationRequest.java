package com.ecommerce.product.application.dto;

/**
 * Stock reservation request DTO
 * Encapsulates stock reservation data for bulk operations
 */
public class StockReservationRequest {
    
    private String productId;
    private Integer quantity;
    private String customerId;
    private String orderId;
    private String reason;
    
    // Constructors
    public StockReservationRequest() {}
    
    public StockReservationRequest(String productId, Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }
    
    public StockReservationRequest(String productId, Integer quantity, String customerId, String orderId) {
        this.productId = productId;
        this.quantity = quantity;
        this.customerId = customerId;
        this.orderId = orderId;
    }
    
    // Validation methods
    public boolean isValid() {
        return productId != null && !productId.trim().isEmpty() && 
               quantity != null && quantity > 0;
    }
    
    public boolean hasCustomerInfo() {
        return customerId != null && !customerId.trim().isEmpty();
    }
    
    public boolean hasOrderInfo() {
        return orderId != null && !orderId.trim().isEmpty();
    }
    
    // Getters and Setters
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    
    @Override
    public String toString() {
        return "StockReservationRequest{" +
                "productId='" + productId + '\'' +
                ", quantity=" + quantity +
                ", customerId='" + customerId + '\'' +
                ", orderId='" + orderId + '\'' +
                ", reason='" + reason + '\'' +
                '}';
    }
}