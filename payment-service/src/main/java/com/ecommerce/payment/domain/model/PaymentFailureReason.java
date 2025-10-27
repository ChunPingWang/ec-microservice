package com.ecommerce.payment.domain.model;

/**
 * 付款失敗原因枚舉
 * 定義各種付款失敗的原因和處理方式
 */
public enum PaymentFailureReason {
    // 卡片相關錯誤
    INSUFFICIENT_FUNDS("01", "餘額不足", "Insufficient funds", false),
    INVALID_CARD("02", "無效卡片", "Invalid card", false),
    EXPIRED_CARD("03", "卡片過期", "Expired card", false),
    CARD_DECLINED("04", "卡片被拒", "Card declined", false),
    INVALID_CVV("05", "CVV錯誤", "Invalid CVV", false),
    CARD_BLOCKED("06", "卡片被鎖", "Card blocked", false),
    
    // 限額相關錯誤
    LIMIT_EXCEEDED("10", "超過限額", "Limit exceeded", false),
    DAILY_LIMIT_EXCEEDED("11", "超過日限額", "Daily limit exceeded", false),
    MONTHLY_LIMIT_EXCEEDED("12", "超過月限額", "Monthly limit exceeded", false),
    
    // 交易相關錯誤
    INVALID_AMOUNT("20", "無效金額", "Invalid amount", false),
    INVALID_CURRENCY("21", "無效幣別", "Invalid currency", false),
    DUPLICATE_TRANSACTION("22", "重複交易", "Duplicate transaction", false),
    INVALID_TRANSACTION_STATE("23", "無效交易狀態", "Invalid transaction state", false),
    
    // 閘道相關錯誤
    GATEWAY_ERROR("30", "閘道錯誤", "Gateway error", true),
    GATEWAY_TIMEOUT("31", "閘道超時", "Gateway timeout", true),
    GATEWAY_UNAVAILABLE("32", "閘道不可用", "Gateway unavailable", true),
    
    // 網路相關錯誤
    NETWORK_ERROR("40", "網路錯誤", "Network error", true),
    TIMEOUT("41", "連線超時", "Connection timeout", true),
    CONNECTION_FAILED("42", "連線失敗", "Connection failed", true),
    
    // 安全相關錯誤
    FRAUD_DETECTED("50", "疑似詐騙", "Fraud detected", false),
    SECURITY_VIOLATION("51", "安全違規", "Security violation", false),
    AUTHENTICATION_FAILED("52", "認證失敗", "Authentication failed", false),
    
    // 系統相關錯誤
    SYSTEM_ERROR("90", "系統錯誤", "System error", true),
    DATABASE_ERROR("91", "資料庫錯誤", "Database error", true),
    CONFIGURATION_ERROR("92", "設定錯誤", "Configuration error", false),
    SERVICE_UNAVAILABLE("93", "服務不可用", "Service unavailable", true),
    
    // 未知錯誤
    UNKNOWN_ERROR("99", "未知錯誤", "Unknown error", true);
    
    private final String code;
    private final String chineseDescription;
    private final String englishDescription;
    private final boolean retryable;
    
    PaymentFailureReason(String code, String chineseDescription, String englishDescription, boolean retryable) {
        this.code = code;
        this.chineseDescription = chineseDescription;
        this.englishDescription = englishDescription;
        this.retryable = retryable;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getChineseDescription() {
        return chineseDescription;
    }
    
    public String getEnglishDescription() {
        return englishDescription;
    }
    
    public String getDescription() {
        return chineseDescription;
    }
    
    public boolean isRetryable() {
        return retryable;
    }
    
    /**
     * 根據錯誤代碼取得失敗原因
     */
    public static PaymentFailureReason fromCode(String code) {
        if (code == null) {
            return UNKNOWN_ERROR;
        }
        
        for (PaymentFailureReason reason : values()) {
            if (reason.code.equals(code)) {
                return reason;
            }
        }
        
        return UNKNOWN_ERROR;
    }
    
    /**
     * 根據錯誤訊息推斷失敗原因
     */
    public static PaymentFailureReason fromMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            return UNKNOWN_ERROR;
        }
        
        String lowerMessage = message.toLowerCase();
        
