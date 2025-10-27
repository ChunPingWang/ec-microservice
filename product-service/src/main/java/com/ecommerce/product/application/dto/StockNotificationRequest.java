package com.ecommerce.product.application.dto;

/**
 * Stock notification request DTO
 * Encapsulates notification data for stock-related events
 */
public class StockNotificationRequest {
    
    private String productId;
    private String productName;
    private String customerId;
    private String email;
    private NotificationType notificationType;
    private Integer quantity;
    private String message;
    
    // Constructors
    public StockNotificationRequest() {}
    
    public StockNotificationRequest(String productId, String productName, String customerId, 
                                  String email, NotificationType notificationType) {
        this.productId = productId;
        this.productName = productName;
        this.customerId = customerId;
        this.email = email;
        this.notificationType = notificationType;
    }
    
    public StockNotificationRequest(String productId, String productName, String customerId, 
                                  String email, NotificationType notificationType, Integer quantity) {
        this(productId, productName, customerId, email, notificationType);
        this.quantity = quantity;
    }
    
    // Validation methods
    public boolean isValid() {
        return productId != null && !productId.trim().isEmpty() &&
               productName != null && !productName.trim().isEmpty() &&
               notificationType != null;
    }
    
    public boolean hasCustomerInfo() {
        return customerId != null && !customerId.trim().isEmpty() &&
               email != null && !email.trim().isEmpty();
    }
    
    public boolean hasQuantityInfo() {
        return quantity != null && quantity >= 0;
    }
    
    // Notification types
    public enum NotificationType {
        STOCK_AVAILABLE,
        OUT_OF_STOCK,
        LOW_STOCK_ALERT,
        RESTOCK_NOTIFICATION,
        PRICE_CHANGE
    }
    
    // Getters and Setters
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public NotificationType getNotificationType() { return notificationType; }
    public void setNotificationType(NotificationType notificationType) { this.notificationType = notificationType; }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    @Override
    public String toString() {
        return "StockNotificationRequest{" +
                "productId='" + productId + '\'' +
                ", productName='" + productName + '\'' +
                ", customerId='" + customerId + '\'' +
                ", email='" + email + '\'' +
                ", notificationType=" + notificationType +
                ", quantity=" + quantity +
                ", message='" + message + '\'' +
                '}';
    }
}