package com.ecommerce.payment.infrastructure.adapter.persistence.mapper;

import com.ecommerce.payment.domain.model.CreditCard;
import com.ecommerce.payment.domain.model.PaymentTransaction;
import com.ecommerce.payment.infrastructure.adapter.persistence.entity.PaymentTransactionJpaEntity;
import org.springframework.stereotype.Component;

import java.time.YearMonth;

/**
 * 付款交易 JPA 映射器
 * 負責領域實體與 JPA 實體之間的轉換
 */
@Component
public class PaymentTransactionJpaMapper {
    
    /**
     * 將領域實體轉換為 JPA 實體
     */
    public PaymentTransactionJpaEntity toJpaEntity(PaymentTransaction domainEntity) {
        if (domainEntity == null) {
            return null;
        }
        
        PaymentTransactionJpaEntity jpaEntity = new PaymentTransactionJpaEntity(
            domainEntity.getTransactionId(),
            domainEntity.getOrderId(),
            domainEntity.getCustomerId(),
            domainEntity.getAmount(),
            domainEntity.getPaymentMethod(),
            domainEntity.getStatus()
        );
        
        // 設定其他屬性
        jpaEntity.setRefundedAmount(domainEntity.getRefundedAmount());
        jpaEntity.setGatewayTransactionId(domainEntity.getGatewayTransactionId());
        jpaEntity.setGatewayResponse(domainEntity.getGatewayResponse());
        jpaEntity.setFailureReason(domainEntity.getFailureReason());
        jpaEntity.setDescription(domainEntity.getDescription());
        jpaEntity.setMerchantReference(domainEntity.getMerchantReference());
        jpaEntity.setProcessedAt(domainEntity.getProcessedAt());
        jpaEntity.setRefundedAt(domainEntity.getRefundedAt());
        
        // 設定時間戳
        jpaEntity.setCreatedAt(domainEntity.getCreatedAt());
        jpaEntity.setUpdatedAt(domainEntity.getUpdatedAt());
        
        // 處理信用卡資訊
        if (domainEntity.getCreditCard() != null) {
            CreditCard creditCard = domainEntity.getCreditCard();
            jpaEntity.setMaskedCardNumber(creditCard.getMaskedCardNumber());
            jpaEntity.setCardHolderName(creditCard.getCardHolderName());
            
            if (creditCard.getExpiryDate() != null) {
                jpaEntity.setCardExpiryMonth(creditCard.getExpiryDate().getMonthValue());
                jpaEntity.setCardExpiryYear(creditCard.getExpiryDate().getYear());
            }
        }
        
        return jpaEntity;
    }
    
    /**
     * 將 JPA 實體轉換為領域實體
     */
    public PaymentTransaction toDomainEntity(PaymentTransactionJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }
        
        // 使用反射或建構子創建領域實體
        // 由於 PaymentTransaction 的建構子是 protected，我們需要使用工廠方法
        PaymentTransaction domainEntity = PaymentTransaction.create(
            jpaEntity.getOrderId(),
            jpaEntity.getCustomerId(),
            jpaEntity.getAmount(),
            jpaEntity.getPaymentMethod(),
            jpaEntity.getDescription()
        );
        
        // 使用反射設定私有欄位（在實際專案中可能需要更優雅的方式）
        setPrivateField(domainEntity, "transactionId", jpaEntity.getTransactionId());
        setPrivateField(domainEntity, "refundedAmount", jpaEntity.getRefundedAmount());
        setPrivateField(domainEntity, "status", jpaEntity.getStatus());
        setPrivateField(domainEntity, "gatewayTransactionId", jpaEntity.getGatewayTransactionId());
        setPrivateField(domainEntity, "gatewayResponse", jpaEntity.getGatewayResponse());
        setPrivateField(domainEntity, "failureReason", jpaEntity.getFailureReason());
        setPrivateField(domainEntity, "merchantReference", jpaEntity.getMerchantReference());
        setPrivateField(domainEntity, "processedAt", jpaEntity.getProcessedAt());
        setPrivateField(domainEntity, "refundedAt", jpaEntity.getRefundedAt());
        
        // 設定時間戳
        domainEntity.setCreatedAt(jpaEntity.getCreatedAt());
        domainEntity.setUpdatedAt(jpaEntity.getUpdatedAt());
        
        // 重建信用卡資訊（注意：從資料庫讀取時，我們只能重建部分資訊）
        if (jpaEntity.getMaskedCardNumber() != null && jpaEntity.getCardHolderName() != null) {
            YearMonth expiryDate = null;
            if (jpaEntity.getCardExpiryMonth() != null && jpaEntity.getCardExpiryYear() != null) {
                expiryDate = YearMonth.of(jpaEntity.getCardExpiryYear(), jpaEntity.getCardExpiryMonth());
            }
            
            // 由於我們只儲存遮罩後的卡號，無法完全重建 CreditCard 物件
            // 在實際應用中，可能需要建立一個專門的 MaskedCreditCard 類別
            // 或者在 CreditCard 中添加支援遮罩資料的建構方法
            // 目前暫時設為 null，表示信用卡資訊不完整
            setPrivateField(domainEntity, "creditCard", null);
        }
        
        return domainEntity;
    }
    
    /**
     * 更新 JPA 實體的資料
     */
    public void updateJpaEntity(PaymentTransactionJpaEntity jpaEntity, PaymentTransaction domainEntity) {
        if (jpaEntity == null || domainEntity == null) {
            return;
        }
        
        // 更新可變的欄位
        jpaEntity.setStatus(domainEntity.getStatus());
        jpaEntity.setRefundedAmount(domainEntity.getRefundedAmount());
        jpaEntity.setGatewayTransactionId(domainEntity.getGatewayTransactionId());
        jpaEntity.setGatewayResponse(domainEntity.getGatewayResponse());
        jpaEntity.setFailureReason(domainEntity.getFailureReason());
        jpaEntity.setProcessedAt(domainEntity.getProcessedAt());
        jpaEntity.setRefundedAt(domainEntity.getRefundedAt());
        jpaEntity.setUpdatedAt(domainEntity.getUpdatedAt());
        
        // 更新信用卡資訊（如果有變更）
        if (domainEntity.getCreditCard() != null) {
            CreditCard creditCard = domainEntity.getCreditCard();
            jpaEntity.setMaskedCardNumber(creditCard.getMaskedCardNumber());
            jpaEntity.setCardHolderName(creditCard.getCardHolderName());
            
            if (creditCard.getExpiryDate() != null) {
                jpaEntity.setCardExpiryMonth(creditCard.getExpiryDate().getMonthValue());
                jpaEntity.setCardExpiryYear(creditCard.getExpiryDate().getYear());
            }
        }
    }
    
    /**
     * 使用反射設定私有欄位
     * 注意：這是一個簡化的實作，在生產環境中可能需要更安全的方式
     */
    private void setPrivateField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // 在實際專案中，可能需要更好的錯誤處理
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }
}