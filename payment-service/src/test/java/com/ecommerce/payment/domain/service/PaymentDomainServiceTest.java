package com.ecommerce.payment.domain.service;

import com.ecommerce.common.exception.ValidationException;
import com.ecommerce.payment.domain.exception.InvalidPaymentStateException;
import com.ecommerce.payment.domain.model.*;
import com.ecommerce.payment.domain.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * PaymentDomainService 領域服務測試
 * 測試付款領域服務的業務規則和驗證邏輯
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("付款領域服務測試")
class PaymentDomainServiceTest {

    @Mock
    private PaymentRepository paymentRepository;
    
    private PaymentDomainService paymentDomainService;
    
    @BeforeEach
    void setUp() {
        paymentDomainService = new PaymentDomainService(paymentRepository);
    }
    
    @Test
    @DisplayName("驗證付款請求 - 成功案例")
    void shouldValidatePaymentRequestSuccessfully() {
        // Given
        String orderId = "ORDER-123";
        String customerId = "CUSTOMER-456";
        BigDecimal amount = new BigDecimal("1000.00");
        PaymentMethod paymentMethod = PaymentMethod.CREDIT_CARD;
        CreditCard creditCard = CreditCard.create("4111111111111111", "John Doe", 
                                                 YearMonth.now().plusYears(2), "123");
        
        when(paymentRepository.hasSuccessfulPaymentForOrder(orderId)).thenReturn(false);
        when(paymentRepository.findByCustomerIdAndStatus(eq(customerId), eq(PaymentStatus.SUCCESS)))
            .thenReturn(Collections.emptyList());
        
        // When & Then - Should not throw exception
        assertThatCode(() -> paymentDomainService.validatePaymentRequest(
            orderId, customerId, amount, paymentMethod, creditCard
        )).doesNotThrowAnyException();
        
        verify(paymentRepository).hasSuccessfulPaymentForOrder(orderId);
    }
    
    @Test
    @DisplayName("驗證付款請求 - 訂單已有成功付款")
    void shouldRejectPaymentForOrderWithExistingSuccessfulPayment() {
        // Given
        String orderId = "ORDER-123";
        String customerId = "CUSTOMER-456";
        BigDecimal amount = new BigDecimal("1000.00");
        PaymentMethod paymentMethod = PaymentMethod.CREDIT_CARD;
        CreditCard creditCard = CreditCard.create("4111111111111111", "John Doe", 
                                                 YearMonth.now().plusYears(2), "123");
        
        when(paymentRepository.hasSuccessfulPaymentForOrder(orderId)).thenReturn(true);
        
        // When & Then
        assertThatThrownBy(() -> paymentDomainService.validatePaymentRequest(
            orderId, customerId, amount, paymentMethod, creditCard
        )).isInstanceOf(InvalidPaymentStateException.class)
          .hasMessageContaining("Order already has a successful payment: " + orderId);
    }
    
    @Test
    @DisplayName("驗證付款請求 - 金額超過單筆限額")
    void shouldRejectPaymentExceedingSinglePaymentLimit() {
        // Given
        String orderId = "ORDER-123";
        String customerId = "CUSTOMER-456";
        BigDecimal amount = new BigDecimal("150000.00"); // Exceeds 100,000 limit
        PaymentMethod paymentMethod = PaymentMethod.CREDIT_CARD;
        CreditCard creditCard = CreditCard.create("4111111111111111", "John Doe", 
                                                 YearMonth.now().plusYears(2), "123");
        
        when(paymentRepository.hasSuccessfulPaymentForOrder(orderId)).thenReturn(false);
        
        // When & Then
        assertThatThrownBy(() -> paymentDomainService.validatePaymentRequest(
            orderId, customerId, amount, paymentMethod, creditCard
        )).isInstanceOf(InvalidPaymentStateException.class)
          .hasMessageContaining("Payment amount exceeds maximum limit");
    }
    
