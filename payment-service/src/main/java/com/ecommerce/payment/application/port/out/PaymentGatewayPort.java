package com.ecommerce.payment.application.port.out;

import com.ecommerce.payment.application.dto.GatewayPaymentRequest;
import com.ecommerce.payment.application.dto.GatewayPaymentResponse;
import com.ecommerce.payment.application.dto.GatewayRefundRequest;
import com.ecommerce.payment.application.dto.GatewayRefundResponse;

/**
 * 付款閘道輸出埠
 * 定義與外部付款閘道的整合介面
 */
public interface PaymentGatewayPort {
    
    /**
     * 處理信用卡付款
     */
    GatewayPaymentResponse processCreditCardPayment(GatewayPaymentRequest request);
    
    /**
     * 處理金融卡付款
     */
    GatewayPaymentResponse processDebitCardPayment(GatewayPaymentRequest request);
    
    /**
     * 處理銀行轉帳付款
     */
    GatewayPaymentResponse processBankTransferPayment(GatewayPaymentRequest request);
    
    /**
     * 處理退款
     */
    GatewayRefundResponse processRefund(GatewayRefundRequest request);
    
    /**
     * 查詢付款狀態
     */
    GatewayPaymentResponse queryPaymentStatus(String gatewayTransactionId);
    
    /**
     * 檢查閘道連線狀態
     */
    boolean isGatewayHealthy();
}