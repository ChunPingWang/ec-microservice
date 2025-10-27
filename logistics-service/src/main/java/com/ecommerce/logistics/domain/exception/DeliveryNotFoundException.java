package com.ecommerce.logistics.domain.exception;

import com.ecommerce.common.exception.ResourceNotFoundException;

/**
 * 配送請求不存在異常
 * 遵循 SRP 原則 - 只負責配送請求查找相關的異常處理
 */
public class DeliveryNotFoundException extends ResourceNotFoundException {
    
    public DeliveryNotFoundException(String deliveryId) {
        super("配送請求不存在: " + deliveryId);
    }
    
    public DeliveryNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}