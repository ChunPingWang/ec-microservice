package com.ecommerce.payment.application.usecase;

import com.ecommerce.payment.application.dto.*;
import com.ecommerce.payment.application.port.out.PaymentNotificationPort;
import com.ecommerce.payment.application.port.out.PaymentPersistencePort;
import com.ecommerce.payment.application.service.PaymentRetryService;
import com.ecommerce.payment.application.strategy.PaymentStrategy;
import com.ecommerce.payment.application.strategy.PaymentStrategyFactory;
import com.ecommerce.payment.domain.exception.PaymentProcessingException;
import com.ecommerce.payment.domain.model.*;
import com.ecommerce.payment.domain.service.PaymentDomainService;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * PaymentProcessingService 單元測試
 * 測試付款處理服務的核心業務邏輯
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("付款處理服務測試")
class PaymentProcessingServiceTest {

    @Mock
    private PaymentPersistencePort paymentPersistencePort;
    
    @Mock
    private PaymentNotificationPort paymentNotificationPort;
    
    @Mock
    private PaymentDomainService paymentDomainService;
    
    @Mock
    private PaymentStrategyFactory strategyFactory;
    
    @Mock
    private PaymentRetryService retryService;
    
    @Mock
    private PaymentStrategy paymentStrategy;
    
    private PaymentProcessingService paymentProcessingService;
    
    @BeforeEach
    void setUp() {
        paymentProcessingService = new PaymentProcessingService(
            paymentPersistencePort,
            paymentNotificationPort,
            paymentDomainService,
            strategyFactory,
            retryService
        );
    }
    
    @Test
    @DisplayName("信用卡付款成功案例")
    void shouldProcessCreditCardPaymentSuccessfully() {
        // Given
        PaymentRequest request = createCreditCardPaymentRequest();
        PaymentTransaction transaction = createMockTransaction();
        GatewayPaymentResponse gatewayResponse = createSuccessfulGatewayResponse();
        
        when(paymentPersistencePort.save(any(PaymentTransaction.class))).thenReturn(transaction);
        when(strategyFactory.getStrategy(PaymentMethod.CREDIT_CARD)).thenReturn(paymentStrategy);
        when(paymentStrategy.processPayment(any(GatewayPaymentRequest.class))).thenReturn(gatewayResponse);
        
        // When
        PaymentResponse response = paymentProcessingService.processPayment(request);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(response.getTransactionId()).isEqualTo(transaction.getTransactionId());
        assertThat(response.getAmount()).isEqualTo(request.getAmount());
        assertThat(response.getPaymentMethod()).isEqualTo(PaymentMethod.CREDIT_CARD);
        
        // Verify domain service validation was called
        verify(paymentDomainService).validatePaymentRequest(
            eq(request.getOrderId()),
            eq(request.getCustomerId()),
            eq(request.getAmount()),
            eq(request.getPaymentMethod()),
            any(CreditCard.class)
        );
        
        // Verify transaction was saved multiple times (initial, processing, success)
        verify(paymentPersistencePort, atLeast(3)).save(any(PaymentTransaction.class));
        
        // Verify payment strategy was called
        verify(paymentStrategy).processPayment(any(GatewayPaymentRequest.class));
        
        // Verify success notification was sent
        verify(paymentNotificationPort).sendPaymentSuccessNotification(any(PaymentNotification.class));
    }
    
