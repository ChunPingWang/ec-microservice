package com.ecommerce.sales.domain.event;

import com.ecommerce.common.architecture.DomainEvent;
import com.ecommerce.sales.domain.model.ReportType;
import java.time.LocalDate;

/**
 * 銷售報表生成事件
 * 當銷售報表生成完成時發布此事件
 */
public class SalesReportGeneratedEvent extends DomainEvent {
    
    private final String reportId;
    private final String reportName;
    private final ReportType reportType;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final int recordCount;
    private final String generatedBy;
    
    public SalesReportGeneratedEvent(String reportId, String reportName, ReportType reportType,
                                   LocalDate startDate, LocalDate endDate, int recordCount,
                                   String generatedBy) {
        super("SalesReportGenerated");
        this.reportId = reportId;
        this.reportName = reportName;
        this.reportType = reportType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.recordCount = recordCount;
        this.generatedBy = generatedBy;
    }
    
    public String getReportId() { return reportId; }
    public String getReportName() { return reportName; }
    public ReportType getReportType() { return reportType; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public int getRecordCount() { return recordCount; }
    public String getGeneratedBy() { return generatedBy; }
    
    @Override
    public String toString() {
        return "SalesReportGeneratedEvent{" +
                "reportId='" + reportId + '\'' +
                ", reportName='" + reportName + '\'' +
                ", reportType=" + reportType +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", recordCount=" + recordCount +
                ", generatedBy='" + generatedBy + '\'' +
                ", timestamp=" + getOccurredOn() +
                '}';
    }
}