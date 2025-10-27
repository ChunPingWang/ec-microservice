package com.ecommerce.logistics.application.port.out;

import com.ecommerce.logistics.domain.model.Address;

/**
 * 地址驗證輸出埠
 * 遵循 DIP 原則 - 定義應用層需要的外部地址驗證服務抽象
 * 遵循 ISP 原則 - 只定義地址驗證相關的操作
 */
public interface AddressValidationPort {
    
    /**
     * 驗證地址是否存在
     */
    boolean validateAddressExists(Address address);
    
    /**
     * 驗證台北地址格式
     */
    boolean validateTaipeiAddressFormat(Address address);
    
    /**
     * 標準化地址格式
     */
    Address normalizeAddress(Address address);
    
    /**
     * 檢查地址是否在配送範圍內
     */
    boolean isInDeliveryRange(Address address);
    
    /**
     * 取得地址的經緯度座標
     */
    String getAddressCoordinates(Address address);
}