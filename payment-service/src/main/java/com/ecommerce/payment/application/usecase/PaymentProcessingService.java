package com.ecommerce.payment.application.usecase;

import com.ecommerce.common.architecture.UseCase;
import com.ecommerce.payment.application.dto.*;
import com.ecommerce.payment.application.port.in.PaymentProcessingUseCase;
import com.ecommerce.payment.application.port.out.PaymentNotificationPort;
import com.ecommerce.payment.application.port.out.PaymentPersistencePort;
import com.ecommerce.payment.application.service.PaymentRetryService;
import com.ecommerce.payment.application.strategy.PaymentStrategy;
import com.ecommerce.payment.application.strategy.PaymentStrategyFactory;
import com.ecommerce.payment.domain.exception.PaymentNotFoundException;
import com.ecommerce.payment.domain.exception.PaymentProcessingException;
import com.ecommerce.payment.domain.model.CreditCard;
import com.ecommerce.payment.domain.model.PaymentFailureReason;
import com.ecommerce.payment.domain.model.PaymentTransaction;
import com.ecommerce.payment.domain.service.PaymentDomainService;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 付款處理服務實作
 * 實作付款相關的業務邏輯
 */
@UseCase
@Transactional
public class PaymentProcessingService implements PaymentProcessingUseCase {
    
    private final PaymentPersistencePort paymentPersistencePort;
    private final PaymentNotificationPort paymentNotificationPort;
    private final PaymentDomainService paymentDomainService;
    private final PaymentStrategyFactory strategyFactory;
    private final PaymentRetryService retryService;
    
    public PaymentProcessingService(PaymentPersistencePort paymentPersistencePort,
                                  PaymentNotificationPort paymentNotificationPort,
                                  PaymentDomainService paymentDomainService,
                                  PaymentStrategyFactory strategyFactory,
                                  PaymentRetryService retryService) {
        this.paymentPersistencePort = paymentPersistencePort;
        this.paymentNotificationPort = paymentNotificationPort;
        this.paymentDomainService = paymentDomainService;
        this.strategyFactory = strategyFactory;
        this.retryService = retryService;
    }
    
