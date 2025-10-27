package com.ecommerce.logistics.application.dto;

import com.ecommerce.logistics.domain.model.DeliveryType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 建立配送請求DTO
 * 遵循 SRP 原則 - 只負責建立配送請求的資料傳輸
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateDeliveryRequest {
    
    @NotBlank(message = "訂單ID不能為空")
    private String orderId;
    
    @NotBlank(message = "客戶ID不能為空")
    private String customerId;
    
    @Valid
    @NotNull(message = "配送地址不能為空")
    private AddressDto deliveryAddress;
    
    @NotNull(message = "配送類型不能為空")
    private DeliveryType deliveryType;
}