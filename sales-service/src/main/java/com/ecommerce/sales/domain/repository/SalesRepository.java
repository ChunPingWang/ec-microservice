package com.ecommerce.sales.domain.repository;

import com.ecommerce.sales.domain.model.SalesChannel;
import com.ecommerce.sales.domain.model.SalesRecord;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 銷售記錄倉儲介面 - 定義資料存取抽象
 * 遵循 DIP：高層模組定義抽象介面
 * 遵循 ISP：介面職責單一，只處理銷售記錄相關操作
 */
public interface SalesRepository {
    
    /**
     * 儲存銷售記錄
     */
    SalesRecord save(SalesRecord salesRecord);
    
    /**
     * 根據ID查詢銷售記錄
     */
    Optional<SalesRecord> findById(String salesRecordId);
    
    /**
     * 根據訂單ID查詢銷售記錄
     */
    List<SalesRecord> findByOrderId(String orderId);
    
    /**
     * 根據客戶ID查詢銷售記錄
     */
    List<SalesRecord> findByCustomerId(String customerId);
    
    /**
     * 根據商品ID查詢銷售記錄
     */
    List<SalesRecord> findByProductId(String productId);
    
    /**
     * 根據日期範圍查詢銷售記錄
     */
    List<SalesRecord> findByDateRange(LocalDate startDate, LocalDate endDate);
    
    /**
     * 根據分類查詢銷售記錄
     */
    List<SalesRecord> findByCategory(String category);
    
    /**
     * 根據銷售通道查詢銷售記錄
     */
    List<SalesRecord> findByChannel(SalesChannel channel);
    
    /**
     * 根據區域查詢銷售記錄
     */
    List<SalesRecord> findByRegion(String region);
    
    /**
     * 查詢指定日期範圍內的高價值銷售記錄
     */
    List<SalesRecord> findHighValueSalesByDateRange(LocalDate startDate, LocalDate endDate);
    
    /**
     * 查詢指定日期範圍內的促銷銷售記錄
     */
    List<SalesRecord> findPromotionalSalesByDateRange(LocalDate startDate, LocalDate endDate);
    
    /**
     * 查詢所有銷售記錄
     */
    List<SalesRecord> findAll();
    
    /**
     * 刪除銷售記錄
     */
    void deleteById(String salesRecordId);
    
    /**
     * 檢查銷售記錄是否存在
     */
    boolean existsById(String salesRecordId);
    
    /**
     * 檢查訂單是否已有銷售記錄
     */
    boolean existsByOrderId(String orderId);
}