    @Override
    public PaymentResponse processPayment(PaymentRequest request) {
        try {
            // 建立付款交易
            PaymentTransaction transaction = createPaymentTransaction(request);
            
            // 驗證付款請求
            validatePaymentRequest(request, transaction);
            
            // 儲存初始交易
            transaction = paymentPersistencePort.save(transaction);
            
            // 開始處理付款
            transaction.startProcessing();
            transaction = paymentPersistencePort.save(transaction);
            
            // 執行付款處理
            GatewayPaymentResponse gatewayResponse = executePayment(request, transaction);
            
            // 更新交易狀態
            updateTransactionStatus(transaction, gatewayResponse);
            
            // 儲存最終交易狀態
            transaction = paymentPersistencePort.save(transaction);
            
            // 發送通知
            sendPaymentNotification(transaction, request);
            
            // 轉換為回應 DTO
            return convertToPaymentResponse(transaction);
            
        } catch (PaymentProcessingException e) {
            return handlePaymentProcessingException(e, request);
        } catch (Exception e) {
            return handleUnexpectedException(e, request);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentStatus(String transactionId) {
        PaymentTransaction transaction = paymentPersistencePort.findById(transactionId)
            .orElseThrow(() -> PaymentNotFoundException.byTransactionId(transactionId));
        
        return convertToPaymentResponse(transaction);
    }
    
    @Override
    public PaymentResponse cancelPayment(String transactionId, String reason) {
        PaymentTransaction transaction = paymentPersistencePort.findById(transactionId)
            .orElseThrow(() -> PaymentNotFoundException.byTransactionId(transactionId));
        
        // 取消付款
        transaction.cancel(reason);
        transaction = paymentPersistencePort.save(transaction);
        
        // 發送取消通知
        sendCancellationNotification(transaction, reason);
        
        return convertToPaymentResponse(transaction);
    }
    
    @Override
    public RefundResponse processRefund(RefundRequest request) {
        try {
            // 查找原始交易
            PaymentTransaction originalTransaction = paymentPersistencePort.findById(request.getTransactionId())
                .orElseThrow(() -> PaymentNotFoundException.byTransactionId(request.getTransactionId()));
            
            // 驗證退款請求
            validateRefundRequest(request, originalTransaction);
            
            // 計算退款金額
            BigDecimal refundAmount = determineRefundAmount(request, originalTransaction);
            
            // 計算退款手續費
            BigDecimal refundFee = paymentDomainService.calculateRefundFee(originalTransaction, refundAmount);
            
            // 建立退款交易
            PaymentTransaction refundTransaction = originalTransaction.refund(refundAmount, request.getReason());
            
            // 儲存退款交易
            refundTransaction = paymentPersistencePort.save(refundTransaction);
            originalTransaction = paymentPersistencePort.save(originalTransaction);
            
            // 執行閘道退款
            GatewayRefundResponse gatewayResponse = executeRefund(originalTransaction, refundTransaction, request);
            
            // 發送退款通知
            sendRefundNotification(refundTransaction, originalTransaction, gatewayResponse, request);
            
            // 轉換為回應 DTO
            return convertToRefundResponse(refundTransaction, originalTransaction, refundFee, gatewayResponse);
            
        } catch (PaymentProcessingException e) {
            return handleRefundProcessingException(e, request);
        } catch (Exception e) {
            return handleRefundUnexpectedException(e, request);
        }
    }
    
    @Override
    public PaymentResponse retryPayment(String transactionId) {
        PaymentTransaction transaction = paymentPersistencePort.findById(transactionId)
            .orElseThrow(() -> PaymentNotFoundException.byTransactionId(transactionId));
        
        // 檢查是否可以重試
        if (!retryService.canRetryPayment(transaction)) {
            throw new PaymentProcessingException(
                "Payment cannot be retried",
                PaymentFailureReason.LIMIT_EXCEEDED
            );
        }
        
        // 建立重試請求
        GatewayPaymentRequest gatewayRequest = createGatewayRequest(transaction);
        
        // 執行重試
        GatewayPaymentResponse gatewayResponse = retryService.retryPayment(transaction, gatewayRequest);
        
        // 更新交易狀態
        updateTransactionStatus(transaction, gatewayResponse);
        
        // 儲存交易
        transaction = paymentPersistencePort.save(transaction);
        
        return convertToPaymentResponse(transaction);
    }
    
    // Private helper methods
    private PaymentTransaction createPaymentTransaction(PaymentRequest request) {
        switch (request.getPaymentMethod()) {
            case CREDIT_CARD:
                CreditCard creditCard = CreditCard.create(
                    request.getCardNumber(),
                    request.getCardHolderName(),
                    request.getExpiryDate(),
                    request.getCvv()
                );
                return PaymentTransaction.createCreditCardPayment(
                    request.getOrderId(),
                    request.getCustomerId(),
                    request.getAmount(),
                    creditCard,
                    request.getDescription()
                );
            default:
                return PaymentTransaction.create(
                    request.getOrderId(),
                    request.getCustomerId(),
                    request.getAmount(),
                    request.getPaymentMethod(),
                    request.getDescription()
                );
        }
    }
    
    private void validatePaymentRequest(PaymentRequest request, PaymentTransaction transaction) {
        // 使用領域服務驗證
        paymentDomainService.validatePaymentRequest(
            request.getOrderId(),
            request.getCustomerId(),
            request.getAmount(),
            request.getPaymentMethod(),
            transaction.getCreditCard()
        );
    }
    
    private GatewayPaymentResponse executePayment(PaymentRequest request, PaymentTransaction transaction) {
        // 取得付款策略
        PaymentStrategy strategy = strategyFactory.getStrategy(request.getPaymentMethod());
        
        // 建立閘道請求
        GatewayPaymentRequest gatewayRequest = createGatewayRequest(request, transaction);
        
        // 執行付款
        return strategy.processPayment(gatewayRequest);
    }
    
    private GatewayPaymentRequest createGatewayRequest(PaymentRequest request, PaymentTransaction transaction) {
        switch (request.getPaymentMethod()) {
            case CREDIT_CARD:
                return GatewayPaymentRequest.createCreditCardRequest(
                    transaction.getTransactionId(),
                    transaction.getMerchantReference(),
                    request.getAmount(),
                    request.getDescription(),
                    request.getCardNumber(),
                    request.getCardHolderName(),
                    request.getExpiryDate(),
                    request.getCvv()
                );
            case BANK_TRANSFER:
                return GatewayPaymentRequest.createBankTransferRequest(
                    transaction.getTransactionId(),
                    transaction.getMerchantReference(),
                    request.getAmount(),
                    request.getDescription(),
                    request.getBankAccount(),
                    request.getBankCode()
                );
            default:
                throw new PaymentProcessingException(
                    "Unsupported payment method: " + request.getPaymentMethod(),
                    PaymentFailureReason.SYSTEM_ERROR
                );
        }
    }
    
    private GatewayPaymentRequest createGatewayRequest(PaymentTransaction transaction) {
        GatewayPaymentRequest request = new GatewayPaymentRequest(
            transaction.getTransactionId(),
            transaction.getMerchantReference(),
            transaction.getAmount(),
            transaction.getPaymentMethod(),
            transaction.getDescription()
        );
        
        if (transaction.getCreditCard() != null) {
            CreditCard card = transaction.getCreditCard();
            request.setCardHolderName(card.getCardHolderName());
            request.setExpiryDate(card.getExpiryDate());
            // 注意：實際實作中不應該儲存完整的卡號和CVV
        }
        
        return request;
    }
    
    private void updateTransactionStatus(PaymentTransaction transaction, GatewayPaymentResponse gatewayResponse) {
        if (gatewayResponse.isSuccessful()) {
            transaction.markAsSuccess(
                gatewayResponse.getGatewayTransactionId(),
                gatewayResponse.getResponseMessage()
            );
        } else {
            transaction.markAsFailed(
                gatewayResponse.getResponseCode(),
                gatewayResponse.getResponseMessage()
            );
        }
    }
    
    private void validateRefundRequest(RefundRequest request, PaymentTransaction originalTransaction) {
        if (!originalTransaction.canBeRefunded()) {
            throw new PaymentProcessingException(
                "Transaction cannot be refunded",
                PaymentFailureReason.INVALID_TRANSACTION_STATE
            );
        }
        
        if (request.isPartialRefund()) {
            BigDecimal availableAmount = originalTransaction.getAvailableRefundAmount();
            if (request.getRefundAmount().compareTo(availableAmount) > 0) {
                throw new PaymentProcessingException(
                    "Refund amount exceeds available amount",
                    PaymentFailureReason.INVALID_AMOUNT
                );
            }
        }
    }
    
    private BigDecimal determineRefundAmount(RefundRequest request, PaymentTransaction originalTransaction) {
        if (request.isPartialRefund()) {
            return request.getRefundAmount();
        } else {
            return originalTransaction.getAvailableRefundAmount();
        }
    }
    
    private GatewayRefundResponse executeRefund(PaymentTransaction originalTransaction,
                                              PaymentTransaction refundTransaction,
                                              RefundRequest request) {
        PaymentStrategy strategy = strategyFactory.getStrategy(originalTransaction.getPaymentMethod());
        
        GatewayRefundRequest gatewayRequest = GatewayRefundRequest.partialRefund(
            refundTransaction.getTransactionId(),
            originalTransaction.getGatewayTransactionId(),
            refundTransaction.getMerchantReference(),
            refundTransaction.getAmount().abs(),
            originalTransaction.getAmount(),
            request.getReason()
        );
        
        return strategy.processRefund(gatewayRequest);
    }
    
    private void sendPaymentNotification(PaymentTransaction transaction, PaymentRequest request) {
        try {
            PaymentNotification notification;
            
            if (transaction.isSuccessful()) {
                notification = PaymentNotification.paymentSuccess(
                    transaction.getTransactionId(),
                    transaction.getOrderId(),
                    transaction.getCustomerId(),
                    null, // 需要從客戶服務取得 email
                    transaction.getAmount(),
                    transaction.getPaymentMethod(),
                    transaction.getGatewayTransactionId(),
                    transaction.getCreditCard() != null ? transaction.getCreditCard().getMaskedCardNumber() : null
                );
                paymentNotificationPort.sendPaymentSuccessNotification(notification);
            } else {
                notification = PaymentNotification.paymentFailure(
                    transaction.getTransactionId(),
                    transaction.getOrderId(),
                    transaction.getCustomerId(),
                    null, // 需要從客戶服務取得 email
                    transaction.getAmount(),
                    transaction.getPaymentMethod(),
                    transaction.getFailureReason()
                );
                paymentNotificationPort.sendPaymentFailureNotification(notification);
            }
        } catch (Exception e) {
            // 通知失敗不應該影響付款處理
            System.err.println("Failed to send payment notification: " + e.getMessage());
        }
    }
    
    private void sendCancellationNotification(PaymentTransaction transaction, String reason) {
        try {
            PaymentNotification notification = PaymentNotification.paymentCancellation(
                transaction.getTransactionId(),
                transaction.getOrderId(),
                transaction.getCustomerId(),
                null, // 需要從客戶服務取得 email
                transaction.getAmount(),
                transaction.getPaymentMethod(),
                reason
            );
            paymentNotificationPort.sendPaymentCancellationNotification(notification);
        } catch (Exception e) {
            System.err.println("Failed to send cancellation notification: " + e.getMessage());
        }
    }
    
    private void sendRefundNotification(PaymentTransaction refundTransaction,
                                      PaymentTransaction originalTransaction,
                                      GatewayRefundResponse gatewayResponse,
                                      RefundRequest request) {
        try {
            PaymentNotification notification;
            
            if (gatewayResponse.isSuccessful()) {
                notification = PaymentNotification.refundSuccess(
                    refundTransaction.getTransactionId(),
                    originalTransaction.getTransactionId(),
                    originalTransaction.getOrderId(),
                    originalTransaction.getCustomerId(),
                    null, // 需要從客戶服務取得 email
                    refundTransaction.getAmount().abs(),
                    originalTransaction.getPaymentMethod(),
                    request.getReason()
                );
                paymentNotificationPort.sendRefundSuccessNotification(notification);
            } else {
                notification = PaymentNotification.refundFailure(
                    refundTransaction.getTransactionId(),
                    originalTransaction.getTransactionId(),
                    originalTransaction.getOrderId(),
                    originalTransaction.getCustomerId(),
                    null, // 需要從客戶服務取得 email
                    refundTransaction.getAmount().abs(),
                    originalTransaction.getPaymentMethod(),
                    gatewayResponse.getFailureReason(),
                    request.getReason()
                );
                paymentNotificationPort.sendRefundFailureNotification(notification);
            }
        } catch (Exception e) {
            System.err.println("Failed to send refund notification: " + e.getMessage());
        }
    }
    
    private PaymentResponse convertToPaymentResponse(PaymentTransaction transaction) {
        PaymentResponse response = new PaymentResponse(
            transaction.getTransactionId(),
            transaction.getOrderId(),
            transaction.getCustomerId(),
            transaction.getAmount(),
            transaction.getPaymentMethod(),
            transaction.getStatus()
        );
        
        response.setGatewayTransactionId(transaction.getGatewayTransactionId());
        response.setFailureReason(transaction.getFailureReason());
        response.setDescription(transaction.getDescription());
        response.setProcessedAt(transaction.getProcessedAt());
        response.setRetryable(retryService.isRetryableFailure(transaction.getFailureReason()));
        
        if (transaction.getCreditCard() != null) {
            response.setMaskedCardNumber(transaction.getCreditCard().getMaskedCardNumber());
        }
        
        return response;
    }
    
    private RefundResponse convertToRefundResponse(PaymentTransaction refundTransaction,
                                                 PaymentTransaction originalTransaction,
                                                 BigDecimal refundFee,
                                                 GatewayRefundResponse gatewayResponse) {
        BigDecimal refundAmount = refundTransaction.getAmount().abs();
        return RefundResponse.success(
            refundTransaction.getTransactionId(),
            originalTransaction.getTransactionId(),
            originalTransaction.getOrderId(),
            originalTransaction.getCustomerId(),
            refundAmount,
            refundFee,
            originalTransaction.getPaymentMethod(),
            gatewayResponse.getGatewayRefundId(),
            refundTransaction.getDescription(),
            refundAmount.compareTo(originalTransaction.getAmount()) < 0
        );
    }
    
    private PaymentResponse handlePaymentProcessingException(PaymentProcessingException e, PaymentRequest request) {
        return PaymentResponse.failure(
            null,
            request.getOrderId(),
            request.getCustomerId(),
            request.getAmount(),
            request.getPaymentMethod(),
            e.getMessage(),
            e.isRetryable()
        );
    }
    
    private PaymentResponse handleUnexpectedException(Exception e, PaymentRequest request) {
        return PaymentResponse.failure(
            null,
            request.getOrderId(),
            request.getCustomerId(),
            request.getAmount(),
            request.getPaymentMethod(),
            "SYSTEM_ERROR",
            true
        );
    }
    
    private RefundResponse handleRefundProcessingException(PaymentProcessingException e, RefundRequest request) {
        return RefundResponse.failure(
            null,
            request.getTransactionId(),
            request.getOrderId(),
            request.getCustomerId(),
            request.getRefundAmount(),
            null,
            e.getMessage(),
            request.getReason()
        );
    }
    
    private RefundResponse handleRefundUnexpectedException(Exception e, RefundRequest request) {
        return RefundResponse.failure(
            null,
            request.getTransactionId(),
            request.getOrderId(),
            request.getCustomerId(),
            request.getRefundAmount(),
            null,
            "System error: " + e.getMessage(),
            request.getReason()
        );
    }
}