    @Test
    @DisplayName("餘額不足錯誤處理")
    void shouldHandleInsufficientFundsError() {
        // Given
        PaymentRequest request = createCreditCardPaymentRequest();
        PaymentTransaction transaction = createMockTransaction();
        GatewayPaymentResponse gatewayResponse = createInsufficientFundsGatewayResponse();
        
        when(paymentPersistencePort.save(any(PaymentTransaction.class))).thenReturn(transaction);
        when(strategyFactory.getStrategy(PaymentMethod.CREDIT_CARD)).thenReturn(paymentStrategy);
        when(paymentStrategy.processPayment(any(GatewayPaymentRequest.class))).thenReturn(gatewayResponse);
        
        // When
        PaymentResponse response = paymentProcessingService.processPayment(request);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(response.getFailureReason()).contains("INSUFFICIENT_FUNDS");
        assertThat(response.isRetryable()).isFalse();
        
        // Verify failure notification was sent
        ArgumentCaptor<PaymentNotification> notificationCaptor = ArgumentCaptor.forClass(PaymentNotification.class);
        verify(paymentNotificationPort).sendPaymentFailureNotification(notificationCaptor.capture());
        
        PaymentNotification notification = notificationCaptor.getValue();
        assertThat(notification.getTransactionId()).isEqualTo(transaction.getTransactionId());
        assertThat(notification.getFailureReason()).isEqualTo("INSUFFICIENT_FUNDS");
        assertThat(notification.getNotificationType()).isEqualTo(PaymentNotification.NotificationType.PAYMENT_FAILURE);
    }
    
    @Test
    @DisplayName("付款處理異常時的錯誤處理")
    void shouldHandlePaymentProcessingException() {
        // Given
        PaymentRequest request = createCreditCardPaymentRequest();
        PaymentTransaction transaction = createMockTransaction();
        
        when(paymentPersistencePort.save(any(PaymentTransaction.class))).thenReturn(transaction);
        when(strategyFactory.getStrategy(PaymentMethod.CREDIT_CARD)).thenReturn(paymentStrategy);
        when(paymentStrategy.processPayment(any(GatewayPaymentRequest.class)))
            .thenThrow(PaymentProcessingException.cardDeclined("Card was declined by issuer"));
        
        // When
        PaymentResponse response = paymentProcessingService.processPayment(request);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(response.getFailureReason()).contains("Card was declined");
        assertThat(response.isRetryable()).isFalse();
    }
    
