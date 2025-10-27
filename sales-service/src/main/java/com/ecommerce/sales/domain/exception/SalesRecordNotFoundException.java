package com.ecommerce.sales.domain.exception;

import com.ecommerce.common.exception.ResourceNotFoundException;

/**
 * 銷售記錄不存在異常
 * 當查詢的銷售記錄不存在時拋出此異常
 */
public class SalesRecordNotFoundException extends ResourceNotFoundException {
    
    public SalesRecordNotFoundException(String salesRecordId) {
        super("找不到ID為 " + salesRecordId + " 的銷售記錄");
    }
    
    public SalesRecordNotFoundException(String field, String value) {
        super("銷售記錄", field + "=" + value);
    }
}