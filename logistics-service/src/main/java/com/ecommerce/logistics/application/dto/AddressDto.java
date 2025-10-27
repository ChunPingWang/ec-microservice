package com.ecommerce.logistics.application.dto;

import com.ecommerce.common.dto.BaseDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 地址資料傳輸物件
 * 遵循 SRP 原則 - 只負責地址資料的傳輸
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class AddressDto extends BaseDto {
    
    @NotBlank(message = "城市不能為空")
    @Size(max = 50, message = "城市名稱長度不能超過50個字元")
    private String city;
    
    @NotBlank(message = "區域不能為空")
    @Size(max = 50, message = "區域名稱長度不能超過50個字元")
    private String district;
    
    @NotBlank(message = "街道地址不能為空")
    @Size(max = 200, message = "街道地址長度不能超過200個字元")
    private String street;
    
    @NotBlank(message = "郵遞區號不能為空")
    @Pattern(regexp = "^\\d{3,5}$", message = "郵遞區號格式不正確")
    private String postalCode;
    
    @NotBlank(message = "收件人姓名不能為空")
    @Size(max = 100, message = "收件人姓名長度不能超過100個字元")
    private String recipientName;
    
    @NotBlank(message = "收件人電話不能為空")
    @Pattern(regexp = "^09\\d{8}$", message = "手機號碼格式不正確，應為09開頭的10位數字")
    private String recipientPhone;
    
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