package com.ecommerce.logistics.application.port.in;

import com.ecommerce.logistics.application.dto.CreateDeliveryRequest;
import com.ecommerce.logistics.application.dto.DeliveryDto;
import com.ecommerce.logistics.application.dto.UpdateDeliveryStatusRequest;
import com.ecommerce.logistics.application.dto.UpdateDeliveryAddressRequest;
import com.ecommerce.logistics.domain.model.DeliveryStatus;
import com.ecommerce.common.architecture.UseCase;

import java.util.List;

/**
 * 配送管理使用案例介面
 * 遵循 ISP 原則 - 只定義配送管理相關的操作
 * 遵循 DIP 原則 - 定義應用層的輸入埠
 */
@UseCase
public interface DeliveryManagementUseCase {
    
    /**
     * 建立配送請求
     */
    DeliveryDto createDeliveryRequest(CreateDeliveryRequest request);
    
    /**
     * 根據配送ID查詢配送資訊
     */
    DeliveryDto getDeliveryById(String deliveryId);
    
    /**
     * 根據訂單ID查詢配送資訊
     */
    DeliveryDto getDeliveryByOrderId(String orderId);
    
    /**
     * 根據追蹤號碼查詢配送資訊
     */
    DeliveryDto getDeliveryByTrackingNumber(String trackingNumber);
    
    /**
     * 根據客戶ID查詢配送列表
     */
    List<DeliveryDto> getDeliveriesByCustomerId(String customerId);
    
    /**
     * 根據狀態查詢配送列表
     */
    List<DeliveryDto> getDeliveriesByStatus(DeliveryStatus status);
    
    /**
     * 更新配送狀態
     */
    DeliveryDto updateDeliveryStatus(UpdateDeliveryStatusRequest request);
    
    /**
     * 更新配送地址
     */
    DeliveryDto updateDeliveryAddress(UpdateDeliveryAddressRequest request);
    
    /**
     * 開始配送
     */
    DeliveryDto startDelivery(String deliveryId, String trackingNumber);
    
    /**
     * 完成配送
     */
    DeliveryDto completeDelivery(String deliveryId);
    
    /**
     * 取消配送
     */
    DeliveryDto cancelDelivery(String deliveryId);
    
    /**
     * 標記配送失敗
     */
    DeliveryDto markDeliveryAsFailed(String deliveryId, String failureReason);
}