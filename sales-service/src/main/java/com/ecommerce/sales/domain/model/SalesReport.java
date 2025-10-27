package com.ecommerce.sales.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 銷售報表值物件 - 不可變的報表資料結構
 * 遵循 SRP：只負責封裝報表資料和計算邏輯
 */
public class SalesReport {
    
    private final String reportId;
    private final String reportName;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final LocalDateTime generatedAt;
    private final ReportType reportType;
    private final SalesMetrics metrics;
    private final List<SalesRecord> records;
    private final Map<String, Object> additionalData;
    
    private SalesReport(Builder builder) {
        this.reportId = builder.reportId;
        this.reportName = builder.reportName;
        this.startDate = builder.startDate;
        this.endDate = builder.endDate;
        this.generatedAt = LocalDateTime.now();
        this.reportType = builder.reportType;
        this.metrics = calculateMetrics(builder.records);
        this.records = List.copyOf(builder.records); // 不可變列表
        this.additionalData = Map.copyOf(builder.additionalData); // 不可變映射
    }
    
    /**
     * 計算銷售指標的業務邏輯
     */
    private SalesMetrics calculateMetrics(List<SalesRecord> records) {
        if (records.isEmpty()) {
            return new SalesMetrics(BigDecimal.ZERO, 0, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }
        
        BigDecimal totalRevenue = records.stream()
                .map(SalesRecord::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        int totalQuantity = records.stream()
                .mapToInt(SalesRecord::getQuantity)
                .sum();
        
        BigDecimal averageOrderValue = totalRevenue.divide(
                BigDecimal.valueOf(records.size()), 2, BigDecimal.ROUND_HALF_UP);
        
        BigDecimal totalDiscount = records.stream()
                .map(SalesRecord::getDiscount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        long highValueSalesCount = records.stream()
                .mapToLong(record -> record.isHighValueSale() ? 1 : 0)
                .sum();
        
        BigDecimal highValueSalesRate = BigDecimal.valueOf(highValueSalesCount)
                .divide(BigDecimal.valueOf(records.size()), 4, BigDecimal.ROUND_HALF_UP);
        
        return new SalesMetrics(totalRevenue, totalQuantity, averageOrderValue, 
                              totalDiscount, highValueSalesRate);
    }
    
    /**
     * 檢查報表是否包含指定期間的資料
     */
    public boolean containsDateRange(LocalDate start, LocalDate end) {
        return !startDate.isAfter(end) && !endDate.isBefore(start);
    }
    
    /**
     * 檢查是否為空報表
     */
    public boolean isEmpty() {
        return records.isEmpty();
    }
    
    /**
     * 取得指定分類的銷售記錄
     */
    public List<SalesRecord> getRecordsByCategory(String category) {
        return records.stream()
                .filter(record -> category.equals(record.getCategory()))
                .toList();
    }
    
    /**
     * 取得指定通道的銷售記錄
     */
    public List<SalesRecord> getRecordsByChannel(SalesChannel channel) {
        return records.stream()
                .filter(record -> channel.equals(record.getChannel()))
                .toList();
    }
    
    // Getters
    public String getReportId() { return reportId; }
    public String getReportName() { return reportName; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public ReportType getReportType() { return reportType; }
    public SalesMetrics getMetrics() { return metrics; }
    public List<SalesRecord> getRecords() { return records; }
    public Map<String, Object> getAdditionalData() { return additionalData; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SalesReport that = (SalesReport) o;
        return Objects.equals(reportId, that.reportId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(reportId);
    }
    
    @Override
    public String toString() {
        return "SalesReport{" +
                "reportId='" + reportId + '\'' +
                ", reportName='" + reportName + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", reportType=" + reportType +
                ", recordCount=" + records.size() +
                ", totalRevenue=" + metrics.getTotalRevenue() +
                '}';
    }
    
    /**
     * Builder 模式用於建立不可變的 SalesReport
     */
    public static class Builder {
        private String reportId;
        private String reportName;
        private LocalDate startDate;
        private LocalDate endDate;
        private ReportType reportType;
        private List<SalesRecord> records;
        private Map<String, Object> additionalData = Map.of();
        
        public Builder reportId(String reportId) {
            this.reportId = reportId;
            return this;
        }
        
        public Builder reportName(String reportName) {
            this.reportName = reportName;
            return this;
        }
        
        public Builder dateRange(LocalDate startDate, LocalDate endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
            return this;
        }
        
        public Builder reportType(ReportType reportType) {
            this.reportType = reportType;
            return this;
        }
        
        public Builder records(List<SalesRecord> records) {
            this.records = records;
            return this;
        }
        
        public Builder additionalData(Map<String, Object> additionalData) {
            this.additionalData = additionalData;
            return this;
        }
        
        public SalesReport build() {
            validateBuilder();
            return new SalesReport(this);
        }
        
        private void validateBuilder() {
            if (reportId == null || reportId.trim().isEmpty()) {
                throw new IllegalArgumentException("報表ID不能為空");
            }
            if (reportName == null || reportName.trim().isEmpty()) {
                throw new IllegalArgumentException("報表名稱不能為空");
            }
            if (startDate == null) {
                throw new IllegalArgumentException("開始日期不能為空");
            }
            if (endDate == null) {
                throw new IllegalArgumentException("結束日期不能為空");
            }
            if (startDate.isAfter(endDate)) {
                throw new IllegalArgumentException("開始日期不能晚於結束日期");
            }
            if (reportType == null) {
                throw new IllegalArgumentException("報表類型不能為空");
            }
            if (records == null) {
                throw new IllegalArgumentException("銷售記錄不能為空");
            }
        }
    }
}