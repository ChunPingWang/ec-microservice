package com.ecommerce.payment.application.service;

import com.ecommerce.payment.application.dto.PaymentNotification;
import com.ecommerce.payment.application.port.out.PaymentNotificationPort;
import com.ecommerce.payment.domain.event.PaymentFailedEvent;
import com.ecommerce.payment.domain.event.PaymentRefundedEvent;
import com.ecommerce.payment.domain.event.PaymentSuccessEvent;
import com.ecommerce.payment.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * PaymentNotificationService 單元測試
 * 測試付款通知服務的通知機制
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("付款通知服務測試")
class PaymentNotificationServiceTest {

    @Mock
    private PaymentNotificationPort paymentNotificationPort;
    
    private PaymentNotificationService paymentNotificationService;
    
    @BeforeEach
    void setUp() {
        paymentNotificationService = new PaymentNotificationService(paymentNotificationPort);
    }
    
    @Test
    @DisplayName("處理付款成功事件並發送通知")
    void shouldHandlePaymentSuccessEventAndSendNotification() {
        // Given
        PaymentSuccessEvent event = new PaymentSuccessEvent(
            "TXN-123456",
            "ORDER-789",
            "CUSTOMER-456",
            new BigDecimal("1000.00"),
            PaymentMethod.CREDIT_CARD,
            "GTW-123456",
            LocalDateTime.now()
        );
        
        // When
        paymentNotificationService.handlePaymentSuccessEvent(event);
        
        // Then
        ArgumentCaptor<PaymentNotification> notificationCaptor = ArgumentCaptor.forClass(PaymentNotification.class);
        verify(paymentNotificationPort, timeout(1000)).sendPaymentSuccessNotification(notificationCaptor.capture());
        
        PaymentNotification notification = notificationCaptor.getValue();
        assertThat(notification.getTransactionId()).isEqualTo(event.getTransactionId());
        assertThat(notification.getOrderId()).isEqualTo(event.getOrderId());
        assertThat(notification.getCustomerId()).isEqualTo(event.getCustomerId());
        assertThat(notification.getAmount()).isEqualTo(event.getAmount());
        assertThat(notification.getPaymentMethod()).isEqualTo(event.getPaymentMethod());
        assertThat(notification.getGatewayTransactionId()).isEqualTo(event.getGatewayTransactionId());
        assertThat(notification.getNotificationType()).isEqualTo(PaymentNotification.NotificationType.PAYMENT_SUCCESS);
    }
    
    @Test
    @DisplayName("處理付款失敗事件並發送通知")
    void shouldHandlePaymentFailedEventAndSendNotification() {
        // Given
        PaymentFailedEvent event = new PaymentFailedEvent(
            "TXN-123456",
            "ORDER-789",
            "CUSTOMER-456",
            new BigDecimal("1000.00"),
            PaymentMethod.CREDIT_CARD,
            "INSUFFICIENT_FUNDS",
            false,
            LocalDateTime.now()
        );
        
        // When
        paymentNotificationService.handlePaymentFailedEvent(event);
        
        // Then
        ArgumentCaptor<PaymentNotification> notificationCaptor = ArgumentCaptor.forClass(PaymentNotification.class);
        verify(paymentNotificationPort, timeout(1000)).sendPaymentFailureNotification(notificationCaptor.capture());
        
        PaymentNotification notification = notificationCaptor.getValue();
        assertThat(notification.getTransactionId()).isEqualTo(event.getTransactionId());
        assertThat(notification.getOrderId()).isEqualTo(event.getOrderId());
        assertThat(notification.getCustomerId()).isEqualTo(event.getCustomerId());
        assertThat(notification.getAmount()).isEqualTo(event.getAmount());
        assertThat(notification.getFailureReason()).isEqualTo(event.getFailureReason());
        assertThat(notification.getNotificationType()).isEqualTo(PaymentNotification.NotificationType.PAYMENT_FAILURE);
    }
    
    @Test
    @DisplayName("處理退款事件並發送通知")
    void shouldHandlePaymentRefundedEventAndSendNotification() {
        // Given
        PaymentRefundedEvent event = new PaymentRefundedEvent(
            "REFUND-123456",
            "ORDER-789",
            "CUSTOMER-456",
            new BigDecimal("500.00"),
            "TXN-123456",
            PaymentMethod.CREDIT_CARD,
            "Customer requested refund",
            LocalDateTime.now()
        );
        
        // When
        paymentNotificationService.handlePaymentRefundedEvent(event);
        
        // Then
        ArgumentCaptor<PaymentNotification> notificationCaptor = ArgumentCaptor.forClass(PaymentNotification.class);
        verify(paymentNotificationPort, timeout(1000)).sendRefundSuccessNotification(notificationCaptor.capture());
        
        PaymentNotification notification = notificationCaptor.getValue();
        assertThat(notification.getTransactionId()).isEqualTo(event.getTransactionId());
        assertThat(notification.getOrderId()).isEqualTo(event.getOrderId());
        assertThat(notification.getCustomerId()).isEqualTo(event.getCustomerId());
        assertThat(notification.getAmount()).isEqualTo(event.getAmount());
        assertThat(notification.getNotificationType()).isEqualTo(PaymentNotification.NotificationType.REFUND_SUCCESS);
    }
    
