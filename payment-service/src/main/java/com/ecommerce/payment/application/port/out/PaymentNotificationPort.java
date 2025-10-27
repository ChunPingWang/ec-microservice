package com.ecommerce.payment.application.port.out;

import com.ecommerce.payment.application.dto.PaymentNotification;

/**
 * 付款通知輸出埠
 * 定義付款狀態通知的介面
 */
public interface PaymentNotificationPort {
    
    /**
     * 發送付款成功通知
     */
    void sendPaymentSuccessNotification(PaymentNotification notification);
    
    /**
     * 發送付款失敗通知
     */
    void sendPaymentFailureNotification(PaymentNotification notification);
    
    /**
     * 發送退款成功通知
     */
    void sendRefundSuccessNotification(PaymentNotification notification);
    
    /**
     * 發送退款失敗通知
     */
    void sendRefundFailureNotification(PaymentNotification notification);
    
    /**
     * 發送付款取消通知
     */
    void sendPaymentCancellationNotification(PaymentNotification notification);
}