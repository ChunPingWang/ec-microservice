package com.ecommerce.payment.application.strategy;

import com.ecommerce.common.exception.ValidationException;
import com.ecommerce.payment.application.dto.GatewayPaymentRequest;
import com.ecommerce.payment.application.dto.GatewayPaymentResponse;
import com.ecommerce.payment.application.dto.GatewayRefundRequest;
import com.ecommerce.payment.application.dto.GatewayRefundResponse;
import com.ecommerce.payment.application.port.out.PaymentGatewayPort;
import com.ecommerce.payment.domain.exception.PaymentProcessingException;
import com.ecommerce.payment.domain.model.PaymentMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.YearMonth;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * CreditCardPaymentStrategy 單元測試
 * 測試信用卡付款策略的驗證和處理邏輯
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("信用卡付款策略測試")
class CreditCardPaymentStrategyTest {

    @Mock
    private PaymentGatewayPort paymentGatewayPort;
    
    private CreditCardPaymentStrategy creditCardPaymentStrategy;
    
    @BeforeEach
    void setUp() {
        creditCardPaymentStrategy = new CreditCardPaymentStrategy(paymentGatewayPort);
    }
    
    @Test
    @DisplayName("支援的付款方式應為信用卡")
    void shouldSupportCreditCardPaymentMethod() {
        // When
        PaymentMethod supportedMethod = creditCardPaymentStrategy.getSupportedPaymentMethod();
        
        // Then
        assertThat(supportedMethod).isEqualTo(PaymentMethod.CREDIT_CARD);
    }
    
    @Test
    @DisplayName("成功處理信用卡付款")
    void shouldProcessCreditCardPaymentSuccessfully() {
        // Given
        GatewayPaymentRequest request = createValidCreditCardRequest();
        GatewayPaymentResponse expectedResponse = createSuccessfulResponse();
        
        when(paymentGatewayPort.isGatewayHealthy()).thenReturn(true);
        when(paymentGatewayPort.processCreditCardPayment(request)).thenReturn(expectedResponse);
        
        // When
        GatewayPaymentResponse response = creditCardPaymentStrategy.processPayment(request);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.isSuccessful()).isTrue();
        assertThat(response.getGatewayTransactionId()).isEqualTo("GTW-123456");
        assertThat(response.getAmount()).isEqualTo(request.getAmount());
        
