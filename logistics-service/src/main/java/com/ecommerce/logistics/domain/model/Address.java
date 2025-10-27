package com.ecommerce.logistics.domain.model;

import com.ecommerce.logistics.domain.exception.InvalidAddressException;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Column;
import java.util.regex.Pattern;

/**
 * 地址值物件
 * 遵循 SRP 原則 - 只負責地址資料和驗證邏輯
 * 支援台北地址格式驗證
 */
@Data
@NoArgsConstructor
@Embeddable
public class Address {
    
    @Column(name = "city", nullable = false, length = 50)
    private String city;
    
    @Column(name = "district", nullable = false, length = 50)
    private String district;
    
    @Column(name = "street", nullable = false, length = 200)
    private String street;
    
    @Column(name = "postal_code", nullable = false, length = 10)
    private String postalCode;
    
    @Column(name = "recipient_name", nullable = false, length = 100)
    private String recipientName;
    
    @Column(name = "recipient_phone", nullable = false, length = 20)
    private String recipientPhone;
    
    // 台北市郵遞區號範圍：100-116
    private static final Pattern TAIPEI_POSTAL_CODE_PATTERN = Pattern.compile("^1(0[0-9]|1[0-6])$");
    
    // 台灣手機號碼格式
    private static final Pattern PHONE_PATTERN = Pattern.compile("^09\\d{8}$");
    
    public Address(String city, String district, String street, String postalCode, 
                   String recipientName, String recipientPhone) {
        this.city = city;
        this.district = district;
        this.street = street;
        this.postalCode = postalCode;
        this.recipientName = recipientName;
        this.recipientPhone = recipientPhone;
        validate();
    }
    
    /**
     * 驗證地址格式
     * 實作台北地址驗證業務規則
     */
    public void validate() {
        if (city == null || city.trim().isEmpty()) {
            throw new InvalidAddressException("城市不能為空");
        }
        
        if (district == null || district.trim().isEmpty()) {
            throw new InvalidAddressException("區域不能為空");
        }
        
        if (street == null || street.trim().isEmpty()) {
            throw new InvalidAddressException("街道地址不能為空");
        }
        
        if (postalCode == null || postalCode.trim().isEmpty()) {
            throw new InvalidAddressException("郵遞區號不能為空");
        }
        
        if (recipientName == null || recipientName.trim().isEmpty()) {
            throw new InvalidAddressException("收件人姓名不能為空");
        }
        
        if (recipientPhone == null || recipientPhone.trim().isEmpty()) {
            throw new InvalidAddressException("收件人電話不能為空");
        }
        
        // 驗證台北地址格式
        if ("台北市".equals(city) || "臺北市".equals(city)) {
            validateTaipeiAddress();
        }
        
        // 驗證手機號碼格式
        if (!PHONE_PATTERN.matcher(recipientPhone).matches()) {
            throw new InvalidAddressException("手機號碼格式不正確，應為09開頭的10位數字");
        }
    }
    
    /**
     * 驗證台北市地址格式
     */
    private void validateTaipeiAddress() {
        if (!TAIPEI_POSTAL_CODE_PATTERN.matcher(postalCode).matches()) {
            throw new InvalidAddressException("台北市郵遞區號格式不正確，應為100-116");
        }
        
        // 驗證台北市行政區
        if (!isValidTaipeiDistrict(district)) {
            throw new InvalidAddressException("無效的台北市行政區：" + district);
        }
    }
    
    /**
     * 檢查是否為有效的台北市行政區
     */
    private boolean isValidTaipeiDistrict(String district) {
        String[] validDistricts = {
            "中正區", "大同區", "中山區", "松山區", "大安區", "萬華區",
            "信義區", "士林區", "北投區", "內湖區", "南港區", "文山區"
        };
        
        for (String validDistrict : validDistricts) {
            if (validDistrict.equals(district)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 取得完整地址字串
     */
    public String getFullAddress() {
        return String.format("%s%s%s (%s)", city, district, street, postalCode);
    }
    
    /**
     * 檢查是否為台北市地址
     */
    public boolean isTaipeiAddress() {
        return "台北市".equals(city) || "臺北市".equals(city);
    }
}