        // 卡片相關
        if (lowerMessage.contains("insufficient") || lowerMessage.contains("餘額不足")) {
            return INSUFFICIENT_FUNDS;
        }
        if (lowerMessage.contains("invalid card") || lowerMessage.contains("無效卡片")) {
            return INVALID_CARD;
        }
        if (lowerMessage.contains("expired") || lowerMessage.contains("過期")) {
            return EXPIRED_CARD;
        }
        if (lowerMessage.contains("declined") || lowerMessage.contains("被拒")) {
            return CARD_DECLINED;
        }
        if (lowerMessage.contains("cvv") || lowerMessage.contains("cvc")) {
            return INVALID_CVV;
        }
        if (lowerMessage.contains("blocked") || lowerMessage.contains("被鎖")) {
            return CARD_BLOCKED;
        }
        
        // 限額相關
        if (lowerMessage.contains("limit exceeded") || lowerMessage.contains("超過限額")) {
            return LIMIT_EXCEEDED;
        }
        
        // 網路相關
        if (lowerMessage.contains("network") || lowerMessage.contains("網路")) {
            return NETWORK_ERROR;
        }
        if (lowerMessage.contains("timeout") || lowerMessage.contains("超時")) {
            return TIMEOUT;
        }
        if (lowerMessage.contains("connection") || lowerMessage.contains("連線")) {
            return CONNECTION_FAILED;
        }
        
        // 閘道相關
        if (lowerMessage.contains("gateway") || lowerMessage.contains("閘道")) {
            return GATEWAY_ERROR;
        }
        
        // 安全相關
        if (lowerMessage.contains("fraud") || lowerMessage.contains("詐騙")) {
            return FRAUD_DETECTED;
        }
        
        // 系統相關
        if (lowerMessage.contains("system") || lowerMessage.contains("系統")) {
            return SYSTEM_ERROR;
        }
        if (lowerMessage.contains("database") || lowerMessage.contains("資料庫")) {
            return DATABASE_ERROR;
        }
        if (lowerMessage.contains("service unavailable") || lowerMessage.contains("服務不可用")) {
            return SERVICE_UNAVAILABLE;
        }
        
        return UNKNOWN_ERROR;
    }
    
    /**
     * 檢查是否為卡片相關錯誤
     */
    public boolean isCardRelated() {
        return this == INSUFFICIENT_FUNDS || this == INVALID_CARD || this == EXPIRED_CARD ||
               this == CARD_DECLINED || this == INVALID_CVV || this == CARD_BLOCKED;
    }
    
    /**
     * 檢查是否為限額相關錯誤
     */
    public boolean isLimitRelated() {
        return this == LIMIT_EXCEEDED || this == DAILY_LIMIT_EXCEEDED || this == MONTHLY_LIMIT_EXCEEDED;
    }
    
    /**
     * 檢查是否為網路相關錯誤
     */
    public boolean isNetworkRelated() {
        return this == NETWORK_ERROR || this == TIMEOUT || this == CONNECTION_FAILED;
    }
    
    /**
     * 檢查是否為閘道相關錯誤
     */
    public boolean isGatewayRelated() {
        return this == GATEWAY_ERROR || this == GATEWAY_TIMEOUT || this == GATEWAY_UNAVAILABLE;
    }
    
    /**
     * 檢查是否為安全相關錯誤
     */
    public boolean isSecurityRelated() {
        return this == FRAUD_DETECTED || this == SECURITY_VIOLATION || this == AUTHENTICATION_FAILED;
    }
    
    /**
     * 檢查是否為系統相關錯誤
     */
    public boolean isSystemRelated() {
        return this == SYSTEM_ERROR || this == DATABASE_ERROR || this == CONFIGURATION_ERROR ||
               this == SERVICE_UNAVAILABLE;
    }
    
    /**
     * 取得建議的處理動作
     */
    public String getSuggestedAction() {
        if (isCardRelated()) {
            return "請檢查卡片資訊或聯絡發卡銀行";
        } else if (isLimitRelated()) {
            return "請檢查交易限額或聯絡銀行調整";
        } else if (isNetworkRelated() || isGatewayRelated()) {
            return "請稍後再試或聯絡客服";
        } else if (isSecurityRelated()) {
            return "請聯絡客服進行身份驗證";
        } else if (isSystemRelated()) {
            return "系統維護中，請稍後再試";
        } else {
            return "請聯絡客服協助處理";
        }
    }
}