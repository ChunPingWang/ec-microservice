package com.ecommerce.payment.application.service;

import com.ecommerce.common.architecture.DomainService;
import com.ecommerce.payment.application.dto.PaymentNotification;
import com.ecommerce.payment.application.port.out.PaymentNotificationPort;
import com.ecommerce.payment.domain.event.*;
import com.ecommerce.payment.domain.model.PaymentTransaction;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;

import java.util.concurrent.CompletableFuture;

/**
 * 付款通知服務
 * 處理付款狀態變更的通知邏輯
 */
@DomainService
public class PaymentNotificationService {
    
    private final PaymentNotificationPort paymentNotificationPort;
    
    public PaymentNotificationService(PaymentNotificationPort paymentNotificationPort) {
        this.paymentNotificationPort = paymentNotificationPort;
    }
    
    /**
     * 處理付款成功事件
     */
    @EventListener
    @Async
    public void handlePaymentSuccessEvent(PaymentSuccessEvent event) {
        try {
            PaymentNotification notification = PaymentNotification.paymentSuccess(
                event.getTransactionId(),
                event.getOrderId(),
                event.getCustomerId(),
                null, // 需要從客戶服務取得 email
                event.getAmount(),
                event.getPaymentMethod(),
                event.getGatewayTransactionId(),
                null // 遮罩卡號
            );
            
            paymentNotificationPort.sendPaymentSuccessNotification(notification);
            
        } catch (Exception e) {
            handleNotificationError("PaymentSuccess", event.getTransactionId(), e);
        }
    }
    
    /**
     * 處理付款失敗事件
     */
    @EventListener
    @Async
    public void handlePaymentFailedEvent(PaymentFailedEvent event) {
        try {
            PaymentNotification notification = PaymentNotification.paymentFailure(
                event.getTransactionId(),
                event.getOrderId(),
                event.getCustomerId(),
                null, // 需要從客戶服務取得 email
                event.getAmount(),
                null, // 需要從交易中取得付款方式
                event.getFailureReason()
            );
            
            paymentNotificationPort.sendPaymentFailureNotification(notification);
            
        } catch (Exception e) {
            handleNotificationError("PaymentFailed", event.getTransactionId(), e);
        }
    }
    
    /**
     * 處理退款成功事件
     */
    @EventListener
    @Async
    public void handlePaymentRefundedEvent(PaymentRefundedEvent event) {
        try {
            PaymentNotification notification = PaymentNotification.refundSuccess(
                event.getTransactionId(),
                null, // 原始交易ID
                event.getOrderId(),
                event.getCustomerId(),
                null, // 需要從客戶服務取得 email
                event.getAmount(),
                null, // 需要從交易中取得付款方式
                "Refund processed"
            );
            
            paymentNotificationPort.sendRefundSuccessNotification(notification);
            
        } catch (Exception e) {
            handleNotificationError("PaymentRefunded", event.getTransactionId(), e);
        }
    }
    
    /**
     * 發送即時付款狀態通知
     */
    public CompletableFuture<Void> sendPaymentStatusNotification(PaymentTransaction transaction) {
        return CompletableFuture.runAsync(() -> {
            try {
                PaymentNotification notification = createNotificationFromTransaction(transaction);
                
                switch (transaction.getStatus()) {
                    case SUCCESS:
                        paymentNotificationPort.sendPaymentSuccessNotification(notification);
                        break;
                    case FAILED:
                        paymentNotificationPort.sendPaymentFailureNotification(notification);
                        break;
                    case CANCELLED:
                        paymentNotificationPort.sendPaymentCancellationNotification(notification);
                        break;
                    case REFUNDED:
                    case PARTIAL_REFUNDED:
                        paymentNotificationPort.sendRefundSuccessNotification(notification);
                        break;
                    default:
                        // 其他狀態不發送通知
                        break;
                }
                
            } catch (Exception e) {
                handleNotificationError("PaymentStatus", transaction.getTransactionId(), e);
            }
        });
    }
    
    /**
     * 發送批量付款狀態通知
     */
    public CompletableFuture<Void> sendBatchPaymentNotifications(PaymentTransaction... transactions) {
        return CompletableFuture.runAsync(() -> {
            for (PaymentTransaction transaction : transactions) {
                try {
                    sendPaymentStatusNotification(transaction).join();
                } catch (Exception e) {
                    handleNotificationError("BatchPayment", transaction.getTransactionId(), e);
                }
            }
        });
    }
    
