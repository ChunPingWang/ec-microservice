package com.ecommerce.payment.application.mapper;


import com.ecommerce.payment.application.dto.PaymentRequest;
import com.ecommerce.payment.application.dto.PaymentResponse;
import com.ecommerce.payment.domain.model.CreditCard;
import com.ecommerce.payment.domain.model.PaymentTransaction;

/**
 * 付款映射器
 * 處理付款相關的 DTO 與領域物件之間的轉換
 */

public class PaymentMapper {
    
    /**
     * 將付款請求轉換為付款交易
     */
    public static PaymentTransaction toPaymentTransaction(PaymentRequest request) {
        switch (request.getPaymentMethod()) {
            case CREDIT_CARD:
                CreditCard creditCard = CreditCard.create(
                    request.getCardNumber(),
                    request.getCardHolderName(),
                    request.getExpiryDate(),
                    request.getCvv()
                );
                return PaymentTransaction.createCreditCardPayment(
                    request.getOrderId(),
                    request.getCustomerId(),
                    request.getAmount(),
                    creditCard,
                    request.getDescription()
                );
            default:
                return PaymentTransaction.create(
                    request.getOrderId(),
                    request.getCustomerId(),
                    request.getAmount(),
                    request.getPaymentMethod(),
                    request.getDescription()
                );
        }
    }
    
    /**
     * 將付款交易轉換為付款回應
     */
    public static PaymentResponse toPaymentResponse(PaymentTransaction transaction) {
        PaymentResponse response = new PaymentResponse(
            transaction.getTransactionId(),
            transaction.getOrderId(),
            transaction.getCustomerId(),
            transaction.getAmount(),
            transaction.getPaymentMethod(),
            transaction.getStatus()
        );
        
        response.setGatewayTransactionId(transaction.getGatewayTransactionId());
        response.setFailureReason(transaction.getFailureReason());
        response.setDescription(transaction.getDescription());
        response.setProcessedAt(transaction.getProcessedAt());
        
        if (transaction.getCreditCard() != null) {
            response.setMaskedCardNumber(transaction.getCreditCard().getMaskedCardNumber());
        }
        
        return response;
    }
    
    /**
     * 將付款交易轉換為成功回應
     */
    public static PaymentResponse toSuccessResponse(PaymentTransaction transaction) {
        return PaymentResponse.success(
            transaction.getTransactionId(),
            transaction.getOrderId(),
            transaction.getCustomerId(),
            transaction.getAmount(),
            transaction.getPaymentMethod(),
            transaction.getGatewayTransactionId(),
            transaction.getProcessedAt()
        );
    }
    
    /**
     * 將付款交易轉換為失敗回應
     */
    public static PaymentResponse toFailureResponse(PaymentTransaction transaction, boolean retryable) {
        return PaymentResponse.failure(
            transaction.getTransactionId(),
            transaction.getOrderId(),
            transaction.getCustomerId(),
            transaction.getAmount(),
            transaction.getPaymentMethod(),
            transaction.getFailureReason(),
            retryable
        );
    }
    
    /**
     * 將付款交易轉換為待處理回應
     */
    public static PaymentResponse toPendingResponse(PaymentTransaction transaction) {
        return PaymentResponse.pending(
            transaction.getTransactionId(),
            transaction.getOrderId(),
            transaction.getCustomerId(),
            transaction.getAmount(),
            transaction.getPaymentMethod()
        );
    }
    
    /**
     * 將付款交易轉換為處理中回應
     */
    public static PaymentResponse toProcessingResponse(PaymentTransaction transaction) {
        return PaymentResponse.processing(
            transaction.getTransactionId(),
            transaction.getOrderId(),
            transaction.getCustomerId(),
            transaction.getAmount(),
            transaction.getPaymentMethod()
        );
    }
    
    /**
     * 將付款交易轉換為取消回應
     */
    public static PaymentResponse toCancelledResponse(PaymentTransaction transaction) {
        return PaymentResponse.cancelled(
            transaction.getTransactionId(),
            transaction.getOrderId(),
            transaction.getCustomerId(),
            transaction.getAmount(),
            transaction.getPaymentMethod(),
            transaction.getFailureReason()
        );
    }
}