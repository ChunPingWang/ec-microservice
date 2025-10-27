package com.ecommerce.order.domain.model;

/**
 * 訂單狀態枚舉
 * 定義訂單的生命週期狀態
 */
public enum OrderStatus {
    PENDING("待處理"),
    CONFIRMED("已確認"),
    PAID("已付款"),
    SHIPPED("已出貨"),
    DELIVERED("已送達"),
    CANCELLED("已取消"),
    REFUNDED("已退款");
    
    private final String description;
    
    OrderStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 檢查是否可以轉換到目標狀態
     */
    public boolean canTransitionTo(OrderStatus targetStatus) {
        return switch (this) {
            case PENDING -> targetStatus == CONFIRMED || targetStatus == CANCELLED;
            case CONFIRMED -> targetStatus == PAID || targetStatus == CANCELLED;
            case PAID -> targetStatus == SHIPPED || targetStatus == REFUNDED;
            case SHIPPED -> targetStatus == DELIVERED;
            case DELIVERED -> targetStatus == REFUNDED;
            case CANCELLED, REFUNDED -> false;
        };
    }
    
    /**
     * 檢查是否為最終狀態
     */
    public boolean isFinalStatus() {
        return this == DELIVERED || this == CANCELLED || this == REFUNDED;
    }
    
    /**
     * 檢查是否可以取消
     */
    public boolean isCancellable() {
        return this == PENDING || this == CONFIRMED;
    }
}