package com.ecommerce.logistics.application.dto;

import com.ecommerce.logistics.domain.model.DeliveryStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 更新配送狀態請求DTO
 * 遵循 SRP 原則 - 只負責配送狀態更新的資料傳輸
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDeliveryStatusRequest {
    
    @NotBlank(message = "配送ID不能為空")
    private String deliveryId;
    
    @NotNull(message = "配送狀態不能為空")
    private DeliveryStatus status;
    
    @Size(max = 500, message = "備註長度不能超過500個字元")
    private String notes;
    
    @Size(max = 100, message = "追蹤號碼長度不能超過100個字元")
    private String trackingNumber;
}