package com.ecommerce.payment.application.dto;

import com.ecommerce.common.dto.BaseDto;
import com.ecommerce.payment.domain.model.PaymentMethod;
import com.ecommerce.payment.domain.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 付款通知 DTO
 */
public class PaymentNotification extends BaseDto {
    
    private String transactionId;
    private String orderId;
    private String customerId;
    private String customerEmail;
    private String customerPhone;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private String gatewayTransactionId;
    private String failureReason;
    private String description;
    private LocalDateTime processedAt;
    private String maskedCardNumber;
    private NotificationType notificationType;
    
    // Refund specific fields
    private String originalTransactionId;
    private BigDecimal refundAmount;
    private String refundReason;
    
    // Constructors
    public PaymentNotification() {}
    
    public PaymentNotification(String transactionId, String orderId, String customerId,
                             String customerEmail, BigDecimal amount, PaymentMethod paymentMethod,
                             PaymentStatus status, NotificationType notificationType) {
        this.transactionId = transactionId;
        this.orderId = orderId;
        this.customerId = customerId;
        this.customerEmail = customerEmail;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.notificationType = notificationType;
        this.processedAt = LocalDateTime.now();
    }
    
    // Factory methods
    public static PaymentNotification paymentSuccess(String transactionId, String orderId, String customerId,
                                                   String customerEmail, BigDecimal amount, PaymentMethod paymentMethod,
                                                   String gatewayTransactionId, String maskedCardNumber) {
        PaymentNotification notification = new PaymentNotification(transactionId, orderId, customerId,
                                                                  customerEmail, amount, paymentMethod,
                                                                  PaymentStatus.SUCCESS, NotificationType.PAYMENT_SUCCESS);
        notification.setGatewayTransactionId(gatewayTransactionId);
        notification.setMaskedCardNumber(maskedCardNumber);
        return notification;
    }
    
    public static PaymentNotification paymentFailure(String transactionId, String orderId, String customerId,
                                                    String customerEmail, BigDecimal amount, PaymentMethod paymentMethod,
                                                    String failureReason) {
        PaymentNotification notification = new PaymentNotification(transactionId, orderId, customerId,
                                                                  customerEmail, amount, paymentMethod,
                                                                  PaymentStatus.FAILED, NotificationType.PAYMENT_FAILURE);
        notification.setFailureReason(failureReason);
        return notification;
    }
    
    public static PaymentNotification paymentCancellation(String transactionId, String orderId, String customerId,
                                                        String customerEmail, BigDecimal amount, PaymentMethod paymentMethod,
                                                        String reason) {
        PaymentNotification notification = new PaymentNotification(transactionId, orderId, customerId,
                                                                  customerEmail, amount, paymentMethod,
                                                                  PaymentStatus.CANCELLED, NotificationType.PAYMENT_CANCELLATION);
        notification.setFailureReason(reason);
        return notification;
    }
    
    public static PaymentNotification refundSuccess(String refundTransactionId, String originalTransactionId,
                                                   String orderId, String customerId, String customerEmail,
                                                   BigDecimal refundAmount, PaymentMethod paymentMethod,
                                                   String refundReason) {
        PaymentNotification notification = new PaymentNotification(refundTransactionId, orderId, customerId,
                                                                  customerEmail, refundAmount, paymentMethod,
                                                                  PaymentStatus.SUCCESS, NotificationType.REFUND_SUCCESS);
        notification.setOriginalTransactionId(originalTransactionId);
        notification.setRefundAmount(refundAmount);
        notification.setRefundReason(refundReason);
        return notification;
    }
    
    public static PaymentNotification refundFailure(String refundTransactionId, String originalTransactionId,
                                                   String orderId, String customerId, String customerEmail,
                                                   BigDecimal refundAmount, PaymentMethod paymentMethod,
                                                   String failureReason, String refundReason) {
        PaymentNotification notification = new PaymentNotification(refundTransactionId, orderId, customerId,
                                                                  customerEmail, refundAmount, paymentMethod,
                                                                  PaymentStatus.FAILED, NotificationType.REFUND_FAILURE);
        notification.setOriginalTransactionId(originalTransactionId);
        notification.setRefundAmount(refundAmount);
        notification.setRefundReason(refundReason);
        notification.setFailureReason(failureReason);
        return notification;
    }
    
    // Getters and Setters
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    
    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
    
    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }
    
    public String getGatewayTransactionId() { return gatewayTransactionId; }
    public void setGatewayTransactionId(String gatewayTransactionId) { this.gatewayTransactionId = gatewayTransactionId; }
    
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
    
    public String getMaskedCardNumber() { return maskedCardNumber; }
    public void setMaskedCardNumber(String maskedCardNumber) { this.maskedCardNumber = maskedCardNumber; }
    
    public NotificationType getNotificationType() { return notificationType; }
    public void setNotificationType(NotificationType notificationType) { this.notificationType = notificationType; }
    
    public String getOriginalTransactionId() { return originalTransactionId; }
    public void setOriginalTransactionId(String originalTransactionId) { this.originalTransactionId = originalTransactionId; }
    
    public BigDecimal getRefundAmount() { return refundAmount; }
    public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }
    
    public String getRefundReason() { return refundReason; }
    public void setRefundReason(String refundReason) { this.refundReason = refundReason; }
    
    /**
     * 通知類型枚舉
     */
    public enum NotificationType {
        PAYMENT_SUCCESS("付款成功"),
        PAYMENT_FAILURE("付款失敗"),
        PAYMENT_CANCELLATION("付款取消"),
        REFUND_SUCCESS("退款成功"),
        REFUND_FAILURE("退款失敗");
        
        private final String description;
        
        NotificationType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}