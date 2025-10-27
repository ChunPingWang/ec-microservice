package com.ecommerce.logistics.application.usecase;

import com.ecommerce.logistics.application.dto.AddressDto;
import com.ecommerce.logistics.application.dto.AddressValidationResult;
import com.ecommerce.logistics.application.mapper.DeliveryMapper;
import com.ecommerce.logistics.application.port.in.AddressValidationUseCase;
import com.ecommerce.logistics.application.port.out.AddressValidationPort;
import com.ecommerce.logistics.domain.exception.InvalidAddressException;
import com.ecommerce.logistics.domain.model.Address;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 地址服務實作
 * 遵循 SRP 原則 - 只負責地址驗證和處理的業務流程協調
 * 遵循 DIP 原則 - 依賴抽象介面而非具體實作
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AddressService implements AddressValidationUseCase {
    
    private final AddressValidationPort addressValidationPort;
    private final DeliveryMapper deliveryMapper;
    
    @Override
    public AddressValidationResult validateAddress(AddressDto addressDto) {
        log.debug("驗證地址 - {}", addressDto.getFullAddress());
        
        AddressValidationResult result = new AddressValidationResult(true);
        
        try {
            // 轉換為領域物件進行驗證
            Address address = deliveryMapper.toAddressDomain(addressDto);
            
            // 基本格式驗證（由領域物件的validate方法處理）
            address.validate();
            
            // 外部地址驗證
            if (!addressValidationPort.validateAddressExists(address)) {
                result.addError("地址不存在或無法驗證");
            }
            
            // 檢查配送範圍
            boolean inRange = addressValidationPort.isInDeliveryRange(address);
            result.setInDeliveryRange(inRange);
            
            if (!inRange) {
                result.addError("地址不在配送範圍內");
            }
            
            // 標準化地址
            Address normalizedAddress = addressValidationPort.normalizeAddress(address);
            result.setNormalizedAddress(deliveryMapper.toAddressDto(normalizedAddress));
            
            // 取得座標
            String coordinates = addressValidationPort.getAddressCoordinates(address);
            result.setCoordinates(coordinates);
            
            log.debug("地址驗證完成 - 結果: {}", result.isValid());
            
        } catch (InvalidAddressException e) {
            log.warn("地址格式驗證失敗: {}", e.getMessage());
            result.addError(e.getMessage());
        } catch (Exception e) {
            log.error("地址驗證過程發生錯誤", e);
            result.addError("地址驗證服務暫時無法使用");
        }
        
        return result;
    }
    
    @Override
    public AddressValidationResult validateTaipeiAddress(AddressDto addressDto) {
        log.debug("驗證台北地址 - {}", addressDto.getFullAddress());
        
        AddressValidationResult result = new AddressValidationResult(true);
        
        // 檢查是否為台北地址
        if (!addressDto.isTaipeiAddress()) {
            result.addError("不是台北市地址");
            return result;
        }
        
        try {
            // 轉換為領域物件
            Address address = deliveryMapper.toAddressDomain(addressDto);
            
            // 台北地址特殊驗證
            if (!addressValidationPort.validateTaipeiAddressFormat(address)) {
                result.addError("台北市地址格式不正確");
            }
            
            // 基本驗證
            address.validate();
            
            // 檢查配送範圍（台北市通常都在範圍內）
            result.setInDeliveryRange(true);
            
            // 標準化地址
            Address normalizedAddress = addressValidationPort.normalizeAddress(address);
            result.setNormalizedAddress(deliveryMapper.toAddressDto(normalizedAddress));
            
            log.debug("台北地址驗證完成 - 結果: {}", result.isValid());
            
        } catch (InvalidAddressException e) {
            log.warn("台北地址格式驗證失敗: {}", e.getMessage());
            result.addError(e.getMessage());
        } catch (Exception e) {
            log.error("台北地址驗證過程發生錯誤", e);
            result.addError("台北地址驗證服務暫時無法使用");
        }
        
        return result;
    }
    
    @Override
    public AddressDto normalizeAddress(AddressDto addressDto) {
        log.debug("標準化地址 - {}", addressDto.getFullAddress());
        
        try {
            // 轉換為領域物件
            Address address = deliveryMapper.toAddressDomain(addressDto);
            
            // 使用外部服務標準化地址
            Address normalizedAddress = addressValidationPort.normalizeAddress(address);
            
            AddressDto result = deliveryMapper.toAddressDto(normalizedAddress);
            log.debug("地址標準化完成 - {}", result.getFullAddress());
            
            return result;
            
        } catch (Exception e) {
            log.warn("地址標準化失敗，返回原地址: {}", e.getMessage());
            return addressDto;
        }
    }
    
    @Override
    public boolean isAddressInDeliveryRange(AddressDto addressDto) {
        log.debug("檢查配送範圍 - {}", addressDto.getFullAddress());
        
        try {
            // 轉換為領域物件
            Address address = deliveryMapper.toAddressDomain(addressDto);
            
            // 檢查配送範圍
            boolean inRange = addressValidationPort.isInDeliveryRange(address);
            
            log.debug("配送範圍檢查完成 - 結果: {}", inRange);
            return inRange;
            
        } catch (Exception e) {
            log.error("配送範圍檢查失敗", e);
            // 發生錯誤時，保守地返回false
            return false;
        }
    }
}