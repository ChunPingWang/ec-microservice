package com.ecommerce.sales.domain.model;

/**
 * 報表類型枚舉
 * 定義不同類型的銷售報表
 */
public enum ReportType {
    DAILY("日報表"),
    WEEKLY("週報表"),
    MONTHLY("月報表"),
    QUARTERLY("季報表"),
    YEARLY("年報表"),
    PRODUCT_CATEGORY("商品分類報表"),
    CUSTOMER_SEGMENT("客戶分群報表"),
    CHANNEL_PERFORMANCE("通道績效報表"),
    REGIONAL("區域銷售報表"),
    CUSTOM("自訂報表");
    
    private final String displayName;
    
    ReportType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}