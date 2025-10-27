package com.ecommerce.order.infrastructure.adapter.messaging;

import com.ecommerce.common.architecture.EventPublisher;
import com.ecommerce.order.application.port.out.OrderEventPort;
import com.ecommerce.order.domain.model.Order;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 訂單事件發布器
 * 負責發布訂單相關事件到訊息佇列
 */
@EventPublisher
@Component
public class OrderEventPublisher implements OrderEventPort {
    
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    
    // Exchange and routing key constants
    private static final String ORDER_EXCHANGE = "order.exchange";
    private static final String ORDER_CREATED_ROUTING_KEY = "order.created";
    private static final String ORDER_CONFIRMED_ROUTING_KEY = "order.confirmed";
    private static final String ORDER_PAID_ROUTING_KEY = "order.paid";
    private static final String ORDER_SHIPPED_ROUTING_KEY = "order.shipped";
    private static final String ORDER_DELIVERED_ROUTING_KEY = "order.delivered";
    private static final String ORDER_CANCELLED_ROUTING_KEY = "order.cancelled";
    private static final String ORDER_REFUNDED_ROUTING_KEY = "order.refunded";
    
    public OrderEventPublisher(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public void publishOrderCreated(Order order) {
        OrderCreatedEvent event = new OrderCreatedEvent(
            order.getOrderId(),
            order.getCustomerId(),
            order.getCustomerEmail(),
            order.getTotalAmount(),
            order.getFinalAmount(),
            order.getOrderDate()
        );
        
        publishEvent(ORDER_CREATED_ROUTING_KEY, event);
    }
    
    @Override
    public void publishOrderConfirmed(Order order) {
        OrderConfirmedEvent event = new OrderConfirmedEvent(
            order.getOrderId(),
            order.getCustomerId(),
            order.getFinalAmount(),
            order.getConfirmedDate()
        );
        
        publishEvent(ORDER_CONFIRMED_ROUTING_KEY, event);
    }
    
    @Override
    public void publishOrderPaid(Order order) {
        OrderPaidEvent event = new OrderPaidEvent(
            order.getOrderId(),
            order.getCustomerId(),
            order.getFinalAmount(),
            order.getPaymentMethod(),
            order.getPaidDate(),
            order.getShippingAddress()
        );
        
        publishEvent(ORDER_PAID_ROUTING_KEY, event);
    }
    
    @Override
    public void publishOrderShipped(Order order) {
        OrderShippedEvent event = new OrderShippedEvent(
            order.getOrderId(),
            order.getCustomerId(),
            order.getShippingAddress(),
            order.getShippedDate()
        );
        
        publishEvent(ORDER_SHIPPED_ROUTING_KEY, event);
    }
    
    @Override
    public void publishOrderDelivered(Order order) {
        OrderDeliveredEvent event = new OrderDeliveredEvent(
            order.getOrderId(),
            order.getCustomerId(),
            order.getDeliveredDate()
        );
        
        publishEvent(ORDER_DELIVERED_ROUTING_KEY, event);
    }
    
    @Override
    public void publishOrderCancelled(Order order, String reason) {
        OrderCancelledEvent event = new OrderCancelledEvent(
            order.getOrderId(),
            order.getCustomerId(),
            reason,
            order.getCancelledDate()
        );
        
        publishEvent(ORDER_CANCELLED_ROUTING_KEY, event);
    }
    
    @Override
    public void publishOrderRefunded(Order order, String reason) {
        OrderRefundedEvent event = new OrderRefundedEvent(
            order.getOrderId(),
            order.getCustomerId(),
            order.getFinalAmount(),
            reason,
            LocalDateTime.now()
        );
        
        publishEvent(ORDER_REFUNDED_ROUTING_KEY, event);
    }
    
    private void publishEvent(String routingKey, Object event) {
        try {
            rabbitTemplate.convertAndSend(ORDER_EXCHANGE, routingKey, event);
        } catch (Exception e) {
            // Log error but don't fail the main operation
            System.err.println("Failed to publish event: " + routingKey + ", error: " + e.getMessage());
        }
    }
    
    // Event classes
    public static class OrderCreatedEvent {
        private String orderId;
        private String customerId;
        private String customerEmail;
        private BigDecimal totalAmount;
        private BigDecimal finalAmount;
        private LocalDateTime orderDate;
        
        public OrderCreatedEvent() {}
        
        public OrderCreatedEvent(String orderId, String customerId, String customerEmail,
                               BigDecimal totalAmount, BigDecimal finalAmount, LocalDateTime orderDate) {
            this.orderId = orderId;
            this.customerId = customerId;
            this.customerEmail = customerEmail;
            this.totalAmount = totalAmount;
            this.finalAmount = finalAmount;
            this.orderDate = orderDate;
        }
        
        // Getters and setters
        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }
        
        public String getCustomerId() { return customerId; }
        public void setCustomerId(String customerId) { this.customerId = customerId; }
        
        public String getCustomerEmail() { return customerEmail; }
        public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
        
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
        
        public BigDecimal getFinalAmount() { return finalAmount; }
        public void setFinalAmount(BigDecimal finalAmount) { this.finalAmount = finalAmount; }
        
        public LocalDateTime getOrderDate() { return orderDate; }
        public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
    }
    
    public static class OrderConfirmedEvent {
        private String orderId;
        private String customerId;
        private BigDecimal finalAmount;
        private LocalDateTime confirmedDate;
        
        public OrderConfirmedEvent() {}
        
