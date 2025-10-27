package com.ecommerce.logistics.infrastructure.adapter.web;

import com.ecommerce.logistics.application.dto.*;
import com.ecommerce.logistics.application.port.in.AddressValidationUseCase;
import com.ecommerce.logistics.application.port.in.DeliveryManagementUseCase;
import com.ecommerce.logistics.domain.model.DeliveryStatus;
import com.ecommerce.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 配送控制器
 * 遵循 SRP 原則 - 只負責HTTP請求處理和回應
 * 遵循 DIP 原則 - 依賴使用案例介面而非具體實作
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/deliveries")
@RequiredArgsConstructor
public class DeliveryController {
    
    private final DeliveryManagementUseCase deliveryManagementUseCase;
    private final AddressValidationUseCase addressValidationUseCase;
    
    /**
     * 建立配送請求
     */
    @PostMapping
    public ResponseEntity<ApiResponse<DeliveryDto>> createDelivery(
            @Valid @RequestBody CreateDeliveryRequest request) {
        log.info("收到建立配送請求 - 訂單ID: {}", request.getOrderId());
        
        DeliveryDto delivery = deliveryManagementUseCase.createDeliveryRequest(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("配送請求建立成功", delivery));
    }
    
    /**
     * 根據配送ID查詢配送資訊
     */
    @GetMapping("/{deliveryId}")
    public ResponseEntity<ApiResponse<DeliveryDto>> getDeliveryById(
            @PathVariable String deliveryId) {
        log.debug("查詢配送資訊 - 配送ID: {}", deliveryId);
        
        DeliveryDto delivery = deliveryManagementUseCase.getDeliveryById(deliveryId);
        
        return ResponseEntity.ok(ApiResponse.success(delivery));
    }
    
    /**
     * 根據訂單ID查詢配送資訊
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<DeliveryDto>> getDeliveryByOrderId(
            @PathVariable String orderId) {
        log.debug("查詢配送資訊 - 訂單ID: {}", orderId);
        
        DeliveryDto delivery = deliveryManagementUseCase.getDeliveryByOrderId(orderId);
        
        return ResponseEntity.ok(ApiResponse.success(delivery));
    }
    
    /**
     * 根據追蹤號碼查詢配送資訊
     */
    @GetMapping("/tracking/{trackingNumber}")
    public ResponseEntity<ApiResponse<DeliveryDto>> getDeliveryByTrackingNumber(
            @PathVariable String trackingNumber) {
        log.debug("查詢配送資訊 - 追蹤號碼: {}", trackingNumber);
        
        DeliveryDto delivery = deliveryManagementUseCase.getDeliveryByTrackingNumber(trackingNumber);
        
        return ResponseEntity.ok(ApiResponse.success(delivery));
    }
    
    /**
     * 根據客戶ID查詢配送列表
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<List<DeliveryDto>>> getDeliveriesByCustomerId(
            @PathVariable String customerId) {
        log.debug("查詢客戶配送列表 - 客戶ID: {}", customerId);
        
        List<DeliveryDto> deliveries = deliveryManagementUseCase.getDeliveriesByCustomerId(customerId);
        
        return ResponseEntity.ok(ApiResponse.success(deliveries));
    }
    
    /**
     * 根據狀態查詢配送列表
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<DeliveryDto>>> getDeliveriesByStatus(
            @PathVariable DeliveryStatus status) {
        log.debug("查詢配送列表 - 狀態: {}", status);
        
        List<DeliveryDto> deliveries = deliveryManagementUseCase.getDeliveriesByStatus(status);
        
        return ResponseEntity.ok(ApiResponse.success(deliveries));
    }
    
    /**
     * 更新配送狀態
     */
    @PutMapping("/{deliveryId}/status")
    public ResponseEntity<ApiResponse<DeliveryDto>> updateDeliveryStatus(
            @PathVariable String deliveryId,
            @Valid @RequestBody UpdateDeliveryStatusRequest request) {
        log.info("更新配送狀態 - 配送ID: {}, 新狀態: {}", deliveryId, request.getStatus());
        
        // 設定配送ID
        request.setDeliveryId(deliveryId);
        
        DeliveryDto delivery = deliveryManagementUseCase.updateDeliveryStatus(request);
        
        return ResponseEntity.ok(ApiResponse.success("配送狀態更新成功", delivery));
    }
    
