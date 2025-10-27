package com.ecommerce.logistics.infrastructure.adapter.persistence.repository;

import com.ecommerce.logistics.domain.model.DeliveryStatus;
import com.ecommerce.logistics.infrastructure.adapter.persistence.entity.DeliveryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 配送JPA倉儲介面
 * 遵循 ISP 原則 - 只定義配送相關的資料庫操作
 */
@Repository
public interface DeliveryJpaRepository extends JpaRepository<DeliveryJpaEntity, String> {
    
    /**
     * 根據訂單ID查找配送請求
     */
    Optional<DeliveryJpaEntity> findByOrderId(String orderId);
    
    /**
     * 根據客戶ID查找配送請求列表
     */
    List<DeliveryJpaEntity> findByCustomerId(String customerId);
    
    /**
     * 根據狀態查找配送請求列表
     */
    List<DeliveryJpaEntity> findByStatus(DeliveryStatus status);
    
    /**
     * 根據追蹤號碼查找配送請求
     */
    Optional<DeliveryJpaEntity> findByTrackingNumber(String trackingNumber);
    
    /**
     * 檢查訂單是否已有配送請求
     */
    boolean existsByOrderId(String orderId);
    
    /**
     * 根據客戶ID和狀態查找配送請求列表
     */
    @Query("SELECT d FROM DeliveryJpaEntity d WHERE d.customerId = :customerId AND d.status = :status")
    List<DeliveryJpaEntity> findByCustomerIdAndStatus(@Param("customerId") String customerId, 
                                                      @Param("status") DeliveryStatus status);
    
    /**
     * 查找指定狀態的配送請求數量
     */
    @Query("SELECT COUNT(d) FROM DeliveryJpaEntity d WHERE d.status = :status")
    long countByStatus(@Param("status") DeliveryStatus status);
    
    /**
     * 查找客戶的配送請求數量
     */
    @Query("SELECT COUNT(d) FROM DeliveryJpaEntity d WHERE d.customerId = :customerId")
    long countByCustomerId(@Param("customerId") String customerId);
}