    @Test
    @DisplayName("驗證付款請求 - 超過每日付款限額")
    void shouldRejectPaymentExceedingDailyLimit() {
        // Given
        String orderId = "ORDER-123";
        String customerId = "CUSTOMER-456";
        BigDecimal amount = new BigDecimal("100000.00");
        PaymentMethod paymentMethod = PaymentMethod.CREDIT_CARD;
        CreditCard creditCard = CreditCard.create("4111111111111111", "John Doe", 
                                                 YearMonth.now().plusYears(2), "123");
        
        // Mock existing successful payments for today totaling 450,000
        PaymentTransaction existingPayment = createMockTransaction("450000.00");
        when(paymentRepository.hasSuccessfulPaymentForOrder(orderId)).thenReturn(false);
        when(paymentRepository.findByCustomerIdAndStatus(eq(customerId), eq(PaymentStatus.SUCCESS)))
            .thenReturn(Arrays.asList(existingPayment));
        
        // When & Then
        assertThatThrownBy(() -> paymentDomainService.validatePaymentRequest(
            orderId, customerId, amount, paymentMethod, creditCard
        )).isInstanceOf(InvalidPaymentStateException.class)
          .hasMessageContaining("Daily payment limit exceeded");
    }
    
    @Test
    @DisplayName("驗證付款請求 - 過期信用卡")
    void shouldRejectExpiredCreditCard() {
        // Given
        String orderId = "ORDER-123";
        String customerId = "CUSTOMER-456";
        BigDecimal amount = new BigDecimal("1000.00");
        PaymentMethod paymentMethod = PaymentMethod.CREDIT_CARD;
        
        // When & Then - Creating expired card should throw ValidationException
        assertThatThrownBy(() -> {
            CreditCard expiredCard = CreditCard.create("4111111111111111", "John Doe", 
                                                      YearMonth.now().minusMonths(1), "123"); // Expired
            paymentDomainService.validatePaymentRequest(
                orderId, customerId, amount, paymentMethod, expiredCard
            );
        }).isInstanceOf(ValidationException.class)
          .hasMessageContaining("Card has expired");
    }
    
    @Test
    @DisplayName("檢查是否可以重試付款 - 失敗次數未達上限")
    void shouldAllowRetryWhenFailureCountBelowLimit() {
        // Given
        String orderId = "ORDER-123";
        PaymentTransaction failedTransaction1 = createFailedTransaction();
        PaymentTransaction failedTransaction2 = createFailedTransaction();
        
        when(paymentRepository.findAllByOrderId(orderId))
            .thenReturn(Arrays.asList(failedTransaction1, failedTransaction2));
        
        // When
        boolean canRetry = paymentDomainService.canRetryPayment(orderId);
        
        // Then
        assertThat(canRetry).isTrue(); // 2 failures < 3 limit
    }
    
    @Test
    @DisplayName("檢查是否可以重試付款 - 失敗次數達到上限")
    void shouldNotAllowRetryWhenFailureCountReachesLimit() {
        // Given
        String orderId = "ORDER-123";
        PaymentTransaction failedTransaction1 = createFailedTransaction();
        PaymentTransaction failedTransaction2 = createFailedTransaction();
        PaymentTransaction failedTransaction3 = createFailedTransaction();
        
        when(paymentRepository.findAllByOrderId(orderId))
            .thenReturn(Arrays.asList(failedTransaction1, failedTransaction2, failedTransaction3));
        
        // When
        boolean canRetry = paymentDomainService.canRetryPayment(orderId);
        
        // Then
        assertThat(canRetry).isFalse(); // 3 failures = 3 limit
    }
    
    @Test
    @DisplayName("計算信用卡退款手續費")
    void shouldCalculateCreditCardRefundFee() {
        // Given
        PaymentTransaction transaction = createCreditCardTransaction();
        BigDecimal refundAmount = new BigDecimal("1000.00");
        
        // When
        BigDecimal fee = paymentDomainService.calculateRefundFee(transaction, refundAmount);
        
        // Then
        assertThat(fee).isEqualByComparingTo(new BigDecimal("10")); // 1% of 1000, min 10
    }
    
    @Test
    @DisplayName("計算信用卡退款手續費 - 最低手續費")
    void shouldApplyMinimumRefundFeeForCreditCard() {
        // Given
        PaymentTransaction transaction = createCreditCardTransaction();
        BigDecimal refundAmount = new BigDecimal("500.00"); // 1% = 5, but min is 10
        
        // When
        BigDecimal fee = paymentDomainService.calculateRefundFee(transaction, refundAmount);
        
        // Then
        assertThat(fee).isEqualByComparingTo(new BigDecimal("10")); // Minimum fee applied
    }
    
