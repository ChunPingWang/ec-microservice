package com.ecommerce.product.infrastructure.adapter.persistence;

import com.ecommerce.common.architecture.Adapter;
import com.ecommerce.product.application.port.out.StockPersistencePort;
import com.ecommerce.product.domain.model.Stock;
import com.ecommerce.product.infrastructure.adapter.persistence.entity.StockJpaEntity;
import com.ecommerce.product.infrastructure.adapter.persistence.mapper.StockJpaMapper;
import com.ecommerce.product.infrastructure.adapter.persistence.repository.StockJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Stock JPA Adapter
 * Implements StockPersistencePort using JPA for data persistence
 * Follows DIP principle by implementing the output port interface
 */
@Adapter
@Component
@Transactional
public class StockJpaAdapter implements StockPersistencePort {
    
    private final StockJpaRepository stockJpaRepository;
    
    public StockJpaAdapter(StockJpaRepository stockJpaRepository) {
        this.stockJpaRepository = stockJpaRepository;
    }
    
    @Override
    public Stock save(Stock stock) {
        StockJpaEntity jpaEntity = StockJpaMapper.toJpaEntity(stock);
        StockJpaEntity savedEntity = stockJpaRepository.save(jpaEntity);
        return StockJpaMapper.toDomainEntity(savedEntity);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Stock> findById(String stockId) {
        return stockJpaRepository.findByStockId(stockId)
                .map(StockJpaMapper::toDomainEntity);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Stock> findByProductId(String productId) {
        return stockJpaRepository.findByProductId(productId)
                .map(StockJpaMapper::toDomainEntity);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Stock> findByWarehouseLocation(String warehouseLocation) {
        List<StockJpaEntity> jpaEntities = stockJpaRepository.findByWarehouseLocation(warehouseLocation);
        return StockJpaMapper.toDomainEntityList(jpaEntities);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Stock> findLowStockItems() {
        List<StockJpaEntity> jpaEntities = stockJpaRepository.findLowStockItems();
        return StockJpaMapper.toDomainEntityList(jpaEntities);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Stock> findOutOfStockItems() {
        List<StockJpaEntity> jpaEntities = stockJpaRepository.findOutOfStockItems();
        return StockJpaMapper.toDomainEntityList(jpaEntities);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Stock> findByAvailableQuantityGreaterThan(Integer minimumAvailable) {
        List<StockJpaEntity> jpaEntities = stockJpaRepository.findByAvailableQuantityGreaterThan(minimumAvailable);
        return StockJpaMapper.toDomainEntityList(jpaEntities);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Stock> findStocksWithReservations() {
        List<StockJpaEntity> jpaEntities = stockJpaRepository.findStocksWithReservations();
        return StockJpaMapper.toDomainEntityList(jpaEntities);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Stock> findByQuantityBetween(Integer minQuantity, Integer maxQuantity) {
        List<StockJpaEntity> jpaEntities = stockJpaRepository.findByQuantityBetween(minQuantity, maxQuantity);
        return StockJpaMapper.toDomainEntityList(jpaEntities);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Stock> findStocksNeedingRestock(LocalDateTime lastRestockBefore) {
        List<StockJpaEntity> jpaEntities = stockJpaRepository.findStocksNeedingRestock(lastRestockBefore);
        return StockJpaMapper.toDomainEntityList(jpaEntities);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Stock> findSlowMovingStock(LocalDateTime lastSaleBefore) {
        List<StockJpaEntity> jpaEntities = stockJpaRepository.findSlowMovingStock(lastSaleBefore);
        return StockJpaMapper.toDomainEntityList(jpaEntities);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Stock> findStocksNearCapacity(Integer utilizationThreshold) {
        List<StockJpaEntity> jpaEntities = stockJpaRepository.findStocksNearCapacity(utilizationThreshold);
        return StockJpaMapper.toDomainEntityList(jpaEntities);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Stock> findByProductIdIn(List<String> productIds) {
        List<StockJpaEntity> jpaEntities = stockJpaRepository.findByProductIdIn(productIds);
        return StockJpaMapper.toDomainEntityList(jpaEntities);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsByProductId(String productId) {
        return stockJpaRepository.existsByProductId(productId);
    }
    
    @Override
    public void deleteById(String stockId) {
        stockJpaRepository.deleteByStockId(stockId);
    }
    
    @Override
    public void deleteByProductId(String productId) {
        stockJpaRepository.deleteByProductId(productId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long countByWarehouseLocation(String warehouseLocation) {
        return stockJpaRepository.countByWarehouseLocation(warehouseLocation);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long countLowStockItems() {
        return stockJpaRepository.countLowStockItems();
    }
    
    @Override
    @Transactional(readOnly = true)
    public long countOutOfStockItems() {
        return stockJpaRepository.countOutOfStockItems();
    }
}