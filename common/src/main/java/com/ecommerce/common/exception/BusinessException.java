package com.ecommerce.common.exception;

/**
 * 業務邏輯異常，遵循 SRP 原則
 * 用於處理業務規則違反的情況
 */
public class BusinessException extends BaseException {
    
    public BusinessException(String errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }
    
    public BusinessException(String errorCode, String errorMessage, Throwable cause) {
        super(errorCode, errorMessage, cause);
    }
}