    @Test
    @DisplayName("發送付款狀態通知 - 成功付款")
    void shouldSendPaymentStatusNotificationForSuccessfulPayment() {
        // Given
        PaymentTransaction transaction = createSuccessfulTransaction();
        
        // When
        CompletableFuture<Void> future = paymentNotificationService.sendPaymentStatusNotification(transaction);
        future.join(); // Wait for completion
        
        // Then
        ArgumentCaptor<PaymentNotification> notificationCaptor = ArgumentCaptor.forClass(PaymentNotification.class);
        verify(paymentNotificationPort).sendPaymentSuccessNotification(notificationCaptor.capture());
        
        PaymentNotification notification = notificationCaptor.getValue();
        assertThat(notification.getTransactionId()).isEqualTo(transaction.getTransactionId());
        assertThat(notification.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(notification.getNotificationType()).isEqualTo(PaymentNotification.NotificationType.PAYMENT_SUCCESS);
    }
    
    @Test
    @DisplayName("發送付款狀態通知 - 失敗付款")
    void shouldSendPaymentStatusNotificationForFailedPayment() {
        // Given
        PaymentTransaction transaction = createFailedTransaction();
        
        // When
        CompletableFuture<Void> future = paymentNotificationService.sendPaymentStatusNotification(transaction);
        future.join(); // Wait for completion
        
        // Then
        ArgumentCaptor<PaymentNotification> notificationCaptor = ArgumentCaptor.forClass(PaymentNotification.class);
        verify(paymentNotificationPort).sendPaymentFailureNotification(notificationCaptor.capture());
        
        PaymentNotification notification = notificationCaptor.getValue();
        assertThat(notification.getTransactionId()).isEqualTo(transaction.getTransactionId());
        assertThat(notification.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(notification.getFailureReason()).isEqualTo("INSUFFICIENT_FUNDS");
        assertThat(notification.getNotificationType()).isEqualTo(PaymentNotification.NotificationType.PAYMENT_FAILURE);
    }
    
    @Test
    @DisplayName("發送付款狀態通知 - 取消付款")
    void shouldSendPaymentStatusNotificationForCancelledPayment() {
        // Given
        PaymentTransaction transaction = createCancelledTransaction();
        
        // When
        CompletableFuture<Void> future = paymentNotificationService.sendPaymentStatusNotification(transaction);
        future.join(); // Wait for completion
        
        // Then
        ArgumentCaptor<PaymentNotification> notificationCaptor = ArgumentCaptor.forClass(PaymentNotification.class);
        verify(paymentNotificationPort).sendPaymentCancellationNotification(notificationCaptor.capture());
        
        PaymentNotification notification = notificationCaptor.getValue();
        assertThat(notification.getTransactionId()).isEqualTo(transaction.getTransactionId());
        assertThat(notification.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
        assertThat(notification.getNotificationType()).isEqualTo(PaymentNotification.NotificationType.PAYMENT_CANCELLATION);
    }
    
    @Test
    @DisplayName("發送批量付款通知")
    void shouldSendBatchPaymentNotifications() {
        // Given
        PaymentTransaction successTransaction = createSuccessfulTransaction();
        PaymentTransaction failedTransaction = createFailedTransaction();
        
        // When
        CompletableFuture<Void> future = paymentNotificationService.sendBatchPaymentNotifications(
            successTransaction, failedTransaction
        );
        future.join(); // Wait for completion
        
        // Then
        verify(paymentNotificationPort).sendPaymentSuccessNotification(any(PaymentNotification.class));
        verify(paymentNotificationPort).sendPaymentFailureNotification(any(PaymentNotification.class));
    }
    
    @Test
    @DisplayName("發送付款提醒通知")
    void shouldSendPaymentReminderNotification() {
        // Given
        String customerId = "CUSTOMER-456";
        String orderId = "ORDER-789";
        String customerEmail = "customer@example.com";
        
        // When
        CompletableFuture<Void> future = paymentNotificationService.sendPaymentReminderNotification(
            customerId, orderId, customerEmail
        );
        future.join(); // Wait for completion
        
        // Then
        ArgumentCaptor<PaymentNotification> notificationCaptor = ArgumentCaptor.forClass(PaymentNotification.class);
        verify(paymentNotificationPort).sendPaymentFailureNotification(notificationCaptor.capture());
        
        PaymentNotification notification = notificationCaptor.getValue();
        assertThat(notification.getCustomerId()).isEqualTo(customerId);
        assertThat(notification.getOrderId()).isEqualTo(orderId);
        assertThat(notification.getCustomerEmail()).isEqualTo(customerEmail);
        assertThat(notification.getDescription()).contains("Payment reminder");
    }
    
    @Test
    @DisplayName("發送付款超時通知")
    void shouldSendPaymentTimeoutNotification() {
        // Given
        PaymentTransaction transaction = createTimeoutTransaction();
        
        // When
        CompletableFuture<Void> future = paymentNotificationService.sendPaymentTimeoutNotification(transaction);
        future.join(); // Wait for completion
        
        // Then
        ArgumentCaptor<PaymentNotification> notificationCaptor = ArgumentCaptor.forClass(PaymentNotification.class);
        verify(paymentNotificationPort).sendPaymentFailureNotification(notificationCaptor.capture());
        
        PaymentNotification notification = notificationCaptor.getValue();
        assertThat(notification.getTransactionId()).isEqualTo(transaction.getTransactionId());
        assertThat(notification.getFailureReason()).isEqualTo("Payment timeout");
        assertThat(notification.getNotificationType()).isEqualTo(PaymentNotification.NotificationType.PAYMENT_FAILURE);
    }
    
    @Test
    @DisplayName("重新發送失敗的通知")
    void shouldResendFailedNotification() {
        // Given
        PaymentTransaction transaction = createSuccessfulTransaction();
        
        // When
        CompletableFuture<Boolean> future = paymentNotificationService.resendNotification(transaction);
        Boolean result = future.join(); // Wait for completion
        
        // Then
        assertThat(result).isTrue();
        verify(paymentNotificationPort).sendPaymentSuccessNotification(any(PaymentNotification.class));
    }
    
    @Test
    @DisplayName("通知服務健康檢查")
    void shouldCheckNotificationServiceHealth() {
        // When
        boolean isHealthy = paymentNotificationService.isNotificationServiceHealthy();
        
        // Then
        assertThat(isHealthy).isTrue();
    }
    
    @Test
    @DisplayName("處理通知發送異常")
    void shouldHandleNotificationException() {
        // Given
        PaymentSuccessEvent event = new PaymentSuccessEvent(
            "TXN-123456",
            "ORDER-789",
            "CUSTOMER-456",
            new BigDecimal("1000.00"),
            PaymentMethod.CREDIT_CARD,
            "GTW-123456",
            LocalDateTime.now()
        );
        
        doThrow(new RuntimeException("Notification service unavailable"))
            .when(paymentNotificationPort).sendPaymentSuccessNotification(any());
        
        // When & Then - Should not throw exception
        assertThatCode(() -> paymentNotificationService.handlePaymentSuccessEvent(event))
            .doesNotThrowAnyException();
    }
    
    // Helper methods for creating test data
    private PaymentTransaction createSuccessfulTransaction() {
        PaymentTransaction transaction = PaymentTransaction.createCreditCardPayment(
            "ORDER-789",
            "CUSTOMER-456",
            new BigDecimal("1000.00"),
            CreditCard.create("4111111111111111", "John Doe", YearMonth.now().plusYears(2), "123"),
            "Test payment"
        );
        transaction.startProcessing();
        transaction.markAsSuccess("GTW-123456", "Payment successful");
        return transaction;
    }
    
    private PaymentTransaction createFailedTransaction() {
        PaymentTransaction transaction = PaymentTransaction.createCreditCardPayment(
            "ORDER-789",
            "CUSTOMER-456",
            new BigDecimal("1000.00"),
            CreditCard.create("4111111111111111", "John Doe", YearMonth.now().plusYears(2), "123"),
            "Test payment"
        );
        transaction.startProcessing();
        transaction.markAsFailed("INSUFFICIENT_FUNDS", "Card has insufficient funds");
        return transaction;
    }
    
    private PaymentTransaction createCancelledTransaction() {
        PaymentTransaction transaction = PaymentTransaction.createCreditCardPayment(
            "ORDER-789",
            "CUSTOMER-456",
            new BigDecimal("1000.00"),
            CreditCard.create("4111111111111111", "John Doe", YearMonth.now().plusYears(2), "123"),
            "Test payment"
        );
        transaction.cancel("Customer requested cancellation");
        return transaction;
    }
    
    private PaymentTransaction createTimeoutTransaction() {
        PaymentTransaction transaction = PaymentTransaction.createCreditCardPayment(
            "ORDER-789",
            "CUSTOMER-456",
            new BigDecimal("1000.00"),
            CreditCard.create("4111111111111111", "John Doe", YearMonth.now().plusYears(2), "123"),
            "Test payment"
        );
        transaction.startProcessing();
        transaction.markAsFailed("TIMEOUT", "Payment timeout");
        return transaction;
    }
}