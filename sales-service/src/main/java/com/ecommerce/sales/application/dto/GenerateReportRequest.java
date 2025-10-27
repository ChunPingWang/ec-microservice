package com.ecommerce.sales.application.dto;

import com.ecommerce.common.dto.BaseDto;
import com.ecommerce.sales.domain.model.ReportType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Map;

/**
 * 生成報表請求 DTO
 */
public class GenerateReportRequest extends BaseDto {
    
    @NotBlank(message = "報表名稱不能為空")
    private String reportName;
    
    @NotNull(message = "報表類型不能為空")
    private ReportType reportType;
    
    @NotNull(message = "開始日期不能為空")
    private LocalDate startDate;
    
    @NotNull(message = "結束日期不能為空")
    private LocalDate endDate;
    
    private String category;
    private String region;
    private String customerId;
    private Map<String, Object> additionalParameters;
    
    public GenerateReportRequest() {}
    
    public GenerateReportRequest(String reportName, ReportType reportType,
                               LocalDate startDate, LocalDate endDate) {
        this.reportName = reportName;
        this.reportType = reportType;
        this.startDate = startDate;
        this.endDate = endDate;
    }
    
    // Getters and Setters
    public String getReportName() { return reportName; }
    public void setReportName(String reportName) { this.reportName = reportName; }
    
    public ReportType getReportType() { return reportType; }
    public void setReportType(ReportType reportType) { this.reportType = reportType; }
    
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
    
    public Map<String, Object> getAdditionalParameters() { return additionalParameters; }
    public void setAdditionalParameters(Map<String, Object> additionalParameters) { this.additionalParameters = additionalParameters; }
}