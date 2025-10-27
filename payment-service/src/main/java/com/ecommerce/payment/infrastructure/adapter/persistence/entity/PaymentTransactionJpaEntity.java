package com.ecommerce.payment.infrastructure.adapter.persistence.entity;

import com.ecommerce.payment.domain.model.PaymentMethod;
import com.ecommerce.payment.domain.model.PaymentStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 付款交易 JPA 實體
 * 用於資料庫持久化的實體類別
 */
@Entity
@Table(name = "payment_transactions", indexes = {
    @Index(name = "idx_payment_order_id", columnList = "orderId"),
    @Index(name = "idx_payment_customer_id", columnList = "customerId"),
    @Index(name = "idx_payment_status", columnList = "status"),
    @Index(name = "idx_payment_gateway_transaction_id", columnList = "gatewayTransactionId"),
    @Index(name = "idx_payment_created_at", columnList = "createdAt")
})
public class PaymentTransactionJpaEntity {
    
    @Id
    @Column(name = "transaction_id", length = 50)
    private String transactionId;
    
    @Column(name = "order_id", nullable = false, length = 50)
    private String orderId;
    
    @Column(name = "customer_id", nullable = false, length = 50)
    private String customerId;
    
    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "refunded_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal refundedAmount = BigDecimal.ZERO;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PaymentStatus status;
    
    @Column(name = "gateway_transaction_id", length = 100)
    private String gatewayTransactionId;
    
    @Column(name = "gateway_response", columnDefinition = "TEXT")
    private String gatewayResponse;
    
    @Column(name = "failure_reason", length = 500)
    private String failureReason;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "merchant_reference", length = 100)
    private String merchantReference;
    
    // Credit card information (encrypted in production)
    @Column(name = "masked_card_number", length = 20)
    private String maskedCardNumber;
    
    @Column(name = "card_holder_name", length = 100)
    private String cardHolderName;
    
    @Column(name = "card_expiry_month")
    private Integer cardExpiryMonth;
    
    @Column(name = "card_expiry_year")
    private Integer cardExpiryYear;
    
    // Timestamps
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
    
    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;
    
    // JPA requires default constructor
    protected PaymentTransactionJpaEntity() {}
    
    public PaymentTransactionJpaEntity(String transactionId, String orderId, String customerId,
                                     BigDecimal amount, PaymentMethod paymentMethod, PaymentStatus status) {
        this.transactionId = transactionId;
        this.orderId = orderId;
        this.customerId = customerId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.refundedAmount = BigDecimal.ZERO;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public BigDecimal getRefundedAmount() { return refundedAmount; }
    public void setRefundedAmount(BigDecimal refundedAmount) { this.refundedAmount = refundedAmount; }
    
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
    
    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }
    
    public String getGatewayTransactionId() { return gatewayTransactionId; }
    public void setGatewayTransactionId(String gatewayTransactionId) { this.gatewayTransactionId = gatewayTransactionId; }
    
    public String getGatewayResponse() { return gatewayResponse; }
    public void setGatewayResponse(String gatewayResponse) { this.gatewayResponse = gatewayResponse; }
    
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getMerchantReference() { return merchantReference; }
    public void setMerchantReference(String merchantReference) { this.merchantReference = merchantReference; }
    
    public String getMaskedCardNumber() { return maskedCardNumber; }
    public void setMaskedCardNumber(String maskedCardNumber) { this.maskedCardNumber = maskedCardNumber; }
    
    public String getCardHolderName() { return cardHolderName; }
    public void setCardHolderName(String cardHolderName) { this.cardHolderName = cardHolderName; }
    
    public Integer getCardExpiryMonth() { return cardExpiryMonth; }
    public void setCardExpiryMonth(Integer cardExpiryMonth) { this.cardExpiryMonth = cardExpiryMonth; }
    
    public Integer getCardExpiryYear() { return cardExpiryYear; }
    public void setCardExpiryYear(Integer cardExpiryYear) { this.cardExpiryYear = cardExpiryYear; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
    
    public LocalDateTime getRefundedAt() { return refundedAt; }
    public void setRefundedAt(LocalDateTime refundedAt) { this.refundedAt = refundedAt; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaymentTransactionJpaEntity that = (PaymentTransactionJpaEntity) o;
        return Objects.equals(transactionId, that.transactionId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(transactionId);
    }
    
    @Override
    public String toString() {
        return "PaymentTransactionJpaEntity{" +
                "transactionId='" + transactionId + '\'' +
                ", orderId='" + orderId + '\'' +
                ", customerId='" + customerId + '\'' +
                ", amount=" + amount +
                ", paymentMethod=" + paymentMethod +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}