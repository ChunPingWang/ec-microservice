package com.ecommerce.common.patterns;

/**
 * 工廠模式基礎介面，遵循 OCP 和 DIP 原則
 * 支援物件創建的抽象化
 */
public interface Factory<T, P> {
    
    /**
     * 創建物件
     * @param parameter 創建參數
     * @return 創建的物件
     */
    T create(P parameter);
    
    /**
     * 判斷是否支援此類型的創建
     * @param parameter 創建參數
     * @return 是否支援
     */
    boolean supports(P parameter);
}