    @Test
    @DisplayName("網路錯誤時的重試機制")
    void shouldHandleNetworkErrorWithRetry() {
        // Given
        PaymentRequest request = createCreditCardPaymentRequest();
        PaymentTransaction transaction = createMockTransaction();
        GatewayPaymentResponse gatewayResponse = createNetworkErrorGatewayResponse();
        
        when(paymentPersistencePort.save(any(PaymentTransaction.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(strategyFactory.getStrategy(PaymentMethod.CREDIT_CARD)).thenReturn(paymentStrategy);
        when(paymentStrategy.processPayment(any(GatewayPaymentRequest.class))).thenReturn(gatewayResponse);
        when(retryService.isRetryableFailure(any())).thenReturn(true);
        
        // When
        PaymentResponse response = paymentProcessingService.processPayment(request);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(response.getFailureReason()).contains("NETWORK_ERROR");
        assertThat(response.isRetryable()).isTrue();
        
        // Verify failure notification was sent
        verify(paymentNotificationPort).sendPaymentFailureNotification(any(PaymentNotification.class));
    }
    
    @Test
    @DisplayName("付款狀態查詢")
    void shouldGetPaymentStatusSuccessfully() {
        // Given
        String transactionId = "TXN-123456";
        PaymentTransaction transaction = createMockTransaction();
        transaction.setTransactionId(transactionId); // Set the expected transaction ID
        transaction.startProcessing();
        transaction.markAsSuccess("GTW-789", "Payment successful");
        
        when(paymentPersistencePort.findById(transactionId)).thenReturn(Optional.of(transaction));
        when(retryService.isRetryableFailure(any())).thenReturn(false);
        
        // When
        PaymentResponse response = paymentProcessingService.getPaymentStatus(transactionId);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTransactionId()).isEqualTo(transactionId);
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(response.getGatewayTransactionId()).isEqualTo("GTW-789");
    }
    
    @Test
    @DisplayName("付款取消")
    void shouldCancelPaymentSuccessfully() {
        // Given
        String transactionId = "TXN-123456";
        String reason = "Customer requested cancellation";
        PaymentTransaction transaction = createMockTransaction();
        
        when(paymentPersistencePort.findById(transactionId)).thenReturn(Optional.of(transaction));
        when(paymentPersistencePort.save(any(PaymentTransaction.class))).thenReturn(transaction);
        
        // When
        PaymentResponse response = paymentProcessingService.cancelPayment(transactionId, reason);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
        
        // Verify cancellation notification was sent
        verify(paymentNotificationPort).sendPaymentCancellationNotification(any(PaymentNotification.class));
    }
    
    @Test
    @DisplayName("退款處理")
    void shouldProcessRefundSuccessfully() {
        // Given
        RefundRequest refundRequest = createRefundRequest();
        PaymentTransaction originalTransaction = createSuccessfulTransaction();
        GatewayRefundResponse gatewayResponse = createSuccessfulRefundResponse();
        
        when(paymentPersistencePort.findById(refundRequest.getTransactionId()))
            .thenReturn(Optional.of(originalTransaction));
        when(paymentPersistencePort.save(any(PaymentTransaction.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentDomainService.calculateRefundFee(any(), any()))
            .thenReturn(new BigDecimal("10.00"));
        when(strategyFactory.getStrategy(any())).thenReturn(paymentStrategy);
        when(paymentStrategy.processRefund(any(GatewayRefundRequest.class)))
            .thenReturn(gatewayResponse);
        
        // When
        RefundResponse response = paymentProcessingService.processRefund(refundRequest);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.isSuccessful()).isTrue();
        assertThat(response.getRefundAmount()).isEqualTo(refundRequest.getRefundAmount());
        
        // Verify refund notification was sent
        verify(paymentNotificationPort).sendRefundSuccessNotification(any(PaymentNotification.class));
    }
    
    // Helper methods for creating test data
    private PaymentRequest createCreditCardPaymentRequest() {
        PaymentRequest request = new PaymentRequest();
        request.setOrderId("ORDER-123");
        request.setCustomerId("CUSTOMER-456");
        request.setAmount(new BigDecimal("1000.00"));
        request.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        request.setCardNumber("4111111111111111");
        request.setCardHolderName("John Doe");
        request.setExpiryDate(YearMonth.now().plusYears(2));
        request.setCvv("123");
        request.setDescription("Test payment");
        return request;
    }
    
    private PaymentTransaction createMockTransaction() {
        return PaymentTransaction.createCreditCardPayment(
            "ORDER-123",
            "CUSTOMER-456",
            new BigDecimal("1000.00"),
            CreditCard.create("4111111111111111", "John Doe", YearMonth.now().plusYears(2), "123"),
            "Test payment"
        );
    }
    
    private PaymentTransaction createSuccessfulTransaction() {
        PaymentTransaction transaction = createMockTransaction();
        transaction.startProcessing();
        transaction.markAsSuccess("GTW-789", "Payment successful");
        return transaction;
    }
    
    private GatewayPaymentResponse createSuccessfulGatewayResponse() {
        return GatewayPaymentResponse.success(
            "GTW-789",
            "MER-123",
            new BigDecimal("1000.00"),
            "AUTH-123",
            "RECEIPT-456"
        );
    }
    
    private GatewayPaymentResponse createInsufficientFundsGatewayResponse() {
        return GatewayPaymentResponse.failure(
            null,
            "MER-123",
            new BigDecimal("1000.00"),
            "INSUFFICIENT_FUNDS",
            "Insufficient funds",
            "Card has insufficient funds",
            false
        );
    }
    
    private GatewayPaymentResponse createNetworkErrorGatewayResponse() {
        return GatewayPaymentResponse.failure(
            null,
            "MER-123",
            new BigDecimal("1000.00"),
            "NETWORK_ERROR",
            "Network error",
            "Connection timeout",
            true
        );
    }
    
    private RefundRequest createRefundRequest() {
        RefundRequest request = new RefundRequest();
        request.setTransactionId("TXN-123456");
        request.setOrderId("ORDER-123");
        request.setCustomerId("CUSTOMER-456");
        request.setRefundAmount(new BigDecimal("500.00"));
        request.setReason("Customer requested refund");
        request.setPartialRefund(true);
        return request;
    }
    
    private GatewayRefundResponse createSuccessfulRefundResponse() {
        return GatewayRefundResponse.success(
            "REFUND-GTW-456",
            "REFUND-123",
            "GTW-789",
            "MER-123",
            new BigDecimal("500.00"),
            "REFUND-RECEIPT-789"
        );
    }
}