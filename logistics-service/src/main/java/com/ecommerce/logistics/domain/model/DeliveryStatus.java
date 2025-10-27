package com.ecommerce.logistics.domain.model;

/**
 * 配送狀態枚舉
 * 遵循 SRP 原則 - 只負責定義配送狀態
 */
public enum DeliveryStatus {
    PENDING("待配送"),
    IN_TRANSIT("配送中"),
    OUT_FOR_DELIVERY("派送中"),
    DELIVERED("已送達"),
    FAILED("配送失敗"),
    CANCELLED("已取消"),
    RETURNED("已退回");
    
    private final String description;
    
    DeliveryStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 檢查是否可以轉換到目標狀態
     * 實作業務規則驗證
     */
    public boolean canTransitionTo(DeliveryStatus targetStatus) {
        return switch (this) {
            case PENDING -> targetStatus == IN_TRANSIT || targetStatus == CANCELLED;
            case IN_TRANSIT -> targetStatus == OUT_FOR_DELIVERY || targetStatus == FAILED || targetStatus == RETURNED;
            case OUT_FOR_DELIVERY -> targetStatus == DELIVERED || targetStatus == FAILED;
            case DELIVERED, CANCELLED, RETURNED -> false; // 終端狀態
            case FAILED -> targetStatus == IN_TRANSIT || targetStatus == CANCELLED;
        };
    }
}