package com.ecommerce.order.infrastructure.adapter.persistence.entity;

import com.ecommerce.order.domain.model.OrderStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 訂單 JPA 實體
 */
@Entity
@Table(name = "orders")
public class OrderJpaEntity {
    
    @Id
    @Column(name = "order_id", length = 50)
    private String orderId;
    
    @Column(name = "customer_id", nullable = false, length = 50)
    private String customerId;
    
    @Column(name = "customer_name", nullable = false, length = 100)
    private String customerName;
    
    @Column(name = "customer_email", nullable = false, length = 255)
    private String customerEmail;
    
    @Column(name = "shipping_address", nullable = false, length = 500)
    private String shippingAddress;
    
    @Column(name = "billing_address", nullable = false, length = 500)
    private String billingAddress;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus status;
    
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;
    
    @Column(name = "shipping_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal shippingFee;
    
    @Column(name = "tax_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal taxAmount;
    
    @Column(name = "final_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal finalAmount;
    
    @Column(name = "payment_method", length = 50)
    private String paymentMethod;
    
    @Column(name = "notes", length = 1000)
    private String notes;
    
    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;
    
    @Column(name = "confirmed_date")
    private LocalDateTime confirmedDate;
    
    @Column(name = "paid_date")
    private LocalDateTime paidDate;
    
    @Column(name = "shipped_date")
    private LocalDateTime shippedDate;
    
    @Column(name = "delivered_date")
    private LocalDateTime deliveredDate;
    
    @Column(name = "cancelled_date")
    private LocalDateTime cancelledDate;
    
    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<OrderItemJpaEntity> orderItems = new ArrayList<>();
    
    // Constructors
    public OrderJpaEntity() {}
    
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
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public List<OrderItemJpaEntity> getOrderItems() { return orderItems; }
    public void setOrderItems(List<OrderItemJpaEntity> orderItems) { this.orderItems = orderItems; }
    
    // Helper methods
    public void addOrderItem(OrderItemJpaEntity orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }
    
    public void removeOrderItem(OrderItemJpaEntity orderItem) {
        orderItems.remove(orderItem);
        orderItem.setOrder(null);
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}