        public OrderConfirmedEvent(String orderId, String customerId, BigDecimal finalAmount, LocalDateTime confirmedDate) {
            this.orderId = orderId;
            this.customerId = customerId;
            this.finalAmount = finalAmount;
            this.confirmedDate = confirmedDate;
        }
        
        // Getters and setters
        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }
        
        public String getCustomerId() { return customerId; }
        public void setCustomerId(String customerId) { this.customerId = customerId; }
        
        public BigDecimal getFinalAmount() { return finalAmount; }
        public void setFinalAmount(BigDecimal finalAmount) { this.finalAmount = finalAmount; }
        
        public LocalDateTime getConfirmedDate() { return confirmedDate; }
        public void setConfirmedDate(LocalDateTime confirmedDate) { this.confirmedDate = confirmedDate; }
    }
    
    public static class OrderPaidEvent {
        private String orderId;
        private String customerId;
        private BigDecimal finalAmount;
        private String paymentMethod;
        private LocalDateTime paidDate;
        private String shippingAddress;
        
        public OrderPaidEvent() {}
        
        public OrderPaidEvent(String orderId, String customerId, BigDecimal finalAmount,
                            String paymentMethod, LocalDateTime paidDate, String shippingAddress) {
            this.orderId = orderId;
            this.customerId = customerId;
            this.finalAmount = finalAmount;
            this.paymentMethod = paymentMethod;
            this.paidDate = paidDate;
            this.shippingAddress = shippingAddress;
        }
        
        // Getters and setters
        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }
        
        public String getCustomerId() { return customerId; }
        public void setCustomerId(String customerId) { this.customerId = customerId; }
        
        public BigDecimal getFinalAmount() { return finalAmount; }
        public void setFinalAmount(BigDecimal finalAmount) { this.finalAmount = finalAmount; }
        
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
        
        public LocalDateTime getPaidDate() { return paidDate; }
        public void setPaidDate(LocalDateTime paidDate) { this.paidDate = paidDate; }
        
        public String getShippingAddress() { return shippingAddress; }
        public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
    }
    
    public static class OrderShippedEvent {
        private String orderId;
        private String customerId;
        private String shippingAddress;
        private LocalDateTime shippedDate;
        
        public OrderShippedEvent() {}
        
        public OrderShippedEvent(String orderId, String customerId, String shippingAddress, LocalDateTime shippedDate) {
            this.orderId = orderId;
            this.customerId = customerId;
            this.shippingAddress = shippingAddress;
            this.shippedDate = shippedDate;
        }
        
        // Getters and setters
        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }
        
        public String getCustomerId() { return customerId; }
        public void setCustomerId(String customerId) { this.customerId = customerId; }
        
        public String getShippingAddress() { return shippingAddress; }
        public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
        
        public LocalDateTime getShippedDate() { return shippedDate; }
        public void setShippedDate(LocalDateTime shippedDate) { this.shippedDate = shippedDate; }
    }
    
    public static class OrderDeliveredEvent {
        private String orderId;
        private String customerId;
        private LocalDateTime deliveredDate;
        
        public OrderDeliveredEvent() {}
        
        public OrderDeliveredEvent(String orderId, String customerId, LocalDateTime deliveredDate) {
            this.orderId = orderId;
            this.customerId = customerId;
            this.deliveredDate = deliveredDate;
        }
        
        // Getters and setters
        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }
        
        public String getCustomerId() { return customerId; }
        public void setCustomerId(String customerId) { this.customerId = customerId; }
        
        public LocalDateTime getDeliveredDate() { return deliveredDate; }
        public void setDeliveredDate(LocalDateTime deliveredDate) { this.deliveredDate = deliveredDate; }
    }
    
    public static class OrderCancelledEvent {
        private String orderId;
        private String customerId;
        private String reason;
        private LocalDateTime cancelledDate;
        
        public OrderCancelledEvent() {}
        
        public OrderCancelledEvent(String orderId, String customerId, String reason, LocalDateTime cancelledDate) {
            this.orderId = orderId;
            this.customerId = customerId;
            this.reason = reason;
            this.cancelledDate = cancelledDate;
        }
        
        // Getters and setters
        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }
        
        public String getCustomerId() { return customerId; }
        public void setCustomerId(String customerId) { this.customerId = customerId; }
        
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        
        public LocalDateTime getCancelledDate() { return cancelledDate; }
        public void setCancelledDate(LocalDateTime cancelledDate) { this.cancelledDate = cancelledDate; }
    }
    
    public static class OrderRefundedEvent {
        private String orderId;
        private String customerId;
        private BigDecimal refundAmount;
        private String reason;
        private LocalDateTime refundedDate;
        
        public OrderRefundedEvent() {}
        
        public OrderRefundedEvent(String orderId, String customerId, BigDecimal refundAmount,
                                String reason, LocalDateTime refundedDate) {
            this.orderId = orderId;
            this.customerId = customerId;
            this.refundAmount = refundAmount;
            this.reason = reason;
            this.refundedDate = refundedDate;
        }
        
        // Getters and setters
        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }
        
        public String getCustomerId() { return customerId; }
        public void setCustomerId(String customerId) { this.customerId = customerId; }
        
        public BigDecimal getRefundAmount() { return refundAmount; }
        public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }
        
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        
        public LocalDateTime getRefundedDate() { return refundedDate; }
        public void setRefundedDate(LocalDateTime refundedDate) { this.refundedDate = refundedDate; }
    }
}