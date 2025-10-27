package com.ecommerce.payment.domain.exception;

import com.ecommerce.common.exception.DomainException;

/**
 * 付款交易不存在異常
 */
public class PaymentNotFoundException extends DomainException {
    
    public PaymentNotFoundException(String message) {
        super(message);
    }
    
    public static PaymentNotFoundException byTransactionId(String transactionId) {
        return new PaymentNotFoundException("Payment transaction not found: " + transactionId);
    }
    
    public static PaymentNotFoundException byOrderId(String orderId) {
        return new PaymentNotFoundException("No payment found for order: " + orderId);
    }
    
    public static PaymentNotFoundException byCustomerId(String customerId) {
        return new PaymentNotFoundException("No payments found for customer: " + customerId);
    }
    
    public static PaymentNotFoundException byGatewayTransactionId(String gatewayTransactionId) {
        return new PaymentNotFoundException("Payment not found for gateway transaction: " + gatewayTransactionId);
    }
}