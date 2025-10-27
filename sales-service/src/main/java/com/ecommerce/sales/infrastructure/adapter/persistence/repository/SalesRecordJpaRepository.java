package com.ecommerce.sales.infrastructure.adapter.persistence.repository;

import com.ecommerce.sales.domain.model.SalesChannel;
import com.ecommerce.sales.infrastructure.adapter.persistence.entity.SalesRecordJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 銷售記錄 JPA 倉儲介面
 * 遵循 ISP：介面職責單一，只處理資料存取
 */
@Repository
public interface SalesRecordJpaRepository extends JpaRepository<SalesRecordJpaEntity, String> {
    
    /**
     * 根據訂單ID查詢銷售記錄
     */
    List<SalesRecordJpaEntity> findByOrderId(String orderId);
    
    /**
     * 根據客戶ID查詢銷售記錄
     */
    List<SalesRecordJpaEntity> findByCustomerId(String customerId);
    
    /**
     * 根據商品ID查詢銷售記錄
     */
    List<SalesRecordJpaEntity> findByProductId(String productId);
    
    /**
     * 根據日期範圍查詢銷售記錄
     */
    List<SalesRecordJpaEntity> findBySaleDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * 根據分類查詢銷售記錄
     */
    List<SalesRecordJpaEntity> findByCategory(String category);
    
    /**
     * 根據銷售通道查詢銷售記錄
     */
    List<SalesRecordJpaEntity> findByChannel(SalesChannel channel);
    
    /**
     * 根據區域查詢銷售記錄
     */
    List<SalesRecordJpaEntity> findByRegion(String region);
    
    /**
     * 查詢指定日期範圍內的高價值銷售記錄
     */
    @Query("SELECT s FROM SalesRecordJpaEntity s WHERE s.saleDate BETWEEN :startDate AND :endDate " +
           "AND s.totalAmount >= :minAmount")
    List<SalesRecordJpaEntity> findHighValueSalesByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("minAmount") BigDecimal minAmount);
    
    /**
     * 查詢指定日期範圍內的促銷銷售記錄
     */
    @Query("SELECT s FROM SalesRecordJpaEntity s WHERE s.saleDate BETWEEN :startDate AND :endDate " +
           "AND s.discount > 0")
    List<SalesRecordJpaEntity> findPromotionalSalesByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    /**
     * 檢查訂單是否已有銷售記錄
     */
    boolean existsByOrderId(String orderId);
    
    /**
     * 根據客戶ID和日期範圍查詢銷售記錄
     */
    @Query("SELECT s FROM SalesRecordJpaEntity s WHERE s.customerId = :customerId " +
           "AND s.saleDate BETWEEN :startDate AND :endDate")
    List<SalesRecordJpaEntity> findByCustomerIdAndDateRange(
            @Param("customerId") String customerId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    /**
     * 根據分類和日期範圍查詢銷售記錄
     */
    @Query("SELECT s FROM SalesRecordJpaEntity s WHERE s.category = :category " +
           "AND s.saleDate BETWEEN :startDate AND :endDate")
    List<SalesRecordJpaEntity> findByCategoryAndDateRange(
            @Param("category") String category,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    /**
     * 根據通道和日期範圍查詢銷售記錄
     */
    @Query("SELECT s FROM SalesRecordJpaEntity s WHERE s.channel = :channel " +
           "AND s.saleDate BETWEEN :startDate AND :endDate")
    List<SalesRecordJpaEntity> findByChannelAndDateRange(
            @Param("channel") SalesChannel channel,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    /**
     * 統計指定期間的總收入
     */
    @Query("SELECT COALESCE(SUM(s.totalAmount), 0) FROM SalesRecordJpaEntity s " +
           "WHERE s.saleDate BETWEEN :startDate AND :endDate")
    BigDecimal sumTotalAmountByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    /**
     * 統計指定期間的總銷量
     */
    @Query("SELECT COALESCE(SUM(s.quantity), 0) FROM SalesRecordJpaEntity s " +
           "WHERE s.saleDate BETWEEN :startDate AND :endDate")
    Integer sumQuantityByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}