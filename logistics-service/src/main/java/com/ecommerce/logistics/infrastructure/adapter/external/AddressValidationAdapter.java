package com.ecommerce.logistics.infrastructure.adapter.external;

import com.ecommerce.logistics.application.port.out.AddressValidationPort;
import com.ecommerce.logistics.domain.model.Address;
import com.ecommerce.common.architecture.ExternalAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 地址驗證適配器
 * 遵循 SRP 原則 - 只負責外部地址驗證服務的整合
 * 遵循 DIP 原則 - 實作應用層定義的地址驗證埠介面
 */
@Slf4j
@Component
@ExternalAdapter
public class AddressValidationAdapter implements AddressValidationPort {
    
    // 台北市郵遞區號範圍：100-116
    private static final Pattern TAIPEI_POSTAL_CODE_PATTERN = Pattern.compile("^1(0[0-9]|1[0-6])$");
    
    // 台北市有效行政區列表
    private static final List<String> VALID_TAIPEI_DISTRICTS = Arrays.asList(
        "中正區", "大同區", "中山區", "松山區", "大安區", "萬華區",
        "信義區", "士林區", "北投區", "內湖區", "南港區", "文山區"
    );
    
    // 配送範圍內的城市列表（模擬）
    private static final List<String> DELIVERY_CITIES = Arrays.asList(
        "台北市", "臺北市", "新北市", "桃園市", "基隆市"
    );
    
    @Override
    public boolean validateAddressExists(Address address) {
        log.debug("驗證地址是否存在 - {}", address.getFullAddress());
        
        try {
            // 模擬外部地址驗證服務調用
            // 在實際實作中，這裡會調用真實的地址驗證API
            
            // 基本格式檢查
            if (address.getCity() == null || address.getDistrict() == null || 
                address.getStreet() == null || address.getPostalCode() == null) {
                return false;
            }
            
            // 檢查城市是否在支援列表中
            boolean cityExists = DELIVERY_CITIES.contains(address.getCity());
            
            // 如果是台北市，進行更詳細的驗證
            if (address.isTaipeiAddress()) {
                return cityExists && validateTaipeiAddressFormat(address);
            }
            
            log.debug("地址驗證結果 - 存在: {}", cityExists);
            return cityExists;
            
        } catch (Exception e) {
            log.error("地址驗證服務調用失敗", e);
            // 發生錯誤時，保守地返回false
            return false;
        }
    }
    
    @Override
    public boolean validateTaipeiAddressFormat(Address address) {
        log.debug("驗證台北地址格式 - {}", address.getFullAddress());
        
        try {
            // 檢查是否為台北市
            if (!address.isTaipeiAddress()) {
                return false;
            }
            
            // 檢查郵遞區號格式
            if (!TAIPEI_POSTAL_CODE_PATTERN.matcher(address.getPostalCode()).matches()) {
                log.debug("台北市郵遞區號格式不正確: {}", address.getPostalCode());
                return false;
            }
            
            // 檢查行政區是否有效
            if (!VALID_TAIPEI_DISTRICTS.contains(address.getDistrict())) {
                log.debug("無效的台北市行政區: {}", address.getDistrict());
                return false;
            }
            
            // 檢查街道地址是否合理（簡單檢查）
            if (address.getStreet().length() < 3) {
                log.debug("街道地址過短: {}", address.getStreet());
                return false;
            }
            
            log.debug("台北地址格式驗證通過");
            return true;
            
        } catch (Exception e) {
            log.error("台北地址格式驗證失敗", e);
            return false;
        }
    }
    
    @Override
    public Address normalizeAddress(Address address) {
        log.debug("標準化地址 - {}", address.getFullAddress());
        
        try {
            // 模擬地址標準化處理
            // 在實際實作中，這裡會調用地址標準化服務
            
            String normalizedCity = normalizeCity(address.getCity());
            String normalizedDistrict = normalizeDistrict(address.getDistrict());
            String normalizedStreet = normalizeStreet(address.getStreet());
            String normalizedPostalCode = address.getPostalCode().trim();
            
            Address normalizedAddress = new Address(
                normalizedCity,
                normalizedDistrict,
                normalizedStreet,
                normalizedPostalCode,
                address.getRecipientName().trim(),
                address.getRecipientPhone().trim()
            );
            
            log.debug("地址標準化完成 - {}", normalizedAddress.getFullAddress());
            return normalizedAddress;
            
        } catch (Exception e) {
            log.warn("地址標準化失敗，返回原地址: {}", e.getMessage());
            return address;
        }
    }
    
    @Override
    public boolean isInDeliveryRange(Address address) {
        log.debug("檢查配送範圍 - {}", address.getFullAddress());
        
        try {
            // 檢查城市是否在配送範圍內
            boolean inRange = DELIVERY_CITIES.contains(address.getCity());
            
            // 台北市全區都在配送範圍內
            if (address.isTaipeiAddress()) {
                inRange = true;
            }
            
            log.debug("配送範圍檢查結果 - 在範圍內: {}", inRange);
            return inRange;
            
        } catch (Exception e) {
            log.error("配送範圍檢查失敗", e);
            return false;
        }
    }
    
    @Override
    public String getAddressCoordinates(Address address) {
        log.debug("取得地址座標 - {}", address.getFullAddress());
        
        try {
            // 模擬座標查詢服務
            // 在實際實作中，這裡會調用地圖服務API
            
            // 為台北市提供模擬座標
            if (address.isTaipeiAddress()) {
                return generateTaipeiCoordinates(address.getDistrict());
            }
            
            // 其他城市返回預設座標
            return "25.0330,121.5654"; // 台北車站座標
            
        } catch (Exception e) {
            log.error("座標查詢失敗", e);
            return null;
        }
    }
    
    /**
     * 標準化城市名稱
     */
    private String normalizeCity(String city) {
        if (city == null) return null;
        
        String normalized = city.trim();
        
        // 統一台北市名稱
        if ("臺北市".equals(normalized)) {
            normalized = "台北市";
        }
        
        return normalized;
    }
    
    /**
     * 標準化行政區名稱
     */
    private String normalizeDistrict(String district) {
        if (district == null) return null;
        
        return district.trim();
    }
    
    /**
     * 標準化街道地址
     */
    private String normalizeStreet(String street) {
        if (street == null) return null;
        
        return street.trim().replaceAll("\\s+", " ");
    }
    
    /**
     * 生成台北市各區的模擬座標
     */
    private String generateTaipeiCoordinates(String district) {
        // 模擬各區的座標（實際應用中應該使用真實座標）
        return switch (district) {
            case "中正區" -> "25.0320,121.5080";
            case "大同區" -> "25.0633,121.5130";
            case "中山區" -> "25.0636,121.5264";
            case "松山區" -> "25.0497,121.5746";
            case "大安區" -> "25.0267,121.5436";
            case "萬華區" -> "25.0340,121.4998";
            case "信義區" -> "25.0329,121.5654";
            case "士林區" -> "25.0876,121.5258";
            case "北投區" -> "25.1314,121.5018";
            case "內湖區" -> "25.0695,121.5893";
            case "南港區" -> "25.0554,121.6078";
            case "文山區" -> "24.9889,121.5706";
            default -> "25.0330,121.5654"; // 預設台北車站座標
        };
    }
}