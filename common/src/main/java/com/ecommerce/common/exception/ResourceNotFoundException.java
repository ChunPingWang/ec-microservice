package com.ecommerce.common.exception;

/**
 * 資源不存在異常，遵循 SRP 原則
 * 用於處理資源查找失敗的情況
 */
public class ResourceNotFoundException extends BaseException {
    
    public ResourceNotFoundException(String resourceType, String resourceId) {
        super("RESOURCE_NOT_FOUND", 
              String.format("%s 不存在: %s", resourceType, resourceId));
    }
    
    public ResourceNotFoundException(String errorMessage) {
        super("RESOURCE_NOT_FOUND", errorMessage);
    }
}