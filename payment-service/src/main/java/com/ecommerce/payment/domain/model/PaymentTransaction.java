package com.ecommerce.payment.domain.model;

import com.ecommerce.common.architecture.BaseEntity;
import com.ecommerce.common.exception.ValidationException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 付款交易實體
 * 管理付款交易的完整生命週期和業務邏輯
 */
public class PaymentTransaction extends BaseEntity {
    
    private String transactionId;
    private String orderId;
    private String customerId;
    private BigDecimal amount;
    private BigDecimal refundedAmount;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private String gatewayTransactionId;
    private String gatewayResponse;
    private String failureReason;
    private CreditCard creditCard;
    private LocalDateTime processedAt;
    private LocalDateTime refundedAt;
    private String description;
    private String merchantReference;
    
    // Private constructor for JPA
    protected PaymentTransaction() {}
    
    // Factory method for creating payment transactions
    public static PaymentTransaction create(String orderId, String customerId, BigDecimal amount,
                                          PaymentMethod paymentMethod, String description) {
        PaymentTransaction transaction = new PaymentTransaction();
        transaction.transactionId = generateTransactionId();
        transaction.setOrderId(orderId);
        transaction.setCustomerId(customerId);
        transaction.setAmount(amount);
        transaction.setPaymentMethod(paymentMethod);
        transaction.setDescription(description);
        transaction.status = PaymentStatus.PENDING;
        transaction.refundedAmount = BigDecimal.ZERO;
        transaction.merchantReference = generateMerchantReference();
        return transaction;
    }
    
    // Factory method for credit card payments
    public static PaymentTransaction createCreditCardPayment(String orderId, String customerId, 
                                                           BigDecimal amount, CreditCard creditCard, 
                                                           String description) {
        PaymentTransaction transaction = create(orderId, customerId, amount, 
                                              PaymentMethod.CREDIT_CARD, description);
        transaction.setCreditCard(creditCard);
        return transaction;
    }
    
    // Business methods for payment processing
    public void startProcessing() {
        validateStatusTransition(PaymentStatus.PROCESSING);
        this.status = PaymentStatus.PROCESSING;
        this.setUpdatedAt(LocalDateTime.now());
    }
    
    public void markAsSuccess(String gatewayTransactionId, String gatewayResponse) {
        validateStatusTransition(PaymentStatus.SUCCESS);
        
        if (gatewayTransactionId == null || gatewayTransactionId.trim().isEmpty()) {
            throw new ValidationException("Gateway transaction ID is required for successful payment");
        }
        
        this.status = PaymentStatus.SUCCESS;
        this.gatewayTransactionId = gatewayTransactionId.trim();
        this.gatewayResponse = gatewayResponse;
        this.processedAt = LocalDateTime.now();
        this.setUpdatedAt(LocalDateTime.now());
    }
    
    public void markAsFailed(String failureReason, String gatewayResponse) {
        validateStatusTransition(PaymentStatus.FAILED);
        
        this.status = PaymentStatus.FAILED;
        this.failureReason = failureReason;
        this.gatewayResponse = gatewayResponse;
        this.processedAt = LocalDateTime.now();
        this.setUpdatedAt(LocalDateTime.now());
    }
    
    public void cancel(String reason) {
        if (!status.isCancellable()) {
            throw new ValidationException("Cannot cancel payment in status: " + status);
        }
        
        this.status = PaymentStatus.CANCELLED;
        this.failureReason = reason;
        this.setUpdatedAt(LocalDateTime.now());
    }
    
    public PaymentTransaction refund(BigDecimal refundAmount, String reason) {
        if (!status.isRefundable() && !status.isPartialRefundable()) {
            throw new ValidationException("Cannot refund payment in status: " + status);
        }
        
        if (refundAmount == null || refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Refund amount must be greater than zero");
        }
        
        BigDecimal availableRefundAmount = amount.subtract(refundedAmount);
        if (refundAmount.compareTo(availableRefundAmount) > 0) {
            throw new ValidationException("Refund amount exceeds available refund amount");
        }
        
        // Create refund transaction
        PaymentTransaction refundTransaction = new PaymentTransaction();
        refundTransaction.transactionId = generateTransactionId();
        refundTransaction.orderId = this.orderId;
        refundTransaction.customerId = this.customerId;
        refundTransaction.amount = refundAmount.negate(); // Negative amount for refund
        refundTransaction.paymentMethod = this.paymentMethod;
        refundTransaction.status = PaymentStatus.SUCCESS;
        refundTransaction.description = "Refund: " + reason;
        refundTransaction.merchantReference = generateMerchantReference();
        refundTransaction.processedAt = LocalDateTime.now();
        refundTransaction.refundedAt = LocalDateTime.now();
        
        // Update original transaction
        this.refundedAmount = this.refundedAmount.add(refundAmount);
        
        if (this.refundedAmount.compareTo(this.amount) == 0) {
            this.status = PaymentStatus.REFUNDED;
        } else {
            this.status = PaymentStatus.PARTIAL_REFUNDED;
        }
        
        this.setUpdatedAt(LocalDateTime.now());
        
        return refundTransaction;
    }
    
