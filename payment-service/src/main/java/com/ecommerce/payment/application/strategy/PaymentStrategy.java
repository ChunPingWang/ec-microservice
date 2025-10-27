package com.ecommerce.payment.application.strategy;

import com.ecommerce.payment.application.dto.GatewayPaymentRequest;
import com.ecommerce.payment.application.dto.GatewayPaymentResponse;
import com.ecommerce.payment.application.dto.GatewayRefundRequest;
import com.ecommerce.payment.application.dto.GatewayRefundResponse;
import com.ecommerce.payment.domain.model.PaymentMethod;

/**
 * 付款策略介面
 * 定義不同付款方式的處理策略
 */
public interface PaymentStrategy {
    
    /**
     * 取得支援的付款方式
     */
    PaymentMethod getSupportedPaymentMethod();
    
    /**
     * 處理付款
     */
    GatewayPaymentResponse processPayment(GatewayPaymentRequest request);
    
    /**
     * 處理退款
     */
    GatewayRefundResponse processRefund(GatewayRefundRequest request);
    
    /**
     * 查詢付款狀態
     */
    GatewayPaymentResponse queryPaymentStatus(String gatewayTransactionId);
    
    /**
     * 檢查策略是否可用
     */
    boolean isAvailable();
    
    /**
     * 驗證付款請求
     */
    void validatePaymentRequest(GatewayPaymentRequest request);
    
    /**
     * 驗證退款請求
     */
    void validateRefundRequest(GatewayRefundRequest request);
}