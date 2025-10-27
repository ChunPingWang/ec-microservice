package com.ecommerce.logistics.domain.repository;

import com.ecommerce.logistics.domain.model.DeliveryRequest;
import com.ecommerce.logistics.domain.model.DeliveryStatus;
import com.ecommerce.common.patterns.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 配送倉儲介面
 * 遵循 DIP 原則 - 定義領域層需要的抽象介面
 * 遵循 ISP 原則 - 只定義配送相關的操作
 */
public interface DeliveryRepository {
    
    /**
     * 儲存配送請求
     */
    DeliveryRequest save(DeliveryRequest deliveryRequest);
    
    /**
     * 根據配送ID查找配送請求
     */
    Optional<DeliveryRequest> findById(String deliveryId);
    
    /**
     * 根據訂單ID查找配送請求
     */
    Optional<DeliveryRequest> findByOrderId(String orderId);
    
    /**
     * 根據客戶ID查找配送請求列表
     */
    List<DeliveryRequest> findByCustomerId(String customerId);
    
    /**
     * 根據狀態查找配送請求列表
     */
    List<DeliveryRequest> findByStatus(DeliveryStatus status);
    
    /**
     * 根據追蹤號碼查找配送請求
     */
    Optional<DeliveryRequest> findByTrackingNumber(String trackingNumber);
    
    /**
     * 刪除配送請求
     */
    void deleteById(String deliveryId);
    
    /**
     * 檢查配送請求是否存在
     */
    boolean existsById(String deliveryId);
    
    /**
     * 檢查訂單是否已有配送請求
     */
    boolean existsByOrderId(String orderId);
}