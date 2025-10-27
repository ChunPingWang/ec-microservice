package com.ecommerce.order.application.dto;

import com.ecommerce.order.domain.model.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 訂單資料傳輸物件
 */
public class OrderDto {
    
    private String orderId;
    private String customerId;
    private String customerName;
    private String customerEmail;
    private String shippingAddress;
    private String billingAddress;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private BigDecimal shippingFee;
    private BigDecimal taxAmount;
    private BigDecimal finalAmount;
    private String paymentMethod;
    private String notes;
    private LocalDateTime orderDate;
    private LocalDateTime confirmedDate;
    private LocalDateTime paidDate;
    private LocalDateTime shippedDate;
    private LocalDateTime deliveredDate;
    private LocalDateTime cancelledDate;
    private String cancellationReason;
    private List<OrderItemDto> orderItems;
    private int totalItemCount;
    
    // Constructors
    public OrderDto() {}
    
    // Getters and Setters
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    
    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
    
    public String getBillingAddress() { return billingAddress; }
    public void setBillingAddress(String billingAddress) { this.billingAddress = billingAddress; }
    
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    
    public BigDecimal getShippingFee() { return shippingFee; }
    public void setShippingFee(BigDecimal shippingFee) { this.shippingFee = shippingFee; }
    
    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }
    
    public BigDecimal getFinalAmount() { return finalAmount; }
    public void setFinalAmount(BigDecimal finalAmount) { this.finalAmount = finalAmount; }
    
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
    
    public LocalDateTime getConfirmedDate() { return confirmedDate; }
    public void setConfirmedDate(LocalDateTime confirmedDate) { this.confirmedDate = confirmedDate; }
    
    public LocalDateTime getPaidDate() { return paidDate; }
    public void setPaidDate(LocalDateTime paidDate) { this.paidDate = paidDate; }
    
    public LocalDateTime getShippedDate() { return shippedDate; }
    public void setShippedDate(LocalDateTime shippedDate) { this.shippedDate = shippedDate; }
    
    public LocalDateTime getDeliveredDate() { return deliveredDate; }
    public void setDeliveredDate(LocalDateTime deliveredDate) { this.deliveredDate = deliveredDate; }
    
    public LocalDateTime getCancelledDate() { return cancelledDate; }
    public void setCancelledDate(LocalDateTime cancelledDate) { this.cancelledDate = cancelledDate; }
    
    public String getCancellationReason() { return cancellationReason; }
    public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }
    
    public List<OrderItemDto> getOrderItems() { return orderItems; }
    public void setOrderItems(List<OrderItemDto> orderItems) { this.orderItems = orderItems; }
    
    public int getTotalItemCount() { return totalItemCount; }
    public void setTotalItemCount(int totalItemCount) { this.totalItemCount = totalItemCount; }
    
    // Helper methods
    public boolean isPaid() {
        return status == OrderStatus.PAID || status == OrderStatus.SHIPPED || status == OrderStatus.DELIVERED;
    }
    
    public boolean isCompleted() {
        return status == OrderStatus.DELIVERED;
    }
    
    public boolean isCancelled() {
        return status == OrderStatus.CANCELLED;
    }
    
    public boolean isRefunded() {
        return status == OrderStatus.REFUNDED;
    }
    
    public boolean canBeCancelled() {
        return status.isCancellable();
    }
    
    /**
     * 訂單項目 DTO
     */
    public static class OrderItemDto {
        private String orderItemId;
        private String productId;
        private String productName;
        private BigDecimal unitPrice;
        private Integer quantity;
        private BigDecimal totalPrice;
        private String productSpecifications;
        
        // Constructors
        public OrderItemDto() {}
        
        // Getters and Setters
        public String getOrderItemId() { return orderItemId; }
        public void setOrderItemId(String orderItemId) { this.orderItemId = orderItemId; }
        
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