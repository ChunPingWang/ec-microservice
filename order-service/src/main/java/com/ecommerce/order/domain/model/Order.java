package com.ecommerce.order.domain.model;

import com.ecommerce.common.architecture.BaseEntity;
import com.ecommerce.common.exception.ValidationException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 訂單實體
 * 管理訂單的完整生命週期和業務邏輯
 */
public class Order extends BaseEntity {
    
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
    private List<OrderItem> orderItems = new ArrayList<>();
    
    // Private constructor for JPA
    protected Order() {}
    
    // Factory method for creating orders
    public static Order create(String customerId, String customerName, String customerEmail,
                              String shippingAddress, String billingAddress) {
        Order order = new Order();
        order.orderId = generateOrderId();
        order.setCustomerId(customerId);
        order.setCustomerName(customerName);
        order.setCustomerEmail(customerEmail);
        order.setShippingAddress(shippingAddress);
        order.setBillingAddress(billingAddress);
        order.status = OrderStatus.PENDING;
        order.orderDate = LocalDateTime.now();
        order.totalAmount = BigDecimal.ZERO;
        order.shippingFee = BigDecimal.ZERO;
        order.taxAmount = BigDecimal.ZERO;
        order.finalAmount = BigDecimal.ZERO;
        return order;
    }
    
    // Business methods for order management
    public void addOrderItem(OrderItem orderItem) {
        if (orderItem == null) {
            throw new ValidationException("Order item cannot be null");
        }
        if (!canModifyItems()) {
            throw new ValidationException("Cannot modify items in current order status: " + status);
        }
        
        orderItem.setOrderId(this.orderId);
        this.orderItems.add(orderItem);
        recalculateAmounts();
    }
    
    public void removeOrderItem(String orderItemId) {
        if (!canModifyItems()) {
            throw new ValidationException("Cannot modify items in current order status: " + status);
        }
        
        boolean removed = orderItems.removeIf(item -> item.getOrderItemId().equals(orderItemId));
        if (!removed) {
            throw new ValidationException("Order item not found: " + orderItemId);
        }
        recalculateAmounts();
    }
    
    public void updateOrderItemQuantity(String orderItemId, Integer newQuantity) {
        if (!canModifyItems()) {
            throw new ValidationException("Cannot modify items in current order status: " + status);
        }
        
        OrderItem orderItem = findOrderItem(orderItemId);
        orderItem.updateQuantity(newQuantity);
        recalculateAmounts();
    }
    
    // Order status management
    public void confirm() {
        validateStatusTransition(OrderStatus.CONFIRMED);
        if (orderItems.isEmpty()) {
            throw new ValidationException("Cannot confirm order without items");
        }
        if (finalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Cannot confirm order with zero or negative amount");
        }
        
        this.status = OrderStatus.CONFIRMED;
        this.confirmedDate = LocalDateTime.now();
        this.setUpdatedAt(LocalDateTime.now());
    }
    
    public void markAsPaid(String paymentMethod) {
        validateStatusTransition(OrderStatus.PAID);
        setPaymentMethod(paymentMethod);
        
        this.status = OrderStatus.PAID;
        this.paidDate = LocalDateTime.now();
        this.setUpdatedAt(LocalDateTime.now());
    }
    
    public void ship() {
        validateStatusTransition(OrderStatus.SHIPPED);
        
        this.status = OrderStatus.SHIPPED;
        this.shippedDate = LocalDateTime.now();
        this.setUpdatedAt(LocalDateTime.now());
    }
    
    public void deliver() {
        validateStatusTransition(OrderStatus.DELIVERED);
        
        this.status = OrderStatus.DELIVERED;
        this.deliveredDate = LocalDateTime.now();
        this.setUpdatedAt(LocalDateTime.now());
    }
    
    public void cancel(String reason) {
        if (!status.isCancellable()) {
            throw new ValidationException("Cannot cancel order in status: " + status);
        }
        
        this.status = OrderStatus.CANCELLED;
        this.cancelledDate = LocalDateTime.now();
        this.cancellationReason = reason;
        this.setUpdatedAt(LocalDateTime.now());
    }
    
    public void refund() {
        if (status != OrderStatus.PAID && status != OrderStatus.SHIPPED && status != OrderStatus.DELIVERED) {
            throw new ValidationException("Cannot refund order in status: " + status);
        }
        
        this.status = OrderStatus.REFUNDED;
        this.setUpdatedAt(LocalDateTime.now());
    }
    
    // Calculation methods
    public void setShippingFee(BigDecimal shippingFee) {
        if (shippingFee == null || shippingFee.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Shipping fee cannot be negative");
        }
        this.shippingFee = shippingFee;
        recalculateAmounts();
    }
    
