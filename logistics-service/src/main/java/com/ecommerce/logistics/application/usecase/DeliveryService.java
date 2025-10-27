package com.ecommerce.logistics.application.usecase;

import com.ecommerce.logistics.application.dto.*;
import com.ecommerce.logistics.application.mapper.DeliveryMapper;
import com.ecommerce.logistics.application.port.in.DeliveryManagementUseCase;
import com.ecommerce.logistics.application.port.out.DeliveryEventPort;
import com.ecommerce.logistics.application.port.out.DeliveryPersistencePort;
import com.ecommerce.logistics.domain.event.*;
import com.ecommerce.logistics.domain.exception.DeliveryNotFoundException;
import com.ecommerce.logistics.domain.model.Address;
import com.ecommerce.logistics.domain.model.DeliveryRequest;
import com.ecommerce.logistics.domain.model.DeliveryStatus;
import com.ecommerce.logistics.domain.service.DeliveryDomainService;
import com.ecommerce.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 配送服務實作
 * 遵循 SRP 原則 - 只負責配送管理的業務流程協調
 * 遵循 DIP 原則 - 依賴抽象介面而非具體實作
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DeliveryService implements DeliveryManagementUseCase {
    
    private final DeliveryDomainService deliveryDomainService;
    private final DeliveryPersistencePort deliveryPersistencePort;
    private final DeliveryEventPort deliveryEventPort;
    private final DeliveryMapper deliveryMapper;
    
    @Override
    public DeliveryDto createDeliveryRequest(CreateDeliveryRequest request) {
        log.info("建立配送請求 - 訂單ID: {}, 客戶ID: {}", request.getOrderId(), request.getCustomerId());
        
        // 檢查訂單是否已有配送請求
        if (deliveryPersistencePort.existsByOrderId(request.getOrderId())) {
            throw new BusinessException("DUPLICATE_ORDER_DELIVERY", "訂單已存在配送請求: " + request.getOrderId());
        }
        
        // 轉換地址DTO為領域物件
        Address deliveryAddress = deliveryMapper.toAddressDomain(request.getDeliveryAddress());
        
        // 使用領域服務建立配送請求
        DeliveryRequest deliveryRequest = deliveryDomainService.createDeliveryRequest(
            request.getOrderId(),
            request.getCustomerId(),
            deliveryAddress,
            request.getDeliveryType()
        );
        
        // 儲存配送請求
        DeliveryRequest savedDelivery = deliveryPersistencePort.save(deliveryRequest);
        
        // 發布配送建立事件
        DeliveryCreatedEvent event = new DeliveryCreatedEvent(
            savedDelivery.getDeliveryId(),
            savedDelivery.getOrderId(),
            savedDelivery.getCustomerId(),
            savedDelivery.getDeliveryAddress().getFullAddress(),
            savedDelivery.getDeliveryType(),
            savedDelivery.getDeliveryFee(),
            savedDelivery.getEstimatedDeliveryDate()
        );
        deliveryEventPort.publishDeliveryCreatedEvent(event);
        
        log.info("配送請求已建立 - 配送ID: {}", savedDelivery.getDeliveryId());
        return deliveryMapper.toDto(savedDelivery);
    }
    
    @Override
    @Transactional(readOnly = true)
    public DeliveryDto getDeliveryById(String deliveryId) {
        log.debug("查詢配送資訊 - 配送ID: {}", deliveryId);
        
        DeliveryRequest deliveryRequest = deliveryPersistencePort.findById(deliveryId)
            .orElseThrow(() -> new DeliveryNotFoundException(deliveryId));
        
        return deliveryMapper.toDto(deliveryRequest);
    }
    
    @Override
    @Transactional(readOnly = true)
    public DeliveryDto getDeliveryByOrderId(String orderId) {
        log.debug("查詢配送資訊 - 訂單ID: {}", orderId);
        
        DeliveryRequest deliveryRequest = deliveryPersistencePort.findByOrderId(orderId)
            .orElseThrow(() -> new DeliveryNotFoundException("訂單配送請求不存在: " + orderId));
        
        return deliveryMapper.toDto(deliveryRequest);
    }
    
    @Override
    @Transactional(readOnly = true)
    public DeliveryDto getDeliveryByTrackingNumber(String trackingNumber) {
        log.debug("查詢配送資訊 - 追蹤號碼: {}", trackingNumber);
        
        DeliveryRequest deliveryRequest = deliveryPersistencePort.findByTrackingNumber(trackingNumber)
            .orElseThrow(() -> new DeliveryNotFoundException("追蹤號碼配送請求不存在: " + trackingNumber));
        
        return deliveryMapper.toDto(deliveryRequest);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<DeliveryDto> getDeliveriesByCustomerId(String customerId) {
        log.debug("查詢客戶配送列表 - 客戶ID: {}", customerId);
        
        List<DeliveryRequest> deliveries = deliveryPersistencePort.findByCustomerId(customerId);
        return deliveryMapper.toDtoList(deliveries);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<DeliveryDto> getDeliveriesByStatus(DeliveryStatus status) {
        log.debug("查詢配送列表 - 狀態: {}", status);
        
        List<DeliveryRequest> deliveries = deliveryPersistencePort.findByStatus(status);
        return deliveryMapper.toDtoList(deliveries);
    }
    
    @Override
    public DeliveryDto updateDeliveryStatus(UpdateDeliveryStatusRequest request) {
        log.info("更新配送狀態 - 配送ID: {}, 新狀態: {}", request.getDeliveryId(), request.getStatus());
        
        DeliveryRequest deliveryRequest = deliveryPersistencePort.findById(request.getDeliveryId())
            .orElseThrow(() -> new DeliveryNotFoundException(request.getDeliveryId()));
        
        DeliveryStatus oldStatus = deliveryRequest.getStatus();
        
        // 使用領域服務更新狀態
        deliveryDomainService.updateDeliveryStatus(deliveryRequest, request.getStatus(), request.getNotes());
        
        // 如果有追蹤號碼，設定追蹤號碼
        if (request.getTrackingNumber() != null && !request.getTrackingNumber().trim().isEmpty()) {
            deliveryRequest.setTrackingNumber(request.getTrackingNumber());
        }
        
        // 儲存更新
        DeliveryRequest updatedDelivery = deliveryPersistencePort.save(deliveryRequest);
        
        // 發布狀態更新事件
        DeliveryStatusUpdatedEvent event = new DeliveryStatusUpdatedEvent(
            updatedDelivery.getDeliveryId(),
            updatedDelivery.getOrderId(),
            updatedDelivery.getCustomerId(),
            oldStatus,
            updatedDelivery.getStatus(),
            updatedDelivery.getTrackingNumber()
        );
        deliveryEventPort.publishDeliveryStatusUpdatedEvent(event);
        
        // 如果是完成狀態，發布完成事件
        if (updatedDelivery.getStatus() == DeliveryStatus.DELIVERED) {
            DeliveryCompletedEvent completedEvent = new DeliveryCompletedEvent(
                updatedDelivery.getDeliveryId(),
                updatedDelivery.getOrderId(),
                updatedDelivery.getCustomerId(),
                updatedDelivery.getActualDeliveryDate(),
                updatedDelivery.getTrackingNumber()
            );
            deliveryEventPort.publishDeliveryCompletedEvent(completedEvent);
        }
        
        log.info("配送狀態已更新 - 配送ID: {}", updatedDelivery.getDeliveryId());
        return deliveryMapper.toDto(updatedDelivery);
    }
    
    @Override
    public DeliveryDto updateDeliveryAddress(UpdateDeliveryAddressRequest request) {
        log.info("更新配送地址 - 配送ID: {}", request.getDeliveryId());
        
        DeliveryRequest deliveryRequest = deliveryPersistencePort.findById(request.getDeliveryId())
            .orElseThrow(() -> new DeliveryNotFoundException(request.getDeliveryId()));
        
        // 轉換新地址
        Address newAddress = deliveryMapper.toAddressDomain(request.getNewAddress());
        
        // 使用領域服務驗證地址更新
        deliveryDomainService.validateAddressUpdate(deliveryRequest, newAddress);
        
        // 更新地址
        deliveryRequest.updateDeliveryAddress(newAddress);
        
        // 儲存更新
        DeliveryRequest updatedDelivery = deliveryPersistencePort.save(deliveryRequest);
        
        log.info("配送地址已更新 - 配送ID: {}", updatedDelivery.getDeliveryId());
        return deliveryMapper.toDto(updatedDelivery);
    }
    
    @Override
    public DeliveryDto startDelivery(String deliveryId, String trackingNumber) {
        log.info("開始配送 - 配送ID: {}, 追蹤號碼: {}", deliveryId, trackingNumber);
        
        DeliveryRequest deliveryRequest = deliveryPersistencePort.findById(deliveryId)
            .orElseThrow(() -> new DeliveryNotFoundException(deliveryId));
        
        // 使用領域服務開始配送
        deliveryDomainService.startDelivery(deliveryRequest, trackingNumber);
        
        // 儲存更新
        DeliveryRequest updatedDelivery = deliveryPersistencePort.save(deliveryRequest);
        
        // 發布狀態更新事件
        DeliveryStatusUpdatedEvent event = new DeliveryStatusUpdatedEvent(
            updatedDelivery.getDeliveryId(),
            updatedDelivery.getOrderId(),
            updatedDelivery.getCustomerId(),
            DeliveryStatus.PENDING,
            DeliveryStatus.IN_TRANSIT,
            trackingNumber
        );
        deliveryEventPort.publishDeliveryStatusUpdatedEvent(event);
        
        log.info("配送已開始 - 配送ID: {}", updatedDelivery.getDeliveryId());
        return deliveryMapper.toDto(updatedDelivery);
    }
    
    @Override
    public DeliveryDto completeDelivery(String deliveryId) {
        log.info("完成配送 - 配送ID: {}", deliveryId);
        
        DeliveryRequest deliveryRequest = deliveryPersistencePort.findById(deliveryId)
            .orElseThrow(() -> new DeliveryNotFoundException(deliveryId));
        
        // 使用領域服務完成配送
        deliveryDomainService.completeDelivery(deliveryRequest);
        
        // 儲存更新
        DeliveryRequest updatedDelivery = deliveryPersistencePort.save(deliveryRequest);
        
        // 發布完成事件
        DeliveryCompletedEvent event = new DeliveryCompletedEvent(
            updatedDelivery.getDeliveryId(),
            updatedDelivery.getOrderId(),
            updatedDelivery.getCustomerId(),
            updatedDelivery.getActualDeliveryDate(),
            updatedDelivery.getTrackingNumber()
        );
        deliveryEventPort.publishDeliveryCompletedEvent(event);
        
        log.info("配送已完成 - 配送ID: {}", updatedDelivery.getDeliveryId());
        return deliveryMapper.toDto(updatedDelivery);
    }
    
    @Override
    public DeliveryDto cancelDelivery(String deliveryId) {
        log.info("取消配送 - 配送ID: {}", deliveryId);
        
        DeliveryRequest deliveryRequest = deliveryPersistencePort.findById(deliveryId)
            .orElseThrow(() -> new DeliveryNotFoundException(deliveryId));
        
        // 取消配送
        deliveryRequest.cancel();
        
        // 儲存更新
        DeliveryRequest updatedDelivery = deliveryPersistencePort.save(deliveryRequest);
        
        // 發布狀態更新事件
        DeliveryStatusUpdatedEvent event = new DeliveryStatusUpdatedEvent(
            updatedDelivery.getDeliveryId(),
            updatedDelivery.getOrderId(),
            updatedDelivery.getCustomerId(),
            DeliveryStatus.PENDING,
            DeliveryStatus.CANCELLED,
            updatedDelivery.getTrackingNumber()
        );
        deliveryEventPort.publishDeliveryStatusUpdatedEvent(event);
        
        log.info("配送已取消 - 配送ID: {}", updatedDelivery.getDeliveryId());
        return deliveryMapper.toDto(updatedDelivery);
    }
    
    @Override
    public DeliveryDto markDeliveryAsFailed(String deliveryId, String failureReason) {
        log.info("標記配送失敗 - 配送ID: {}, 失敗原因: {}", deliveryId, failureReason);
        
        DeliveryRequest deliveryRequest = deliveryPersistencePort.findById(deliveryId)
            .orElseThrow(() -> new DeliveryNotFoundException(deliveryId));
        
        // 使用領域服務處理配送失敗
        deliveryDomainService.handleDeliveryFailure(deliveryRequest, failureReason);
        
        // 儲存更新
        DeliveryRequest updatedDelivery = deliveryPersistencePort.save(deliveryRequest);
        
        // 發布失敗事件
        DeliveryFailedEvent event = new DeliveryFailedEvent(
            updatedDelivery.getDeliveryId(),
            updatedDelivery.getOrderId(),
            updatedDelivery.getCustomerId(),
            failureReason,
            updatedDelivery.getTrackingNumber()
        );
        deliveryEventPort.publishDeliveryFailedEvent(event);
        
        log.info("配送已標記為失敗 - 配送ID: {}", updatedDelivery.getDeliveryId());
        return deliveryMapper.toDto(updatedDelivery);
    }
}