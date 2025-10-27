package com.ecommerce.payment.domain.model;

/**
 * 付款方式枚舉
 */
public enum PaymentMethod {
    CREDIT_CARD("信用卡"),
    DEBIT_CARD("金融卡"),
    BANK_TRANSFER("銀行轉帳"),
    DIGITAL_WALLET("數位錢包"),
    CASH_ON_DELIVERY("貨到付款");
    
    private final String description;
    
    PaymentMethod(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 檢查是否需要即時處理
     */
    public boolean requiresImmediateProcessing() {
        return this == CREDIT_CARD || this == DEBIT_CARD || this == DIGITAL_WALLET;
    }
    
    /**
     * 檢查是否支援退款
     */
    public boolean supportsRefund() {
        return this != CASH_ON_DELIVERY;
    }
}