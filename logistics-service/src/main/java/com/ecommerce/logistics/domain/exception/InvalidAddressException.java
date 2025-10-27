package com.ecommerce.logistics.domain.exception;

import com.ecommerce.common.exception.DomainException;

/**
 * 無效地址異常
 * 遵循 SRP 原則 - 只負責地址驗證相關的異常處理
 */
public class InvalidAddressException extends DomainException {
    
    public InvalidAddressException(String message) {
        super(message);
    }
    
    public InvalidAddressException(String message, Throwable cause) {
        super(message, cause);
    }
}