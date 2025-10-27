package com.ecommerce.payment.domain.model;

import com.ecommerce.common.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.YearMonth;

import static org.assertj.core.api.Assertions.*;

/**
 * PaymentTransaction 領域模型測試
 * 測試付款交易實體的業務邏輯和狀態轉換
 */
@DisplayName("付款交易實體測試")
class PaymentTransactionTest {

    @Test
    @DisplayName("建立信用卡付款交易")
    void shouldCreateCreditCardPaymentTransaction() {
        // Given
        String orderId = "ORDER-123";
        String customerId = "CUSTOMER-456";
        BigDecimal amount = new BigDecimal("1000.00");
        CreditCard creditCard = CreditCard.create("4111111111111111", "John Doe", 
                                                 YearMonth.now().plusYears(2), "123");
        String description = "Test payment";
        
        // When
        PaymentTransaction transaction = PaymentTransaction.createCreditCardPayment(
            orderId, customerId, amount, creditCard, description
        );
        
        // Then
        assertThat(transaction).isNotNull();
        assertThat(transaction.getOrderId()).isEqualTo(orderId);
        assertThat(transaction.getCustomerId()).isEqualTo(customerId);
        assertThat(transaction.getAmount()).isEqualTo(amount);
        assertThat(transaction.getPaymentMethod()).isEqualTo(PaymentMethod.CREDIT_CARD);
        assertThat(transaction.getCreditCard()).isEqualTo(creditCard);
        assertThat(transaction.getDescription()).isEqualTo(description);
        assertThat(transaction.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(transaction.getRefundedAmount()).isEqualTo(BigDecimal.ZERO);
        assertThat(transaction.getTransactionId()).isNotNull();
        assertThat(transaction.getMerchantReference()).isNotNull();
    }
    
    @Test
    @DisplayName("建立一般付款交易")
    void shouldCreateGeneralPaymentTransaction() {
        // Given
        String orderId = "ORDER-123";
        String customerId = "CUSTOMER-456";
        BigDecimal amount = new BigDecimal("1000.00");
        PaymentMethod paymentMethod = PaymentMethod.BANK_TRANSFER;
        String description = "Bank transfer payment";
        
        // When
        PaymentTransaction transaction = PaymentTransaction.create(
            orderId, customerId, amount, paymentMethod, description
        );
        
        // Then
        assertThat(transaction).isNotNull();
        assertThat(transaction.getOrderId()).isEqualTo(orderId);
        assertThat(transaction.getCustomerId()).isEqualTo(customerId);
        assertThat(transaction.getAmount()).isEqualTo(amount);
        assertThat(transaction.getPaymentMethod()).isEqualTo(paymentMethod);
        assertThat(transaction.getCreditCard()).isNull();
        assertThat(transaction.getDescription()).isEqualTo(description);
        assertThat(transaction.getStatus()).isEqualTo(PaymentStatus.PENDING);
    }
    
    @Test
    @DisplayName("付款交易狀態轉換 - 從待處理到處理中")
    void shouldTransitionFromPendingToProcessing() {
        // Given
        PaymentTransaction transaction = createTestTransaction();
        
        // When
        transaction.startProcessing();
        
        // Then
        assertThat(transaction.getStatus()).isEqualTo(PaymentStatus.PROCESSING);
        assertThat(transaction.isProcessing()).isTrue();
    }
    
    @Test
    @DisplayName("付款交易狀態轉換 - 從處理中到成功")
    void shouldTransitionFromProcessingToSuccess() {
        // Given
        PaymentTransaction transaction = createTestTransaction();
        transaction.startProcessing();
        
        // When
        transaction.markAsSuccess("GTW-123456", "Payment successful");
        
        // Then
        assertThat(transaction.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(transaction.isSuccessful()).isTrue();
        assertThat(transaction.getGatewayTransactionId()).isEqualTo("GTW-123456");
        assertThat(transaction.getGatewayResponse()).isEqualTo("Payment successful");
        assertThat(transaction.getProcessedAt()).isNotNull();
    }
    
    @Test
    @DisplayName("付款交易狀態轉換 - 從處理中到失敗")
    void shouldTransitionFromProcessingToFailed() {
        // Given
        PaymentTransaction transaction = createTestTransaction();
        transaction.startProcessing();
        
        // When
        transaction.markAsFailed("INSUFFICIENT_FUNDS", "Card has insufficient funds");
        
        // Then
        assertThat(transaction.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(transaction.isFailed()).isTrue();
        assertThat(transaction.getFailureReason()).isEqualTo("INSUFFICIENT_FUNDS");
        assertThat(transaction.getGatewayResponse()).isEqualTo("Card has insufficient funds");
        assertThat(transaction.getProcessedAt()).isNotNull();
    }
    
    @Test
    @DisplayName("取消待處理的付款交易")
    void shouldCancelPendingTransaction() {
        // Given
        PaymentTransaction transaction = createTestTransaction();
        String reason = "Customer requested cancellation";
        
        // When
        transaction.cancel(reason);
        
        // Then
        assertThat(transaction.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
        assertThat(transaction.isCancelled()).isTrue();
        assertThat(transaction.getFailureReason()).isEqualTo(reason);
    }
    
    @Test
    @DisplayName("無法取消已成功的付款交易")
    void shouldNotCancelSuccessfulTransaction() {
        // Given
        PaymentTransaction transaction = createTestTransaction();
        transaction.startProcessing();
        transaction.markAsSuccess("GTW-123456", "Payment successful");
        
        // When & Then
        assertThatThrownBy(() -> transaction.cancel("Customer requested cancellation"))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Cannot cancel payment in status: SUCCESS");
    }
    
    @Test
    @DisplayName("處理全額退款")
    void shouldProcessFullRefund() {
        // Given
        PaymentTransaction transaction = createSuccessfulTransaction();
        BigDecimal refundAmount = transaction.getAmount();
        String reason = "Customer requested full refund";
        
        // When
        PaymentTransaction refundTransaction = transaction.refund(refundAmount, reason);
        
        // Then
        assertThat(refundTransaction).isNotNull();
        assertThat(refundTransaction.getAmount()).isEqualTo(refundAmount.negate());
        assertThat(refundTransaction.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(refundTransaction.getDescription()).contains("Refund: " + reason);
        assertThat(refundTransaction.isRefund()).isTrue();
        
        // Original transaction should be marked as refunded
        assertThat(transaction.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        assertThat(transaction.isRefunded()).isTrue();
        assertThat(transaction.getRefundedAmount()).isEqualTo(refundAmount);
    }
    
    @Test
    @DisplayName("處理部分退款")
    void shouldProcessPartialRefund() {
        // Given
        PaymentTransaction transaction = createSuccessfulTransaction();
        BigDecimal refundAmount = new BigDecimal("500.00"); // Half of original amount
        String reason = "Partial refund requested";
        
        // When
        PaymentTransaction refundTransaction = transaction.refund(refundAmount, reason);
        
        // Then
        assertThat(refundTransaction).isNotNull();
        assertThat(refundTransaction.getAmount()).isEqualTo(refundAmount.negate());
        assertThat(refundTransaction.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        
        // Original transaction should be marked as partially refunded
        assertThat(transaction.getStatus()).isEqualTo(PaymentStatus.PARTIAL_REFUNDED);
        assertThat(transaction.isPartiallyRefunded()).isTrue();
        assertThat(transaction.getRefundedAmount()).isEqualTo(refundAmount);
        assertThat(transaction.getAvailableRefundAmount()).isEqualTo(new BigDecimal("500.00"));
    }
    
    @Test
    @DisplayName("無法退款超過可用金額")
    void shouldNotRefundMoreThanAvailableAmount() {
        // Given
        PaymentTransaction transaction = createSuccessfulTransaction();
        BigDecimal excessiveRefundAmount = transaction.getAmount().add(new BigDecimal("100.00"));
        
        // When & Then
        assertThatThrownBy(() -> transaction.refund(excessiveRefundAmount, "Excessive refund"))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Refund amount exceeds available refund amount");
    }
    
    @Test
    @DisplayName("無法退款失敗的交易")
    void shouldNotRefundFailedTransaction() {
        // Given
        PaymentTransaction transaction = createTestTransaction();
        transaction.startProcessing();
        transaction.markAsFailed("INSUFFICIENT_FUNDS", "Card has insufficient funds");
        
        // When & Then
        assertThatThrownBy(() -> transaction.refund(new BigDecimal("100.00"), "Refund failed transaction"))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Cannot refund payment in status: FAILED");
    }
    
    @Test
    @DisplayName("檢查交易是否可以退款")
    void shouldCheckIfTransactionCanBeRefunded() {
        // Given
        PaymentTransaction successfulTransaction = createSuccessfulTransaction();
        PaymentTransaction failedTransaction = createTestTransaction();
        failedTransaction.startProcessing();
        failedTransaction.markAsFailed("CARD_DECLINED", "Card was declined");
        
        // When & Then
        assertThat(successfulTransaction.canBeRefunded()).isTrue();
        assertThat(failedTransaction.canBeRefunded()).isFalse();
    }
    
    @Test
    @DisplayName("檢查交易是否可以取消")
    void shouldCheckIfTransactionCanBeCancelled() {
        // Given
        PaymentTransaction pendingTransaction = createTestTransaction();
        PaymentTransaction processingTransaction = createTestTransaction();
        processingTransaction.startProcessing();
        PaymentTransaction successfulTransaction = createSuccessfulTransaction();
        
        // When & Then
        assertThat(pendingTransaction.canBeCancelled()).isTrue();
        assertThat(processingTransaction.canBeCancelled()).isTrue();
        assertThat(successfulTransaction.canBeCancelled()).isFalse();
    }
    
    @Test
    @DisplayName("驗證必填欄位 - 訂單ID")
    void shouldValidateRequiredOrderId() {
        // When & Then
        assertThatThrownBy(() -> PaymentTransaction.create(
            null, "CUSTOMER-456", new BigDecimal("1000.00"), PaymentMethod.CREDIT_CARD, "Test"
        )).isInstanceOf(ValidationException.class)
          .hasMessageContaining("Order ID is required");
        
        assertThatThrownBy(() -> PaymentTransaction.create(
            "", "CUSTOMER-456", new BigDecimal("1000.00"), PaymentMethod.CREDIT_CARD, "Test"
        )).isInstanceOf(ValidationException.class)
          .hasMessageContaining("Order ID is required");
    }
    
    @Test
    @DisplayName("驗證必填欄位 - 客戶ID")
    void shouldValidateRequiredCustomerId() {
        // When & Then
        assertThatThrownBy(() -> PaymentTransaction.create(
            "ORDER-123", null, new BigDecimal("1000.00"), PaymentMethod.CREDIT_CARD, "Test"
        )).isInstanceOf(ValidationException.class)
          .hasMessageContaining("Customer ID is required");
        
        assertThatThrownBy(() -> PaymentTransaction.create(
            "ORDER-123", "", new BigDecimal("1000.00"), PaymentMethod.CREDIT_CARD, "Test"
        )).isInstanceOf(ValidationException.class)
          .hasMessageContaining("Customer ID is required");
    }
    
    @Test
    @DisplayName("驗證金額必須大於零")
    void shouldValidateAmountGreaterThanZero() {
        // When & Then
        assertThatThrownBy(() -> PaymentTransaction.create(
            "ORDER-123", "CUSTOMER-456", BigDecimal.ZERO, PaymentMethod.CREDIT_CARD, "Test"
        )).isInstanceOf(ValidationException.class)
          .hasMessageContaining("Amount must be greater than zero");
        
        assertThatThrownBy(() -> PaymentTransaction.create(
            "ORDER-123", "CUSTOMER-456", new BigDecimal("-100.00"), PaymentMethod.CREDIT_CARD, "Test"
        )).isInstanceOf(ValidationException.class)
          .hasMessageContaining("Amount must be greater than zero");
    }
    
    @Test
    @DisplayName("驗證金額小數位數")
    void shouldValidateAmountDecimalPlaces() {
        // When & Then
        assertThatThrownBy(() -> PaymentTransaction.create(
            "ORDER-123", "CUSTOMER-456", new BigDecimal("100.123"), PaymentMethod.CREDIT_CARD, "Test"
        )).isInstanceOf(ValidationException.class)
          .hasMessageContaining("Amount cannot have more than 2 decimal places");
    }
    
    @Test
    @DisplayName("驗證付款方式必填")
    void shouldValidateRequiredPaymentMethod() {
        // When & Then
        assertThatThrownBy(() -> PaymentTransaction.create(
            "ORDER-123", "CUSTOMER-456", new BigDecimal("1000.00"), null, "Test"
        )).isInstanceOf(ValidationException.class)
          .hasMessageContaining("Payment method is required");
    }
    
    @Test
    @DisplayName("驗證信用卡付款必須提供信用卡資訊")
    void shouldValidateCreditCardRequiredForCreditCardPayment() {
        // When & Then
        assertThatThrownBy(() -> PaymentTransaction.createCreditCardPayment(
            "ORDER-123", "CUSTOMER-456", new BigDecimal("1000.00"), null, "Test"
        )).isInstanceOf(ValidationException.class)
          .hasMessageContaining("Credit card information is required for credit card payments");
    }
    
    @Test
    @DisplayName("驗證描述長度限制")
    void shouldValidateDescriptionLength() {
        // Given
        String longDescription = "A".repeat(501); // Exceeds 500 character limit
        
        // When & Then
        assertThatThrownBy(() -> PaymentTransaction.create(
            "ORDER-123", "CUSTOMER-456", new BigDecimal("1000.00"), PaymentMethod.CREDIT_CARD, longDescription
        )).isInstanceOf(ValidationException.class)
          .hasMessageContaining("Description cannot exceed 500 characters");
    }
    
    @Test
    @DisplayName("交易相等性比較")
    void shouldCompareTransactionEquality() {
        // Given
        PaymentTransaction transaction1 = createTestTransaction();
        PaymentTransaction transaction2 = createTestTransaction();
        
        // When & Then
        assertThat(transaction1).isNotEqualTo(transaction2); // Different transaction IDs
        assertThat(transaction1).isEqualTo(transaction1); // Same instance
        assertThat(transaction1.hashCode()).isNotEqualTo(transaction2.hashCode());
    }
    
    // Helper methods for creating test data
    private PaymentTransaction createTestTransaction() {
        return PaymentTransaction.createCreditCardPayment(
            "ORDER-123",
            "CUSTOMER-456",
            new BigDecimal("1000.00"),
            CreditCard.create("4111111111111111", "John Doe", YearMonth.now().plusYears(2), "123"),
            "Test payment"
        );
    }
    
    private PaymentTransaction createSuccessfulTransaction() {
        PaymentTransaction transaction = createTestTransaction();
        transaction.startProcessing();
        transaction.markAsSuccess("GTW-123456", "Payment successful");
        return transaction;
    }
}