    public void setTaxAmount(BigDecimal taxAmount) {
        if (taxAmount == null || taxAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Tax amount cannot be negative");
        }
        this.taxAmount = taxAmount;
        recalculateAmounts();
    }
    
    private void recalculateAmounts() {
        this.totalAmount = orderItems.stream()
            .map(OrderItem::getTotalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        this.finalAmount = totalAmount.add(shippingFee).add(taxAmount);
        this.setUpdatedAt(LocalDateTime.now());
    }
    
    // Query methods
    public boolean canModifyItems() {
        return status == OrderStatus.PENDING;
    }
    
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
    
    public int getTotalItemCount() {
        return orderItems.stream()
            .mapToInt(OrderItem::getQuantity)
            .sum();
    }
    
    public List<OrderItem> getOrderItems() {
        return Collections.unmodifiableList(orderItems);
    }
    
    public OrderItem findOrderItem(String orderItemId) {
        return orderItems.stream()
            .filter(item -> item.getOrderItemId().equals(orderItemId))
            .findFirst()
            .orElseThrow(() -> new ValidationException("Order item not found: " + orderItemId));
    }
    
    // Private helper methods
    private void validateStatusTransition(OrderStatus targetStatus) {
        if (!status.canTransitionTo(targetStatus)) {
            throw new ValidationException("Cannot transition from " + status + " to " + targetStatus);
        }
    }
    
    private static String generateOrderId() {
        return "ORD-" + System.currentTimeMillis();
    }
    
    // Validation methods
    private void setCustomerId(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new ValidationException("Customer ID is required");
        }
        this.customerId = customerId.trim();
    }
    
    private void setCustomerName(String customerName) {
        if (customerName == null || customerName.trim().isEmpty()) {
            throw new ValidationException("Customer name is required");
        }
        if (customerName.length() > 100) {
            throw new ValidationException("Customer name cannot exceed 100 characters");
        }
        this.customerName = customerName.trim();
    }
    
    private void setCustomerEmail(String customerEmail) {
        if (customerEmail == null || customerEmail.trim().isEmpty()) {
            throw new ValidationException("Customer email is required");
        }
        if (!customerEmail.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new ValidationException("Invalid email format");
        }
        this.customerEmail = customerEmail.trim().toLowerCase();
    }
    
    private void setShippingAddress(String shippingAddress) {
        if (shippingAddress == null || shippingAddress.trim().isEmpty()) {
            throw new ValidationException("Shipping address is required");
        }
        if (shippingAddress.length() > 500) {
            throw new ValidationException("Shipping address cannot exceed 500 characters");
        }
        this.shippingAddress = shippingAddress.trim();
    }
    
    private void setBillingAddress(String billingAddress) {
        if (billingAddress == null || billingAddress.trim().isEmpty()) {
            throw new ValidationException("Billing address is required");
        }
        if (billingAddress.length() > 500) {
            throw new ValidationException("Billing address cannot exceed 500 characters");
        }
        this.billingAddress = billingAddress.trim();
    }
    
    private void setPaymentMethod(String paymentMethod) {
        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            throw new ValidationException("Payment method is required");
        }
        this.paymentMethod = paymentMethod.trim();
    }
    
    // Getters
    public String getOrderId() { return orderId; }
    public String getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public String getShippingAddress() { return shippingAddress; }
    public String getBillingAddress() { return billingAddress; }
    public OrderStatus getStatus() { return status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public BigDecimal getShippingFee() { return shippingFee; }
    public BigDecimal getTaxAmount() { return taxAmount; }
    public BigDecimal getFinalAmount() { return finalAmount; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getNotes() { return notes; }
    public LocalDateTime getOrderDate() { return orderDate; }
    public LocalDateTime getConfirmedDate() { return confirmedDate; }
    public LocalDateTime getPaidDate() { return paidDate; }
    public LocalDateTime getShippedDate() { return shippedDate; }
    public LocalDateTime getDeliveredDate() { return deliveredDate; }
    public LocalDateTime getCancelledDate() { return cancelledDate; }
    public String getCancellationReason() { return cancellationReason; }
    
    public void setNotes(String notes) {
        if (notes != null && notes.length() > 1000) {
            throw new ValidationException("Notes cannot exceed 1000 characters");
        }
        this.notes = notes;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(orderId, order.orderId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(orderId);
    }
    
    @Override
    public String toString() {
        return "Order{" +
                "orderId='" + orderId + '\'' +
                ", customerId='" + customerId + '\'' +
                ", status=" + status +
                ", totalAmount=" + totalAmount +
                ", finalAmount=" + finalAmount +
                ", itemCount=" + orderItems.size() +
                '}';
    }
}