package com.ecommerce.common.exception;

import lombok.Getter;

/**
 * 基礎異常類別，遵循 SRP 原則
 * 提供所有業務異常的共用屬性和行為
 */
@Getter
public abstract class BaseException extends RuntimeException {
    
    private final String errorCode;
    private final String errorMessage;
    
    protected BaseException(String errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
    
    protected BaseException(String errorCode, String errorMessage, Throwable cause) {
        super(errorMessage, cause);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
}