    @Test
    @DisplayName("計算信用卡退款手續費 - 最高手續費")
    void shouldApplyMaximumRefundFeeForCreditCard() {
        // Given
        PaymentTransaction transaction = createCreditCardTransaction();
        BigDecimal refundAmount = new BigDecimal("20000.00"); // 1% = 200, but max is 100
        
        // When
        BigDecimal fee = paymentDomainService.calculateRefundFee(transaction, refundAmount);
        
        // Then
        assertThat(fee).isEqualByComparingTo(new BigDecimal("100")); // Maximum fee applied
    }
    
    @Test
    @DisplayName("計算銀行轉帳退款手續費 - 免手續費")
    void shouldNotChargeFeeForBankTransferRefund() {
        // Given
        PaymentTransaction transaction = createBankTransferTransaction();
        BigDecimal refundAmount = new BigDecimal("1000.00");
        
        // When
        BigDecimal fee = paymentDomainService.calculateRefundFee(transaction, refundAmount);
        
        // Then
        assertThat(fee).isEqualTo(BigDecimal.ZERO);
    }
    
    @Test
    @DisplayName("檢查付款是否超時")
    void shouldDetectPaymentTimeout() {
        // Given
        PaymentTransaction transaction = createProcessingTransaction();
        // Mock creation time to be 31 minutes ago (beyond 30-minute timeout)
        transaction.setCreatedAt(LocalDateTime.now().minusMinutes(31));
        
        // When
        boolean isTimeout = paymentDomainService.isPaymentTimeout(transaction);
        
        // Then
        assertThat(isTimeout).isTrue();
    }
    
    @Test
    @DisplayName("檢查付款未超時")
    void shouldNotDetectTimeoutForRecentPayment() {
        // Given
        PaymentTransaction transaction = createProcessingTransaction();
        // Mock creation time to be 15 minutes ago (within 30-minute timeout)
        transaction.setCreatedAt(LocalDateTime.now().minusMinutes(15));
        
        // When
        boolean isTimeout = paymentDomainService.isPaymentTimeout(transaction);
        
        // Then
        assertThat(isTimeout).isFalse();
    }
    
    @Test
    @DisplayName("自動取消超時交易")
    void shouldCancelTimeoutTransactions() {
        // Given
        PaymentTransaction timeoutTransaction = createProcessingTransaction();
        timeoutTransaction.setCreatedAt(LocalDateTime.now().minusMinutes(31));
        
        when(paymentRepository.findTimeoutTransactions(any(LocalDateTime.class)))
            .thenReturn(Arrays.asList(timeoutTransaction));
        when(paymentRepository.save(any(PaymentTransaction.class)))
            .thenReturn(timeoutTransaction);
        
        // When
        List<PaymentTransaction> cancelledTransactions = paymentDomainService.cancelTimeoutTransactions();
        
        // Then
        assertThat(cancelledTransactions).hasSize(1);
        assertThat(cancelledTransactions.get(0)).isEqualTo(timeoutTransaction);
        verify(paymentRepository).save(timeoutTransaction);
    }
    
    @Test
    @DisplayName("驗證客戶付款權限 - 成功")
    void shouldValidateCustomerPaymentAccessSuccessfully() {
        // Given
        String transactionId = "TXN-123456";
        String customerId = "CUSTOMER-456";
        PaymentTransaction transaction = createMockTransactionWithCustomer(customerId);
        
        when(paymentRepository.findById(transactionId))
            .thenReturn(java.util.Optional.of(transaction));
        
        // When & Then - Should not throw exception
        assertThatCode(() -> paymentDomainService.validateCustomerPaymentAccess(transactionId, customerId))
            .doesNotThrowAnyException();
    }
    
    @Test
    @DisplayName("驗證客戶付款權限 - 客戶不匹配")
    void shouldRejectAccessForWrongCustomer() {
        // Given
        String transactionId = "TXN-123456";
        String customerId = "CUSTOMER-456";
        String wrongCustomerId = "CUSTOMER-789";
        PaymentTransaction transaction = createMockTransactionWithCustomer(customerId);
        
        when(paymentRepository.findById(transactionId))
            .thenReturn(java.util.Optional.of(transaction));
        
        // When & Then
        assertThatThrownBy(() -> paymentDomainService.validateCustomerPaymentAccess(transactionId, wrongCustomerId))
            .isInstanceOf(InvalidPaymentStateException.class)
            .hasMessageContaining("Customer does not have access to this payment transaction");
    }
    
