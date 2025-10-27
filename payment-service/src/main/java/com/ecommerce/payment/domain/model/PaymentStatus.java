package com.ecommerce.payment.domain.model;

/**
 * 付款狀態枚舉
 * 定義付款交易的生命週期狀態
 */
public enum PaymentStatus {
    PENDING("待處理"),
    PROCESSING("處理中"),
    SUCCESS("成功"),
    FAILED("失敗"),
    CANCELLED("已取消"),
    REFUNDED("已退款"),
    PARTIAL_REFUNDED("部分退款");
    
    private final String description;
    
    PaymentStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 檢查是否為最終狀態
     */
    public boolean isFinalStatus() {
        return this == SUCCESS || this == FAILED || this == CANCELLED || this == REFUNDED;
    }
    
    /**
     * 檢查是否可以取消
     */
    public boolean isCancellable() {
        return this == PENDING || this == PROCESSING;
    }
    
    /**
     * 檢查是否可以退款
     */
    public boolean isRefundable() {
        return this == SUCCESS;
    }
    
    /**
     * 檢查是否可以部分退款
     */
    public boolean isPartialRefundable() {
        return this == SUCCESS || this == PARTIAL_REFUNDED;
    }
}