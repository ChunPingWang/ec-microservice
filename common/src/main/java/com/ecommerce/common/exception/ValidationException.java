package com.ecommerce.common.exception;

/**
 * 驗證異常，遵循 SRP 原則
 * 用於處理資料驗證失敗的情況
 */
public class ValidationException extends BaseException {
    
    public ValidationException(String fieldName, String errorMessage) {
        super("VALIDATION_ERROR", 
              String.format("驗證失敗 - %s: %s", fieldName, errorMessage));
    }
    
    public ValidationException(String errorMessage) {
        super("VALIDATION_ERROR", errorMessage);
    }
}