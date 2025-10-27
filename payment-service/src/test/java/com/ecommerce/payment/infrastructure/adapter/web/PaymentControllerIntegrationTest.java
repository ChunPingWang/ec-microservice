package com.ecommerce.payment.infrastructure.adapter.web;

import com.ecommerce.payment.application.dto.PaymentRequest;
import com.ecommerce.payment.application.dto.PaymentResponse;
import com.ecommerce.payment.application.dto.RefundRequest;
import com.ecommerce.payment.application.dto.RefundResponse;
import com.ecommerce.payment.application.port.in.PaymentProcessingUseCase;
import com.ecommerce.payment.domain.model.PaymentMethod;
import com.ecommerce.payment.domain.model.PaymentStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.YearMonth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * PaymentController 整合測試
 * 測試付款控制器的 REST API 端點
 */
@WebMvcTest(PaymentController.class)
@DisplayName("付款控制器整合測試")
class PaymentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private PaymentProcessingUseCase paymentProcessingUseCase;
    
    @Test
    @DisplayName("處理信用卡付款 - 成功案例")
    void shouldProcessCreditCardPaymentSuccessfully() throws Exception {
        // Given
        PaymentRequest request = createCreditCardPaymentRequest();
        PaymentResponse response = createSuccessfulPaymentResponse();
        
        when(paymentProcessingUseCase.processPayment(any(PaymentRequest.class)))
            .thenReturn(response);
        
        // When & Then
        mockMvc.perform(post("/api/v1/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.transactionId").value(response.getTransactionId()))
                .andExpect(jsonPath("$.data.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.amount").value(1000.00))
                .andExpect(jsonPath("$.data.paymentMethod").value("CREDIT_CARD"))
                .andExpect(jsonPath("$.data.orderId").value("ORDER-123"))
                .andExpect(jsonPath("$.data.customerId").value("CUSTOMER-456"));
    }
    
    @Test
    @DisplayName("處理信用卡付款 - 餘額不足")
    void shouldHandleInsufficientFundsError() throws Exception {
        // Given
        PaymentRequest request = createCreditCardPaymentRequest();
        PaymentResponse response = createInsufficientFundsResponse();
        
        when(paymentProcessingUseCase.processPayment(any(PaymentRequest.class)))
            .thenReturn(response);
        
        // When & Then
        mockMvc.perform(post("/api/v1/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.status").value("FAILED"))
                .andExpect(jsonPath("$.data.failureReason").value("INSUFFICIENT_FUNDS"))
                .andExpect(jsonPath("$.data.retryable").value(false));
    }
    
    @Test
    @DisplayName("處理信用卡付款 - 網路錯誤可重試")
    void shouldHandleNetworkErrorWithRetry() throws Exception {
        // Given
        PaymentRequest request = createCreditCardPaymentRequest();
        PaymentResponse response = createNetworkErrorResponse();
        
        when(paymentProcessingUseCase.processPayment(any(PaymentRequest.class)))
            .thenReturn(response);
        
        // When & Then
        mockMvc.perform(post("/api/v1/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.status").value("FAILED"))
                .andExpect(jsonPath("$.data.failureReason").value("NETWORK_ERROR"))
                .andExpect(jsonPath("$.data.retryable").value(true));
    }
    
    @Test
    @DisplayName("查詢付款狀態 - 成功")
    void shouldGetPaymentStatusSuccessfully() throws Exception {
        // Given
        String transactionId = "TXN-123456";
        PaymentResponse response = createSuccessfulPaymentResponse();
        
        when(paymentProcessingUseCase.getPaymentStatus(transactionId))
            .thenReturn(response);
        
        // When & Then
        mockMvc.perform(get("/api/v1/payments/{transactionId}", transactionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.transactionId").value(response.getTransactionId()))
                .andExpect(jsonPath("$.data.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.gatewayTransactionId").value("GTW-123456"));
    }
    
    @Test
    @DisplayName("取消付款 - 成功")
    void shouldCancelPaymentSuccessfully() throws Exception {
        // Given
        String transactionId = "TXN-123456";
        String reason = "Customer requested cancellation";
        PaymentResponse response = createCancelledPaymentResponse();
        
        when(paymentProcessingUseCase.cancelPayment(eq(transactionId), eq(reason)))
            .thenReturn(response);
        
        // When & Then
        mockMvc.perform(post("/api/v1/payments/{transactionId}/cancel", transactionId)
                .param("reason", reason))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.transactionId").value(response.getTransactionId()))
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }
    
    @Test
    @DisplayName("處理退款 - 成功")
    void shouldProcessRefundSuccessfully() throws Exception {
        // Given
        RefundRequest request = createRefundRequest();
        RefundResponse refundResponse = RefundResponse.success(
            "REFUND-123",
            "TXN-123456",
            "ORDER-123",
            "CUSTOMER-456",
            new BigDecimal("500.00"),
            new BigDecimal("10.00"),
            PaymentMethod.CREDIT_CARD,
            "REFUND-GTW-456",
            "Customer requested refund",
            true
        );
        
        when(paymentProcessingUseCase.processRefund(any(RefundRequest.class)))
            .thenReturn(refundResponse);
        
        // When & Then
        mockMvc.perform(post("/api/v1/payments/refunds")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }
    
    @Test
    @DisplayName("重試付款 - 成功")
    void shouldRetryPaymentSuccessfully() throws Exception {
        // Given
        String transactionId = "TXN-123456";
        PaymentResponse response = createSuccessfulPaymentResponse();
        
        when(paymentProcessingUseCase.retryPayment(transactionId))
            .thenReturn(response);
        
        // When & Then
        mockMvc.perform(post("/api/v1/payments/{transactionId}/retry", transactionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.transactionId").value(response.getTransactionId()))
                .andExpect(jsonPath("$.data.status").value("SUCCESS"));
    }
    
    @Test
    @DisplayName("驗證請求參數 - 缺少必填欄位")
    void shouldValidateRequiredFields() throws Exception {
        // Given
        PaymentRequest invalidRequest = new PaymentRequest();
        // Missing required fields
        
        // When & Then
        mockMvc.perform(post("/api/v1/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("驗證請求參數 - 無效金額")
    void shouldValidateInvalidAmount() throws Exception {
        // Given
        PaymentRequest request = createCreditCardPaymentRequest();
        request.setAmount(BigDecimal.ZERO); // Invalid amount
        
        // When & Then
        mockMvc.perform(post("/api/v1/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("驗證請求參數 - 無效信用卡號")
    void shouldValidateInvalidCardNumber() throws Exception {
        // Given
        PaymentRequest request = createCreditCardPaymentRequest();
        request.setCardNumber("1234"); // Invalid card number
        
        // When & Then
        mockMvc.perform(post("/api/v1/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("處理內部服務錯誤")
    void shouldHandleInternalServiceError() throws Exception {
        // Given
        PaymentRequest request = createCreditCardPaymentRequest();
        
        when(paymentProcessingUseCase.processPayment(any(PaymentRequest.class)))
            .thenThrow(new RuntimeException("Internal service error"));
        
        // When & Then
        mockMvc.perform(post("/api/v1/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
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
    
    private PaymentResponse createSuccessfulPaymentResponse() {
        PaymentResponse response = new PaymentResponse(
            "TXN-123456",
            "ORDER-123",
            "CUSTOMER-456",
            new BigDecimal("1000.00"),
            PaymentMethod.CREDIT_CARD,
            PaymentStatus.SUCCESS
        );
        response.setGatewayTransactionId("GTW-123456");
        response.setDescription("Payment successful");
        response.setRetryable(false);
        return response;
    }
    
    private PaymentResponse createInsufficientFundsResponse() {
        PaymentResponse response = PaymentResponse.failure(
            null,
            "ORDER-123",
            "CUSTOMER-456",
            new BigDecimal("1000.00"),
            PaymentMethod.CREDIT_CARD,
            "Card has insufficient funds",
            false
        );
        response.setFailureReason("INSUFFICIENT_FUNDS");
        return response;
    }
    
    private PaymentResponse createNetworkErrorResponse() {
        PaymentResponse response = PaymentResponse.failure(
            null,
            "ORDER-123",
            "CUSTOMER-456",
            new BigDecimal("1000.00"),
            PaymentMethod.CREDIT_CARD,
            "Network connection failed",
            true
        );
        response.setFailureReason("NETWORK_ERROR");
        return response;
    }
    
    private PaymentResponse createCancelledPaymentResponse() {
        PaymentResponse response = new PaymentResponse(
            "TXN-123456",
            "ORDER-123",
            "CUSTOMER-456",
            new BigDecimal("1000.00"),
            PaymentMethod.CREDIT_CARD,
            PaymentStatus.CANCELLED
        );
        response.setDescription("Payment cancelled by customer");
        return response;
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
}