    /**
     * 更新配送地址
     */
    @PutMapping("/{deliveryId}/address")
    public ResponseEntity<ApiResponse<DeliveryDto>> updateDeliveryAddress(
            @PathVariable String deliveryId,
            @Valid @RequestBody UpdateDeliveryAddressRequest request) {
        log.info("更新配送地址 - 配送ID: {}", deliveryId);
        
        // 設定配送ID
        request.setDeliveryId(deliveryId);
        
        DeliveryDto delivery = deliveryManagementUseCase.updateDeliveryAddress(request);
        
        return ResponseEntity.ok(ApiResponse.success("配送地址更新成功", delivery));
    }
    
    /**
     * 開始配送
     */
    @PostMapping("/{deliveryId}/start")
    public ResponseEntity<ApiResponse<DeliveryDto>> startDelivery(
            @PathVariable String deliveryId,
            @RequestParam String trackingNumber) {
        log.info("開始配送 - 配送ID: {}, 追蹤號碼: {}", deliveryId, trackingNumber);
        
        DeliveryDto delivery = deliveryManagementUseCase.startDelivery(deliveryId, trackingNumber);
        
        return ResponseEntity.ok(ApiResponse.success("配送已開始", delivery));
    }
    
    /**
     * 完成配送
     */
    @PostMapping("/{deliveryId}/complete")
    public ResponseEntity<ApiResponse<DeliveryDto>> completeDelivery(
            @PathVariable String deliveryId) {
        log.info("完成配送 - 配送ID: {}", deliveryId);
        
        DeliveryDto delivery = deliveryManagementUseCase.completeDelivery(deliveryId);
        
        return ResponseEntity.ok(ApiResponse.success("配送已完成", delivery));
    }
    
    /**
     * 取消配送
     */
    @PostMapping("/{deliveryId}/cancel")
    public ResponseEntity<ApiResponse<DeliveryDto>> cancelDelivery(
            @PathVariable String deliveryId) {
        log.info("取消配送 - 配送ID: {}", deliveryId);
        
        DeliveryDto delivery = deliveryManagementUseCase.cancelDelivery(deliveryId);
        
        return ResponseEntity.ok(ApiResponse.success("配送已取消", delivery));
    }
    
    /**
     * 標記配送失敗
     */
    @PostMapping("/{deliveryId}/fail")
    public ResponseEntity<ApiResponse<DeliveryDto>> markDeliveryAsFailed(
            @PathVariable String deliveryId,
            @RequestParam String failureReason) {
        log.info("標記配送失敗 - 配送ID: {}, 失敗原因: {}", deliveryId, failureReason);
        
        DeliveryDto delivery = deliveryManagementUseCase.markDeliveryAsFailed(deliveryId, failureReason);
        
        return ResponseEntity.ok(ApiResponse.success(delivery, "配送已標記為失敗"));
    }
    
    /**
     * 驗證地址
     */
    @PostMapping("/validate-address")
    public ResponseEntity<ApiResponse<AddressValidationResult>> validateAddress(
            @Valid @RequestBody AddressDto address) {
        log.debug("驗證地址 - {}", address.getFullAddress());
        
        AddressValidationResult result = addressValidationUseCase.validateAddress(address);
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    /**
     * 驗證台北地址
     */
    @PostMapping("/validate-taipei-address")
    public ResponseEntity<ApiResponse<AddressValidationResult>> validateTaipeiAddress(
            @Valid @RequestBody AddressDto address) {
        log.debug("驗證台北地址 - {}", address.getFullAddress());
        
        AddressValidationResult result = addressValidationUseCase.validateTaipeiAddress(address);
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    /**
     * 標準化地址
     */
    @PostMapping("/normalize-address")
    public ResponseEntity<ApiResponse<AddressDto>> normalizeAddress(
            @Valid @RequestBody AddressDto address) {
        log.debug("標準化地址 - {}", address.getFullAddress());
        
        AddressDto normalizedAddress = addressValidationUseCase.normalizeAddress(address);
        
        return ResponseEntity.ok(ApiResponse.success(normalizedAddress));
    }
    
    /**
     * 檢查地址是否在配送範圍內
     */
    @PostMapping("/check-delivery-range")
    public ResponseEntity<ApiResponse<Boolean>> checkDeliveryRange(
            @Valid @RequestBody AddressDto address) {
        log.debug("檢查配送範圍 - {}", address.getFullAddress());
        
        boolean inRange = addressValidationUseCase.isAddressInDeliveryRange(address);
        
        return ResponseEntity.ok(ApiResponse.success(inRange));
    }
}