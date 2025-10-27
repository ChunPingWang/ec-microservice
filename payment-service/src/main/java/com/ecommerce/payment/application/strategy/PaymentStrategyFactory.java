package com.ecommerce.payment.application.strategy;


import com.ecommerce.common.exception.ValidationException;
import com.ecommerce.payment.domain.model.PaymentMethod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 付款策略工廠
 * 根據付款方式選擇對應的付款策略
 */

public class PaymentStrategyFactory {
    
    private final Map<PaymentMethod, PaymentStrategy> strategies;
    
    public PaymentStrategyFactory(List<PaymentStrategy> paymentStrategies) {
        this.strategies = new HashMap<>();
        
        // 註冊所有付款策略
        for (PaymentStrategy strategy : paymentStrategies) {
            strategies.put(strategy.getSupportedPaymentMethod(), strategy);
        }
    }
    
    /**
     * 根據付款方式取得對應的策略
     */
    public PaymentStrategy getStrategy(PaymentMethod paymentMethod) {
        if (paymentMethod == null) {
            throw new ValidationException("Payment method is required");
        }
        
        PaymentStrategy strategy = strategies.get(paymentMethod);
        if (strategy == null) {
            throw new ValidationException("Unsupported payment method: " + paymentMethod);
        }
        
        return strategy;
    }
    
    /**
     * 檢查是否支援指定的付款方式
     */
    public boolean isSupported(PaymentMethod paymentMethod) {
        return paymentMethod != null && strategies.containsKey(paymentMethod);
    }
    
    /**
     * 取得所有支援的付款方式
     */
    public PaymentMethod[] getSupportedPaymentMethods() {
        return strategies.keySet().toArray(new PaymentMethod[0]);
    }
    
    /**
     * 檢查指定付款方式的策略是否可用
     */
    public boolean isStrategyAvailable(PaymentMethod paymentMethod) {
        if (!isSupported(paymentMethod)) {
            return false;
        }
        
        try {
            PaymentStrategy strategy = getStrategy(paymentMethod);
            return strategy.isAvailable();
        } catch (Exception e) {
            return false;
        }
    }
}