    // Query methods
    public boolean isSuccessful() {
        return status == PaymentStatus.SUCCESS;
    }
    
    public boolean isFailed() {
        return status == PaymentStatus.FAILED;
    }
    
    public boolean isPending() {
        return status == PaymentStatus.PENDING;
    }
    
    public boolean isProcessing() {
        return status == PaymentStatus.PROCESSING;
    }
    
    public boolean isCancelled() {
        return status == PaymentStatus.CANCELLED;
    }
    
    public boolean isRefunded() {
        return status == PaymentStatus.REFUNDED;
    }
    
    public boolean isPartiallyRefunded() {
        return status == PaymentStatus.PARTIAL_REFUNDED;
    }
    
    public boolean isRefund() {
        return amount.compareTo(BigDecimal.ZERO) < 0;
    }
    
    public BigDecimal getAvailableRefundAmount() {
        if (!status.isRefundable() && !status.isPartialRefundable()) {
            return BigDecimal.ZERO;
        }
        return amount.subtract(refundedAmount);
    }
    
    public boolean canBeRefunded() {
        return status.isRefundable() || status.isPartialRefundable();
    }
    
    public boolean canBeCancelled() {
        return status.isCancellable();
    }
    
    // Private helper methods
    private void validateStatusTransition(PaymentStatus targetStatus) {
        if (status.isFinalStatus() && targetStatus != status) {
            throw new ValidationException("Cannot transition from final status " + status + " to " + targetStatus);
        }
        
        // Define valid transitions
        boolean isValidTransition = switch (status) {
            case PENDING -> targetStatus == PaymentStatus.PROCESSING || 
                           targetStatus == PaymentStatus.CANCELLED;
            case PROCESSING -> targetStatus == PaymentStatus.SUCCESS || 
                              targetStatus == PaymentStatus.FAILED;
            case SUCCESS -> targetStatus == PaymentStatus.REFUNDED || 
                           targetStatus == PaymentStatus.PARTIAL_REFUNDED;
            case FAILED, CANCELLED, REFUNDED, PARTIAL_REFUNDED -> false;
        };
        
        if (!isValidTransition) {
            throw new ValidationException("Invalid status transition from " + status + " to " + targetStatus);
        }
    }
    
    private static String generateTransactionId() {
        return "TXN-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 10000);
    }
    
    // For testing purposes - allow setting transaction ID
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    
    private static String generateMerchantReference() {
        return "MER-" + System.currentTimeMillis();
    }
    
    // Validation methods
    private void setOrderId(String orderId) {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new ValidationException("Order ID is required");
        }
        this.orderId = orderId.trim();
    }
    
    private void setCustomerId(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new ValidationException("Customer ID is required");
        }
        this.customerId = customerId.trim();
    }
    
    private void setAmount(BigDecimal amount) {
        if (amount == null) {
            throw new ValidationException("Amount is required");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Amount must be greater than zero");
        }
        if (amount.scale() > 2) {
            throw new ValidationException("Amount cannot have more than 2 decimal places");
        }
        this.amount = amount;
    }
    
    private void setPaymentMethod(PaymentMethod paymentMethod) {
        if (paymentMethod == null) {
            throw new ValidationException("Payment method is required");
        }
        this.paymentMethod = paymentMethod;
    }
    
    private void setDescription(String description) {
        if (description != null && description.length() > 500) {
            throw new ValidationException("Description cannot exceed 500 characters");
        }
        this.description = description;
    }
    
    private void setCreditCard(CreditCard creditCard) {
        if (paymentMethod == PaymentMethod.CREDIT_CARD && creditCard == null) {
            throw new ValidationException("Credit card information is required for credit card payments");
        }
        this.creditCard = creditCard;
    }
    
    // Getters
    public String getTransactionId() { return transactionId; }
    public String getOrderId() { return orderId; }
    public String getCustomerId() { return customerId; }
    public BigDecimal getAmount() { return amount; }
    public BigDecimal getRefundedAmount() { return refundedAmount; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public PaymentStatus getStatus() { return status; }
    public String getGatewayTransactionId() { return gatewayTransactionId; }
    public String getGatewayResponse() { return gatewayResponse; }
    public String getFailureReason() { return failureReason; }
    public CreditCard getCreditCard() { return creditCard; }
    public LocalDateTime getProcessedAt() { return processedAt; }
    public LocalDateTime getRefundedAt() { return refundedAt; }
    public String getDescription() { return description; }
    public String getMerchantReference() { return merchantReference; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaymentTransaction that = (PaymentTransaction) o;
        return Objects.equals(transactionId, that.transactionId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(transactionId);
    }
    
    @Override
    public String toString() {
        return "PaymentTransaction{" +
                "transactionId='" + transactionId + '\'' +
                ", orderId='" + orderId + '\'' +
                ", customerId='" + customerId + '\'' +
                ", amount=" + amount +
                ", paymentMethod=" + paymentMethod +
                ", status=" + status +
                ", processedAt=" + processedAt +
                '}';
    }
}