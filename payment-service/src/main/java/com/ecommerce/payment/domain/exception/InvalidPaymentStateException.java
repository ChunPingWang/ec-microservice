package com.ecommerce.payment.domain.exception;

import com.ecommerce.common.exception.DomainException;
import com.ecommerce.payment.domain.model.PaymentStatus;

/**
 * 無效付款狀態異常
 */
public class InvalidPaymentStateException extends DomainException {
    
    public InvalidPaymentStateException(String message) {
        super(message);
    }
    
    public static InvalidPaymentStateException cannotTransition(PaymentStatus from, PaymentStatus to) {
        return new InvalidPaymentStateException(
            String.format("Cannot transition payment status from %s to %s", from, to)
        );
    }
    
    public static InvalidPaymentStateException cannotRefundInStatus(PaymentStatus status) {
        return new InvalidPaymentStateException(
            String.format("Cannot refund payment in status: %s", status)
        );
    }
    
    public static InvalidPaymentStateException cannotCancelInStatus(PaymentStatus status) {
        return new InvalidPaymentStateException(
            String.format("Cannot cancel payment in status: %s", status)
        );
    }
    
    public static InvalidPaymentStateException insufficientRefundAmount(String transactionId) {
        return new InvalidPaymentStateException(
            String.format("Insufficient refund amount available for transaction: %s", transactionId)
        );
    }
    
    public static InvalidPaymentStateException duplicatePayment(String orderId) {
        return new InvalidPaymentStateException(
            String.format("Order already has a successful payment: %s", orderId)
        );
    }
    
    public static InvalidPaymentStateException paymentLimitExceeded(String limitType) {
        return new InvalidPaymentStateException(
            String.format("Payment limit exceeded: %s", limitType)
        );
    }
}