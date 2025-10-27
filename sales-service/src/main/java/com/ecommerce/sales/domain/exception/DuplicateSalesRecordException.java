package com.ecommerce.sales.domain.exception;

import com.ecommerce.common.exception.BusinessException;

/**
 * 重複銷售記錄異常
 * 當嘗試為同一訂單建立重複的銷售記錄時拋出此異常
 */
public class DuplicateSalesRecordException extends BusinessException {
    
    public DuplicateSalesRecordException(String orderId) {
        super("DUPLICATE_SALES_RECORD", 
              "訂單 " + orderId + " 的銷售記錄已存在，不能重複建立");
    }
}