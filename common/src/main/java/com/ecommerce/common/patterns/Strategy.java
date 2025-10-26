package com.ecommerce.common.patterns;

/**
 * 策略模式基礎介面，遵循 OCP 和 LSP 原則
 * 支援不同策略的實作和替換
 */
public interface Strategy<T, R> {
    
    /**
     * 執行策略邏輯
     * @param input 輸入參數
     * @return 執行結果
     */
    R execute(T input);
    
    /**
     * 判斷是否支援此策略
     * @param input 輸入參數
     * @return 是否支援
     */
    boolean supports(T input);
}