    @Test
    @DisplayName("獲取客戶付款統計資訊")
    void shouldGetCustomerPaymentStats() {
        // Given
        String customerId = "CUSTOMER-456";
        PaymentTransaction successfulTransaction1 = createSuccessfulTransaction("1000.00");
        PaymentTransaction successfulTransaction2 = createSuccessfulTransaction("2000.00");
        PaymentTransaction failedTransaction = createFailedTransaction();
        
        when(paymentRepository.findByCustomerId(customerId))
            .thenReturn(Arrays.asList(successfulTransaction1, successfulTransaction2, failedTransaction));
        
        // When
        PaymentDomainService.CustomerPaymentStats stats = paymentDomainService.getCustomerPaymentStats(customerId);
        
        // Then
        assertThat(stats.getCustomerId()).isEqualTo(customerId);
        assertThat(stats.getTotalTransactions()).isEqualTo(3);
        assertThat(stats.getSuccessfulTransactions()).isEqualTo(2);
        assertThat(stats.getFailedTransactions()).isEqualTo(1);
        assertThat(stats.getTotalAmount()).isEqualTo(new BigDecimal("3000.00"));
        assertThat(stats.getSuccessRate()).isEqualTo(2.0 / 3.0);
        assertThat(stats.getFailureRate()).isEqualTo(1.0 / 3.0);
    }
    
    // Helper methods for creating test data
    private PaymentTransaction createMockTransaction(String amount) {
        PaymentTransaction transaction = PaymentTransaction.create(
            "ORDER-123",
            "CUSTOMER-456",
            new BigDecimal(amount),
            PaymentMethod.CREDIT_CARD,
            "Test payment"
        );
        transaction.startProcessing();
        transaction.markAsSuccess("GTW-123456", "Payment successful");
        transaction.setCreatedAt(LocalDateTime.now()); // Set creation time for daily limit calculation
        return transaction;
    }
    
    private PaymentTransaction createMockTransactionWithCustomer(String customerId) {
        return PaymentTransaction.create(
            "ORDER-123",
            customerId,
            new BigDecimal("1000.00"),
            PaymentMethod.CREDIT_CARD,
            "Test payment"
        );
    }
    
    private PaymentTransaction createFailedTransaction() {
        PaymentTransaction transaction = PaymentTransaction.create(
            "ORDER-123",
            "CUSTOMER-456",
            new BigDecimal("1000.00"),
            PaymentMethod.CREDIT_CARD,
            "Test payment"
        );
        transaction.startProcessing();
        transaction.markAsFailed("INSUFFICIENT_FUNDS", "Card has insufficient funds");
        return transaction;
    }
    
    private PaymentTransaction createSuccessfulTransaction(String amount) {
        PaymentTransaction transaction = PaymentTransaction.create(
            "ORDER-123",
            "CUSTOMER-456",
            new BigDecimal(amount),
            PaymentMethod.CREDIT_CARD,
            "Test payment"
        );
        transaction.startProcessing();
        transaction.markAsSuccess("GTW-123456", "Payment successful");
        return transaction;
    }
    
    private PaymentTransaction createCreditCardTransaction() {
        return PaymentTransaction.createCreditCardPayment(
            "ORDER-123",
            "CUSTOMER-456",
            new BigDecimal("1000.00"),
            CreditCard.create("4111111111111111", "John Doe", YearMonth.now().plusYears(2), "123"),
            "Test payment"
        );
    }
    
    private PaymentTransaction createBankTransferTransaction() {
        return PaymentTransaction.create(
            "ORDER-123",
            "CUSTOMER-456",
            new BigDecimal("1000.00"),
            PaymentMethod.BANK_TRANSFER,
            "Test payment"
        );
    }
    
    private PaymentTransaction createProcessingTransaction() {
        PaymentTransaction transaction = PaymentTransaction.create(
            "ORDER-123",
            "CUSTOMER-456",
            new BigDecimal("1000.00"),
            PaymentMethod.CREDIT_CARD,
            "Test payment"
        );
        transaction.startProcessing();
        return transaction;
    }
}