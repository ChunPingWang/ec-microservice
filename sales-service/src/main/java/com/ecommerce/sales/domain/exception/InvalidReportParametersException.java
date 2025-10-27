package com.ecommerce.sales.domain.exception;

import com.ecommerce.common.exception.ValidationException;

/**
 * 無效報表參數異常
 * 當報表生成參數無效時拋出此異常
 */
public class InvalidReportParametersException extends ValidationException {
    
    public InvalidReportParametersException(String message) {
        super(message);
    }
    
    public InvalidReportParametersException(String parameter, String reason) {
        super(parameter, reason);
    }
}