package com.ecommerce.common.validation;

import com.ecommerce.common.exception.ValidationException;

/**
 * 驗證器介面，遵循 SRP 和 ISP 原則
 * 提供資料驗證的抽象契約
 */
public interface Validator<T> {
    
    /**
     * 驗證物件
     * @param object 要驗證的物件
     * @throws ValidationException 當驗證失敗時拋出
     */
    void validate(T object) throws ValidationException;
    
    /**
     * 檢查物件是否有效
     * @param object 要檢查的物件
     * @return 是否有效
     */
    boolean isValid(T object);
}