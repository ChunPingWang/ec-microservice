package com.ecommerce.logistics.application.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 更新配送地址請求DTO
 * 遵循 SRP 原則 - 只負責配送地址更新的資料傳輸
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDeliveryAddressRequest {
    
    @NotBlank(message = "配送ID不能為空")
    private String deliveryId;
    
    @Valid
    @NotNull(message = "新配送地址不能為空")
    private AddressDto newAddress;
}