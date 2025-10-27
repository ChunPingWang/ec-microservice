package com.ecommerce.logistics.domain.model;

import java.math.BigDecimal;

/**
 * 配送類型枚舉
 * 遵循 SRP 原則 - 只負責定義配送類型和相關屬性
 */
public enum DeliveryType {
    STANDARD("標準配送", BigDecimal.valueOf(60), 3),
    EXPRESS("快速配送", BigDecimal.valueOf(120), 1),
    SAME_DAY("當日配送", BigDecimal.valueOf(200), 0);
    
    private final String description;
    private final BigDecimal fee;
    private final int deliveryDays;
    
    DeliveryType(String description, BigDecimal fee, int deliveryDays) {
        this.description = description;
        this.fee = fee;
        this.deliveryDays = deliveryDays;
    }
    
    public String getDescription() {
        return description;
    }
    
    public BigDecimal getFee() {
        return fee;
    }
    
    public int getDeliveryDays() {
        return deliveryDays;
    }
}