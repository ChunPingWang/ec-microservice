package com.ecommerce.sales.domain.model;

/**
 * 銷售通道枚舉
 * 定義不同的銷售管道
 */
public enum SalesChannel {
    ONLINE("線上"),
    MOBILE_APP("手機應用"),
    PHYSICAL_STORE("實體店面"),
    PHONE("電話銷售"),
    SOCIAL_MEDIA("社群媒體");
    
    private final String displayName;
    
    SalesChannel(String displayName) {
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