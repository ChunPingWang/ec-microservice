package com.ecommerce.sales.infrastructure.adapter.persistence;

import com.ecommerce.common.architecture.PersistenceAdapter;
import com.ecommerce.sales.application.port.out.SalesPersistencePort;
import com.ecommerce.sales.domain.model.SalesChannel;
import com.ecommerce.sales.domain.model.SalesRecord;
import com.ecommerce.sales.domain.repository.SalesRepository;
import com.ecommerce.sales.infrastructure.adapter.persistence.entity.SalesRecordJpaEntity;
import com.ecommerce.sales.infrastructure.adapter.persistence.mapper.SalesRecordJpaMapper;
import com.ecommerce.sales.infrastructure.adapter.persistence.repository.SalesRecordJpaRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 銷售 JPA 適配器
 * 遵循 DIP：實作輸出埠介面，提供資料持久化功能
 * 遵循 SRP：只負責資料存取邏輯
 */
@Component
@PersistenceAdapter
public class SalesJpaAdapter implements SalesPersistencePort, SalesRepository {
    
    private final SalesRecordJpaRepository salesRecordJpaRepository;
    private final SalesRecordJpaMapper salesRecordJpaMapper;
    
    public SalesJpaAdapter(SalesRecordJpaRepository salesRecordJpaRepository,
                         SalesRecordJpaMapper salesRecordJpaMapper) {
        this.salesRecordJpaRepository = salesRecordJpaRepository;
        this.salesRecordJpaMapper = salesRecordJpaMapper;
    }
    
    @Override
    public SalesRecord save(SalesRecord salesRecord) {
        SalesRecordJpaEntity jpaEntity = salesRecordJpaMapper.toJpaEntity(salesRecord);
        SalesRecordJpaEntity savedEntity = salesRecordJpaRepository.save(jpaEntity);
        return salesRecordJpaMapper.toDomainObject(savedEntity);
    }
    
    @Override
    public Optional<SalesRecord> findById(String salesRecordId) {
        return salesRecordJpaRepository.findById(salesRecordId)
                .map(salesRecordJpaMapper::toDomainObject);
    }
    
    @Override
    public List<SalesRecord> findByOrderId(String orderId) {
        List<SalesRecordJpaEntity> jpaEntities = salesRecordJpaRepository.findByOrderId(orderId);
        return salesRecordJpaMapper.toDomainObjectList(jpaEntities);
    }
    
    @Override
    public List<SalesRecord> findByCustomerId(String customerId) {
        List<SalesRecordJpaEntity> jpaEntities = salesRecordJpaRepository.findByCustomerId(customerId);
        return salesRecordJpaMapper.toDomainObjectList(jpaEntities);
    }
    
    @Override
    public List<SalesRecord> findByProductId(String productId) {
        List<SalesRecordJpaEntity> jpaEntities = salesRecordJpaRepository.findByProductId(productId);
        return salesRecordJpaMapper.toDomainObjectList(jpaEntities);
    }
    
    @Override
    public List<SalesRecord> findByDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        List<SalesRecordJpaEntity> jpaEntities = salesRecordJpaRepository
                .findBySaleDateBetween(startDateTime, endDateTime);
        return salesRecordJpaMapper.toDomainObjectList(jpaEntities);
    }
    
    @Override
    public List<SalesRecord> findByCategory(String category) {
        List<SalesRecordJpaEntity> jpaEntities = salesRecordJpaRepository.findByCategory(category);
        return salesRecordJpaMapper.toDomainObjectList(jpaEntities);
    }
    
    @Override
    public List<SalesRecord> findByChannel(SalesChannel channel) {
        List<SalesRecordJpaEntity> jpaEntities = salesRecordJpaRepository.findByChannel(channel);
        return salesRecordJpaMapper.toDomainObjectList(jpaEntities);
    }
    
    @Override
    public List<SalesRecord> findByRegion(String region) {
        List<SalesRecordJpaEntity> jpaEntities = salesRecordJpaRepository.findByRegion(region);
        return salesRecordJpaMapper.toDomainObjectList(jpaEntities);
    }
    
    @Override
    public List<SalesRecord> findHighValueSalesByDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        BigDecimal minAmount = new BigDecimal("10000"); // 高價值銷售的門檻
        
        List<SalesRecordJpaEntity> jpaEntities = salesRecordJpaRepository
                .findHighValueSalesByDateRange(startDateTime, endDateTime, minAmount);
        return salesRecordJpaMapper.toDomainObjectList(jpaEntities);
    }
    
    @Override
    public List<SalesRecord> findPromotionalSalesByDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        List<SalesRecordJpaEntity> jpaEntities = salesRecordJpaRepository
                .findPromotionalSalesByDateRange(startDateTime, endDateTime);
        return salesRecordJpaMapper.toDomainObjectList(jpaEntities);
    }
    
    @Override
    public List<SalesRecord> findAll() {
        List<SalesRecordJpaEntity> jpaEntities = salesRecordJpaRepository.findAll();
        return salesRecordJpaMapper.toDomainObjectList(jpaEntities);
    }
    
    @Override
    public void deleteById(String salesRecordId) {
        salesRecordJpaRepository.deleteById(salesRecordId);
    }
    
    @Override
    public boolean existsById(String salesRecordId) {
        return salesRecordJpaRepository.existsById(salesRecordId);
    }
    
    @Override
    public boolean existsByOrderId(String orderId) {
        return salesRecordJpaRepository.existsByOrderId(orderId);
    }
    
    // 額外的查詢方法
    
    /**
     * 根據客戶ID和日期範圍查詢銷售記錄
     */
    public List<SalesRecord> findByCustomerIdAndDateRange(String customerId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        List<SalesRecordJpaEntity> jpaEntities = salesRecordJpaRepository
                .findByCustomerIdAndDateRange(customerId, startDateTime, endDateTime);
        return salesRecordJpaMapper.toDomainObjectList(jpaEntities);
    }
    
    /**
     * 根據分類和日期範圍查詢銷售記錄
     */
    public List<SalesRecord> findByCategoryAndDateRange(String category, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        List<SalesRecordJpaEntity> jpaEntities = salesRecordJpaRepository
                .findByCategoryAndDateRange(category, startDateTime, endDateTime);
        return salesRecordJpaMapper.toDomainObjectList(jpaEntities);
    }
    
    /**
     * 根據通道和日期範圍查詢銷售記錄
     */
    public List<SalesRecord> findByChannelAndDateRange(SalesChannel channel, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        List<SalesRecordJpaEntity> jpaEntities = salesRecordJpaRepository
                .findByChannelAndDateRange(channel, startDateTime, endDateTime);
        return salesRecordJpaMapper.toDomainObjectList(jpaEntities);
    }
    
    /**
     * 統計指定期間的總收入
     */
    public BigDecimal sumTotalAmountByDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        return salesRecordJpaRepository.sumTotalAmountByDateRange(startDateTime, endDateTime);
    }
    
    /**
     * 統計指定期間的總銷量
     */
    public Integer sumQuantityByDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        return salesRecordJpaRepository.sumQuantityByDateRange(startDateTime, endDateTime);
    }
}