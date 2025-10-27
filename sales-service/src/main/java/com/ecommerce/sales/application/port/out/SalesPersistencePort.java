package com.ecommerce.sales.application.port.out;

import com.ecommerce.sales.domain.model.SalesChannel;
import com.ecommerce.sales.domain.model.SalesRecord;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 銷售資料持久化輸出埠
 * 遵循 DIP：應用層定義抽象介面，基礎設施層實作
 */
public interface SalesPersistencePort {
    
    SalesRecord save(SalesRecord salesRecord);
    
    Optional<SalesRecord> findById(String salesRecordId);
    
    List<SalesRecord> findByOrderId(String orderId);
    
    List<SalesRecord> findByCustomerId(String customerId);
    
    List<SalesRecord> findByProductId(String productId);
    
    List<SalesRecord> findByDateRange(LocalDate startDate, LocalDate endDate);
    
    List<SalesRecord> findByCategory(String category);
    
    List<SalesRecord> findByChannel(SalesChannel channel);
    
    List<SalesRecord> findByRegion(String region);
    
    List<SalesRecord> findHighValueSalesByDateRange(LocalDate startDate, LocalDate endDate);
    
    List<SalesRecord> findPromotionalSalesByDateRange(LocalDate startDate, LocalDate endDate);
    
    List<SalesRecord> findAll();
    
    void deleteById(String salesRecordId);
    
    boolean existsById(String salesRecordId);
    
    boolean existsByOrderId(String orderId);
}