        verify(paymentGatewayPort).processCreditCardPayment(request);
    }
    
    @Test
    @DisplayName("閘道不可用時返回網路錯誤")
    void shouldReturnNetworkErrorWhenGatewayUnavailable() {
        // Given
        GatewayPaymentRequest request = createValidCreditCardRequest();
        
        when(paymentGatewayPort.isGatewayHealthy()).thenReturn(false);
        
        // When
        GatewayPaymentResponse response = creditCardPaymentStrategy.processPayment(request);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.isSuccessful()).isFalse();
        assertThat(response.getResponseCode()).isEqualTo("98");
        assertThat(response.isRetryable()).isTrue();
        
        verify(paymentGatewayPort, never()).processCreditCardPayment(any());
    }
    
    @Test
    @DisplayName("無效卡號驗證")
    void shouldRejectInvalidCardNumber() {
        // Given
        GatewayPaymentRequest request = createValidCreditCardRequest();
        request.setCardNumber("1234567890123456"); // Invalid Luhn check
        
        // When
        GatewayPaymentResponse response = creditCardPaymentStrategy.processPayment(request);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.isSuccessful()).isFalse();
        assertThat(response.getResponseCode()).isEqualTo("INVALID_CARD");
        assertThat(response.getResponseMessage()).contains("Invalid card number");
        assertThat(response.isRetryable()).isFalse();
        
        verify(paymentGatewayPort, never()).processCreditCardPayment(any());
    }
    
    @Test
    @DisplayName("過期卡片驗證")
    void shouldRejectExpiredCard() {
        // Given
        GatewayPaymentRequest request = createValidCreditCardRequest();
        request.setExpiryDate(YearMonth.now().minusMonths(1)); // Expired card
        
        // When
        GatewayPaymentResponse response = creditCardPaymentStrategy.processPayment(request);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.isSuccessful()).isFalse();
        assertThat(response.getResponseCode()).isEqualTo("EXPIRED_CARD");
        assertThat(response.getResponseMessage()).contains("Card has expired");
        assertThat(response.isRetryable()).isFalse();
        
        verify(paymentGatewayPort, never()).processCreditCardPayment(any());
    }
    
    @Test
    @DisplayName("無效CVV驗證")
    void shouldRejectInvalidCvv() {
        // Given
        GatewayPaymentRequest request = createValidCreditCardRequest();
        request.setCvv("12"); // Too short
        
        // When
        GatewayPaymentResponse response = creditCardPaymentStrategy.processPayment(request);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.isSuccessful()).isFalse();
        assertThat(response.getResponseCode()).contains("INVALID_CVV");
        assertThat(response.getResponseMessage()).contains("Invalid CVV format");
        assertThat(response.isRetryable()).isFalse();
        
        verify(paymentGatewayPort, never()).processCreditCardPayment(any());
    }
    
    @Test
    @DisplayName("金額過小驗證")
    void shouldRejectAmountTooSmall() {
        // Given
        GatewayPaymentRequest request = createValidCreditCardRequest();
        request.setAmount(new BigDecimal("0.50")); // Below minimum
        
        // When
        GatewayPaymentResponse response = creditCardPaymentStrategy.processPayment(request);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.isSuccessful()).isFalse();
        assertThat(response.getResponseCode()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.getResponseMessage()).contains("Amount must be at least");
        assertThat(response.isRetryable()).isFalse();
        
        verify(paymentGatewayPort, never()).processCreditCardPayment(any());
    }
    
    @Test
    @DisplayName("金額過大驗證")
    void shouldRejectAmountTooLarge() {
        // Given
        GatewayPaymentRequest request = createValidCreditCardRequest();
        request.setAmount(new BigDecimal("150000")); // Above maximum
        
        // When
        GatewayPaymentResponse response = creditCardPaymentStrategy.processPayment(request);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.isSuccessful()).isFalse();
        assertThat(response.getResponseCode()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.getResponseMessage()).contains("exceeds maximum limit");
        assertThat(response.isRetryable()).isFalse();
        
        verify(paymentGatewayPort, never()).processCreditCardPayment(any());
    }
    
    @Test
    @DisplayName("成功處理退款")
    void shouldProcessRefundSuccessfully() {
        // Given
        GatewayRefundRequest request = createValidRefundRequest();
        GatewayRefundResponse expectedResponse = createSuccessfulRefundResponse();
        
        when(paymentGatewayPort.isGatewayHealthy()).thenReturn(true);
        when(paymentGatewayPort.processRefund(request)).thenReturn(expectedResponse);
        
        // When
        GatewayRefundResponse response = creditCardPaymentStrategy.processRefund(request);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.isSuccessful()).isTrue();
        assertThat(response.getGatewayRefundId()).isEqualTo("REFUND-123456");
        assertThat(response.getRefundAmount()).isEqualTo(request.getRefundAmount());
        
        verify(paymentGatewayPort).processRefund(request);
    }
    
    @Test
    @DisplayName("退款請求驗證失敗")
    void shouldRejectInvalidRefundRequest() {
        // Given
        GatewayRefundRequest request = createValidRefundRequest();
        request.setRefundAmount(BigDecimal.ZERO); // Invalid amount
        
        // When
        GatewayRefundResponse response = creditCardPaymentStrategy.processRefund(request);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.isSuccessful()).isFalse();
        assertThat(response.getResponseCode()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.getResponseMessage()).contains("must be greater than zero");
        
        verify(paymentGatewayPort, never()).processRefund(any());
    }
    
    @Test
    @DisplayName("查詢付款狀態成功")
    void shouldQueryPaymentStatusSuccessfully() {
        // Given
        String gatewayTransactionId = "GTW-123456";
        GatewayPaymentResponse expectedResponse = createSuccessfulResponse();
        
        when(paymentGatewayPort.isGatewayHealthy()).thenReturn(true);
        when(paymentGatewayPort.queryPaymentStatus(gatewayTransactionId)).thenReturn(expectedResponse);
        
        // When
        GatewayPaymentResponse response = creditCardPaymentStrategy.queryPaymentStatus(gatewayTransactionId);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.isSuccessful()).isTrue();
        assertThat(response.getGatewayTransactionId()).isEqualTo(gatewayTransactionId);
        
        verify(paymentGatewayPort).queryPaymentStatus(gatewayTransactionId);
    }
    
    @Test
    @DisplayName("檢查服務可用性")
    void shouldCheckServiceAvailability() {
        // Given
        when(paymentGatewayPort.isGatewayHealthy()).thenReturn(true);
        
        // When
        boolean isAvailable = creditCardPaymentStrategy.isAvailable();
        
        // Then
        assertThat(isAvailable).isTrue();
        verify(paymentGatewayPort).isGatewayHealthy();
    }
    
    @Test
    @DisplayName("服務不可用時返回false")
    void shouldReturnFalseWhenServiceUnavailable() {
        // Given
        when(paymentGatewayPort.isGatewayHealthy()).thenThrow(new RuntimeException("Connection failed"));
        
        // When
        boolean isAvailable = creditCardPaymentStrategy.isAvailable();
        
        // Then
        assertThat(isAvailable).isFalse();
    }
    
    // Helper methods for creating test data
    private GatewayPaymentRequest createValidCreditCardRequest() {
        return GatewayPaymentRequest.createCreditCardRequest(
            "TXN-123456",
            "MER-789",
            new BigDecimal("1000.00"),
            "Test payment",
            "4111111111111111", // Valid Visa test card
            "John Doe",
            YearMonth.now().plusYears(2),
            "123"
        );
    }
    
    private GatewayPaymentResponse createSuccessfulResponse() {
        return GatewayPaymentResponse.success(
            "GTW-123456",
            "MER-789",
            new BigDecimal("1000.00"),
            "AUTH-123",
            "RECEIPT-456"
        );
    }
    
    private GatewayRefundRequest createValidRefundRequest() {
        return GatewayRefundRequest.partialRefund(
            "REFUND-123",
            "GTW-123456",
            "MER-789",
            new BigDecimal("500.00"),
            new BigDecimal("1000.00"),
            "Customer requested refund"
        );
    }
    
    private GatewayRefundResponse createSuccessfulRefundResponse() {
        return GatewayRefundResponse.success(
            "REFUND-123456",
            "REFUND-123",
            "GTW-123456",
            "MER-789",
            new BigDecimal("500.00"),
            "REFUND-RECEIPT-789"
        );
    }
}