package com.ecommerce.logistics.application.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.ArrayList;

/**
 * 地址驗證結果DTO
 * 遵循 SRP 原則 - 只負責地址驗證結果的資料傳輸
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressValidationResult {
    
    private boolean valid;
    private List<String> errors;
    private AddressDto normalizedAddress;
    private boolean inDeliveryRange;
    private String coordinates;
    
    public AddressValidationResult(boolean valid) {
        this.valid = valid;
        this.errors = new ArrayList<>();
    }
    
    /**
     * 新增錯誤訊息
     */
    public void addError(String error) {
        if (this.errors == null) {
            this.errors = new ArrayList<>();
        }
        this.errors.add(error);
        this.valid = false;
    }
    
    /**
     * 檢查是否有錯誤
     */
    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }
    
    /**
     * 取得錯誤訊息字串
     */
    public String getErrorMessage() {
        if (errors == null || errors.isEmpty()) {
            return "";
        }
        return String.join("; ", errors);
    }
}