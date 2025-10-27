package com.ecommerce.logistics.application.port.in;

import com.ecommerce.logistics.application.dto.AddressDto;
import com.ecommerce.logistics.application.dto.AddressValidationResult;
import com.ecommerce.common.architecture.UseCase;

/**
 * 地址驗證使用案例介面
 * 遵循 ISP 原則 - 只定義地址驗證相關的操作
 * 遵循 SRP 原則 - 只負責地址驗證功能
 */
@UseCase
public interface AddressValidationUseCase {
    
    /**
     * 驗證地址格式
     */
    AddressValidationResult validateAddress(AddressDto address);
    
    /**
     * 驗證台北地址
     */
    AddressValidationResult validateTaipeiAddress(AddressDto address);
    
    /**
     * 標準化地址格式
     */
    AddressDto normalizeAddress(AddressDto address);
    
    /**
     * 檢查地址是否在配送範圍內
     */
    boolean isAddressInDeliveryRange(AddressDto address);
}