    /**
     * 發送付款提醒通知
     */
    public CompletableFuture<Void> sendPaymentReminderNotification(String customerId, String orderId, 
                                                                  String customerEmail) {
        return CompletableFuture.runAsync(() -> {
            try {
                // 創建提醒通知
                PaymentNotification notification = new PaymentNotification();
                notification.setCustomerId(customerId);
                notification.setOrderId(orderId);
                notification.setCustomerEmail(customerEmail);
                notification.setNotificationType(PaymentNotification.NotificationType.PAYMENT_FAILURE);
                notification.setDescription("Payment reminder for order: " + orderId);
                
                // 這裡可以擴展為專門的提醒通知
                paymentNotificationPort.sendPaymentFailureNotification(notification);
                
            } catch (Exception e) {
                handleNotificationError("PaymentReminder", orderId, e);
            }
        });
    }
    
    /**
     * 發送付款超時通知
     */
    public CompletableFuture<Void> sendPaymentTimeoutNotification(PaymentTransaction transaction) {
        return CompletableFuture.runAsync(() -> {
            try {
                PaymentNotification notification = PaymentNotification.paymentFailure(
                    transaction.getTransactionId(),
                    transaction.getOrderId(),
                    transaction.getCustomerId(),
                    null, // 需要從客戶服務取得 email
                    transaction.getAmount(),
                    transaction.getPaymentMethod(),
                    "Payment timeout"
                );
                
                paymentNotificationPort.sendPaymentFailureNotification(notification);
                
            } catch (Exception e) {
                handleNotificationError("PaymentTimeout", transaction.getTransactionId(), e);
            }
        });
    }
    
    /**
     * 檢查通知服務健康狀態
     */
    public boolean isNotificationServiceHealthy() {
        try {
            // 這裡可以實作健康檢查邏輯
            // 例如：檢查通知服務連線、發送測試通知等
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 重新發送失敗的通知
     */
    public CompletableFuture<Boolean> resendNotification(PaymentTransaction transaction) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                sendPaymentStatusNotification(transaction).join();
                return true;
            } catch (Exception e) {
                handleNotificationError("ResendNotification", transaction.getTransactionId(), e);
                return false;
            }
        });
    }
    
    // Private helper methods
    private PaymentNotification createNotificationFromTransaction(PaymentTransaction transaction) {
        PaymentNotification notification = new PaymentNotification();
        notification.setTransactionId(transaction.getTransactionId());
        notification.setOrderId(transaction.getOrderId());
        notification.setCustomerId(transaction.getCustomerId());
        notification.setAmount(transaction.getAmount());
        notification.setPaymentMethod(transaction.getPaymentMethod());
        notification.setStatus(transaction.getStatus());
        notification.setGatewayTransactionId(transaction.getGatewayTransactionId());
        notification.setFailureReason(transaction.getFailureReason());
        notification.setDescription(transaction.getDescription());
        notification.setProcessedAt(transaction.getProcessedAt());
        
        if (transaction.getCreditCard() != null) {
            notification.setMaskedCardNumber(transaction.getCreditCard().getMaskedCardNumber());
        }
        
        // 設定通知類型
        switch (transaction.getStatus()) {
            case SUCCESS:
                notification.setNotificationType(PaymentNotification.NotificationType.PAYMENT_SUCCESS);
                break;
            case FAILED:
                notification.setNotificationType(PaymentNotification.NotificationType.PAYMENT_FAILURE);
                break;
            case CANCELLED:
                notification.setNotificationType(PaymentNotification.NotificationType.PAYMENT_CANCELLATION);
                break;
            case REFUNDED:
            case PARTIAL_REFUNDED:
                notification.setNotificationType(PaymentNotification.NotificationType.REFUND_SUCCESS);
                break;
            default:
                notification.setNotificationType(PaymentNotification.NotificationType.PAYMENT_FAILURE);
                break;
        }
        
        return notification;
    }
    
    private void handleNotificationError(String notificationType, String transactionId, Exception e) {
        String errorMessage = String.format(
            "Failed to send %s notification for transaction %s: %s",
            notificationType,
            transactionId,
            e.getMessage()
        );
        
        // 記錄錯誤日誌
        System.err.println("[NOTIFICATION_ERROR] " + errorMessage);
        
        // 這裡可以實作錯誤處理邏輯，例如：
        // 1. 記錄到錯誤日誌系統
        // 2. 發送到死信佇列
        // 3. 觸發重試機制
        // 4. 發送警報給運維團隊
    }
}