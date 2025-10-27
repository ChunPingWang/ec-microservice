package com.ecommerce.payment.application.port.out;

import com.ecommerce.payment.domain.model.PaymentTransaction;
import com.ecommerce.payment.domain.repository.PaymentRepository;

/**
 * 付款持久化輸出埠
 * 繼承領域倉儲介面，提供資料存取功能
 */
public interface PaymentPersistencePort extends PaymentRepository {
    // 繼承所有 PaymentRepository 的方法
    // 可以在此添加應用層特定的資料存取需求
}