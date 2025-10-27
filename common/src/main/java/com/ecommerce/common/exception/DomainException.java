package com.ecommerce.common.exception;

/**
 * 領域異常基礎類別
 * 所有領域層的業務異常都應該繼承此類別
 */
public abstract class DomainException extends RuntimeException {
    
    private final String errorCode;
    
    public DomainException(String message) {
        super(message);
        this.errorCode = this.getClass().getSimpleName();
    }
    
    public DomainException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public DomainException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = this.getClass().getSimpleName();
    }
    
    public DomainException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}