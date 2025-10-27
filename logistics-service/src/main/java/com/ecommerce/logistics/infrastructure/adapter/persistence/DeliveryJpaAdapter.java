package com.ecommerce.logistics.infrastructure.adapter.persistence;

import com.ecommerce.logistics.application.port.out.DeliveryPersistencePort;
import com.ecommerce.logistics.domain.model.DeliveryRequest;
import com.ecommerce.logistics.domain.model.DeliveryStatus;
import com.ecommerce.logistics.infrastructure.adapter.persistence.entity.DeliveryJpaEntity;
import com.ecommerce.logistics.infrastructure.adapter.persistence.mapper.DeliveryJpaMapper;
import com.ecommerce.logistics.infrastructure.adapter.persistence.repository.DeliveryJpaRepository;
import com.ecommerce.common.architecture.PersistenceAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 配送JPA適配器
 * 遵循 SRP 原則 - 只負責配送資料的持久化操作
 * 遵循 DIP 原則 - 實作應用層定義的輸出埠介面
 */
@Slf4j
@Component
@PersistenceAdapter
@RequiredArgsConstructor
public class DeliveryJpaAdapter implements DeliveryPersistencePort {
    
    private final DeliveryJpaRepository deliveryJpaRepository;
    private final DeliveryJpaMapper deliveryJpaMapper;
    
    @Override
    public DeliveryRequest save(DeliveryRequest deliveryRequest) {
        log.debug("儲存配送請求 - 配送ID: {}", deliveryRequest.getDeliveryId());
        
        DeliveryJpaEntity entity = deliveryJpaMapper.toEntity(deliveryRequest);
        DeliveryJpaEntity savedEntity = deliveryJpaRepository.save(entity);
        
        log.debug("配送請求已儲存 - 配送ID: {}", savedEntity.getDeliveryId());
        return deliveryJpaMapper.toDomain(savedEntity);
    }
    
    @Override
    public Optional<DeliveryRequest> findById(String deliveryId) {
        log.debug("查詢配送請求 - 配送ID: {}", deliveryId);
        
        return deliveryJpaRepository.findById(deliveryId)
            .map(deliveryJpaMapper::toDomain);
    }
    
    @Override
    public Optional<DeliveryRequest> findByOrderId(String orderId) {
        log.debug("查詢配送請求 - 訂單ID: {}", orderId);
        
        return deliveryJpaRepository.findByOrderId(orderId)
            .map(deliveryJpaMapper::toDomain);
    }
    
    @Override
    public List<DeliveryRequest> findByCustomerId(String customerId) {
        log.debug("查詢客戶配送列表 - 客戶ID: {}", customerId);
        
        return deliveryJpaRepository.findByCustomerId(customerId)
            .stream()
            .map(deliveryJpaMapper::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<DeliveryRequest> findByStatus(DeliveryStatus status) {
        log.debug("查詢配送列表 - 狀態: {}", status);
        
        return deliveryJpaRepository.findByStatus(status)
            .stream()
            .map(deliveryJpaMapper::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public Optional<DeliveryRequest> findByTrackingNumber(String trackingNumber) {
        log.debug("查詢配送請求 - 追蹤號碼: {}", trackingNumber);
        
        return deliveryJpaRepository.findByTrackingNumber(trackingNumber)
            .map(deliveryJpaMapper::toDomain);
    }
    
    @Override
    public void deleteById(String deliveryId) {
        log.debug("刪除配送請求 - 配送ID: {}", deliveryId);
        
        deliveryJpaRepository.deleteById(deliveryId);
        
        log.debug("配送請求已刪除 - 配送ID: {}", deliveryId);
    }
    
    @Override
    public boolean existsById(String deliveryId) {
        log.debug("檢查配送請求是否存在 - 配送ID: {}", deliveryId);
        
        return deliveryJpaRepository.existsById(deliveryId);
    }
    
    @Override
    public boolean existsByOrderId(String orderId) {
        log.debug("檢查訂單配送請求是否存在 - 訂單ID: {}", orderId);
        
        return deliveryJpaRepository.existsByOrderId(orderId);
    }
}