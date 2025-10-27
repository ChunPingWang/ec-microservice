package com.ecommerce.logistics.domain.exception;

import com.ecommerce.common.exception.DomainException;

/**
 * 無效配送狀態異常
 * 遵循 SRP 原則 - 只負責配送狀態相關的異常處理
 */
public class InvalidDeliveryStateException extends DomainException {
    
    public InvalidDeliveryStateException(String message) {
        super(message);
    }
    
    public InvalidDeliveryStateException(String message, Throwable cause) {
        super(message, cause);
    }
}