package com.ecommerce.sales.application.dto;

import com.ecommerce.common.dto.BaseDto;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 銷售分析請求 DTO
 * 遵循 SRP：只負責封裝銷售分析的請求參數
 */
public class SalesAnalysisRequest extends BaseDto {
    
    @NotNull(message = "開始日期不能為空")
    private LocalDate startDate;
    
    @NotNull(message = "結束日期不能為空")
    private LocalDate endDate;
    
    private String category;
    private String region;
    private String customerId;
    private String productId;
    
    // 預設建構子
    public SalesAnalysisRequest() {}
    
    // 建構子
    public SalesAnalysisRequest(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }
    
    public SalesAnalysisRequest(LocalDate startDate, LocalDate endDate, String category,
                              String region, String customerId, String productId) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.category = category;
        this.region = region;
        this.customerId = customerId;
        this.productId = productId;
    }
    
    // 驗證方法
    public void validate() {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("開始日期不能晚於結束日期");
        }
    }
    
    // Getters and Setters
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    
    @Override
    public String toString() {
        return "SalesAnalysisRequest{" +
                "startDate=" + startDate +
                ", endDate=" + endDate +
                ", category='" + category + '\'' +
                ", region='" + region + '\'' +
                ", customerId='" + customerId + '\'' +
                ", productId='" + productId + '\'' +
                '}';
    }
}