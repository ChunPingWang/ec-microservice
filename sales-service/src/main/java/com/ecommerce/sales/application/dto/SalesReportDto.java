package com.ecommerce.sales.application.dto;

import com.ecommerce.common.dto.BaseDto;
import com.ecommerce.sales.domain.model.ReportType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 銷售報表 DTO
 */
public class SalesReportDto extends BaseDto {
    
    private String reportId;
    private String reportName;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime generatedAt;
    private ReportType reportType;
    private BigDecimal totalRevenue;
    private Integer totalQuantity;
    private BigDecimal averageOrderValue;
    private BigDecimal totalDiscount;
    private BigDecimal highValueSalesRate;
    private List<SalesRecordDto> records;
    private Map<String, Object> additionalData;
    
    public SalesReportDto() {}
    
    // Getters and Setters
    public String getReportId() { return reportId; }
    public void setReportId(String reportId) { this.reportId = reportId; }
    
    public String getReportName() { return reportName; }
    public void setReportName(String reportName) { this.reportName = reportName; }
    
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
    
    public ReportType getReportType() { return reportType; }
    public void setReportType(ReportType reportType) { this.reportType = reportType; }
    
    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }
    
    public Integer getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(Integer totalQuantity) { this.totalQuantity = totalQuantity; }
    
    public BigDecimal getAverageOrderValue() { return averageOrderValue; }
    public void setAverageOrderValue(BigDecimal averageOrderValue) { this.averageOrderValue = averageOrderValue; }
    
    public BigDecimal getTotalDiscount() { return totalDiscount; }
    public void setTotalDiscount(BigDecimal totalDiscount) { this.totalDiscount = totalDiscount; }
    
    public BigDecimal getHighValueSalesRate() { return highValueSalesRate; }
    public void setHighValueSalesRate(BigDecimal highValueSalesRate) { this.highValueSalesRate = highValueSalesRate; }
    
    public List<SalesRecordDto> getRecords() { return records; }
    public void setRecords(List<SalesRecordDto> records) { this.records = records; }
    
    public Map<String, Object> getAdditionalData() { return additionalData; }
    public void setAdditionalData(Map<String, Object> additionalData) { this.additionalData = additionalData; }
}