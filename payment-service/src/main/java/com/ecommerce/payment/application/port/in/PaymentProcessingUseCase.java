package com.ecommerce.payment.application.port.in;

import com.ecommerce.payment.application.dto.PaymentRequest;
import com.ecommerce.payment.application.dto.PaymentResponse;
import com.ecommerce.payment.application.dto.RefundRequest;
import com.ecommerce.payment.application.dto.RefundResponse;

/**
 * 付款處理使用案例輸入埠
 * 定義付款相關的業務操作
 */
public interface PaymentProcessingUseCase {
    
    /**
     * 處理付款請求
     */
    PaymentResponse processPayment(PaymentRequest request);
    
    /**
     * 查詢付款狀態
     */
    PaymentResponse getPaymentStatus(String transactionId);
    
    /**
     * 取消付款
     */
    PaymentResponse cancelPayment(String transactionId, String reason);
    
    /**
     * 處理退款請求
     */
    RefundResponse processRefund(RefundRequest request);
    
    /**
     * 重試失敗的付款
     */
    PaymentResponse retryPayment(String transactionId);
}