package com.ecommerce.sales.application.usecase;

import com.ecommerce.sales.application.dto.GenerateReportRequest;
import com.ecommerce.sales.application.dto.SalesReportDto;
import com.ecommerce.sales.application.mapper.SalesMapper;
import com.ecommerce.sales.application.port.in.SalesReportUseCase;
import com.ecommerce.sales.application.port.out.ReportGenerationPort;
import com.ecommerce.sales.application.port.out.SalesEventPublisherPort;
import com.ecommerce.sales.application.port.out.SalesPersistencePort;
import com.ecommerce.sales.domain.event.SalesReportGeneratedEvent;
import com.ecommerce.sales.domain.exception.InvalidReportParametersException;
import com.ecommerce.sales.domain.model.ReportType;
import com.ecommerce.sales.domain.model.SalesRecord;
import com.ecommerce.sales.domain.model.SalesReport;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 銷售報表服務實作
 * 遵循 SRP：只負責報表生成和管理相關的業務流程
 */
@Service
@Transactional
public class SalesReportService implements SalesReportUseCase {
    
    private final SalesPersistencePort salesPersistencePort;
    private final ReportGenerationPort reportGenerationPort;
    private final SalesEventPublisherPort salesEventPublisherPort;
    private final SalesMapper salesMapper;
    
    // 簡單的記憶體快取，實際應用中應使用 Redis 或其他快取解決方案
    private final Map<String, SalesReport> reportCache = new ConcurrentHashMap<>();
    
    public SalesReportService(SalesPersistencePort salesPersistencePort,
                            ReportGenerationPort reportGenerationPort,
                            SalesEventPublisherPort salesEventPublisherPort,
                            SalesMapper salesMapper) {
        this.salesPersistencePort = salesPersistencePort;
        this.reportGenerationPort = reportGenerationPort;
        this.salesEventPublisherPort = salesEventPublisherPort;
        this.salesMapper = salesMapper;
    }
    
    @Override
    public SalesReportDto generateReport(GenerateReportRequest request) {
        validateReportRequest(request);
        
        // 根據報表類型取得相應的銷售記錄
        List<SalesRecord> records = getRecordsForReport(request);
        
        // 生成報表ID
        String reportId = generateReportId();
        
        // 建立銷售報表
        SalesReport.Builder reportBuilder = new SalesReport.Builder()
                .reportId(reportId)
                .reportName(request.getReportName())
                .dateRange(request.getStartDate(), request.getEndDate())
                .reportType(request.getReportType())
                .records(records);
        
        // 設定額外參數
        if (request.getAdditionalParameters() != null) {
            reportBuilder.additionalData(request.getAdditionalParameters());
        }
        
        SalesReport salesReport = reportBuilder.build();
        
        // 快取報表
        reportCache.put(reportId, salesReport);
        
        // 發布報表生成事件
        SalesReportGeneratedEvent event = new SalesReportGeneratedEvent(
            reportId,
            request.getReportName(),
            request.getReportType(),
            request.getStartDate(),
            request.getEndDate(),
            records.size(),
            "SYSTEM" // 實際應用中應該是當前使用者
        );
        salesEventPublisherPort.publishSalesReportGenerated(event);
        
        return salesMapper.toDto(salesReport);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<SalesReportDto> getReports(ReportType reportType, LocalDate startDate, LocalDate endDate) {
        // 從快取中過濾符合條件的報表
        return reportCache.values().stream()
                .filter(report -> reportType == null || report.getReportType() == reportType)
                .filter(report -> startDate == null || !report.getStartDate().isBefore(startDate))
                .filter(report -> endDate == null || !report.getEndDate().isAfter(endDate))
                .map(salesMapper::toDto)
                .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public SalesReportDto getReportById(String reportId) {
        SalesReport report = reportCache.get(reportId);
        if (report == null) {
            throw new InvalidReportParametersException("報表不存在：" + reportId);
        }
        
        return salesMapper.toDto(report);
    }
    
    @Override
    @Transactional(readOnly = true)
    public byte[] exportReport(String reportId, String format) {
        SalesReport report = reportCache.get(reportId);
        if (report == null) {
            throw new InvalidReportParametersException("報表不存在：" + reportId);
        }
        
        if (!reportGenerationPort.supportsFormat(format)) {
            throw new InvalidReportParametersException("不支援的格式：" + format);
        }
        
        return switch (format.toLowerCase()) {
            case "pdf" -> reportGenerationPort.generatePdfReport(report);
            case "excel", "xlsx" -> reportGenerationPort.generateExcelReport(report);
            case "csv" -> reportGenerationPort.generateCsvReport(report);
            default -> throw new InvalidReportParametersException("不支援的格式：" + format);
        };
    }
    
    @Override
    public void deleteReport(String reportId) {
        if (!reportCache.containsKey(reportId)) {
            throw new InvalidReportParametersException("報表不存在：" + reportId);
        }
        
        reportCache.remove(reportId);
    }
    
    // 私有輔助方法
    
    private void validateReportRequest(GenerateReportRequest request) {
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new InvalidReportParametersException("開始日期不能晚於結束日期");
        }
        
        // 檢查日期範圍是否合理（不超過一年）
        if (request.getStartDate().plusYears(1).isBefore(request.getEndDate())) {
            throw new InvalidReportParametersException("報表日期範圍不能超過一年");
        }
    }
    
    private List<SalesRecord> getRecordsForReport(GenerateReportRequest request) {
        List<SalesRecord> records = salesPersistencePort.findByDateRange(
            request.getStartDate(), request.getEndDate());
        
        // 根據報表類型和參數進行過濾
        if (request.getCategory() != null && !request.getCategory().trim().isEmpty()) {
            records = records.stream()
                    .filter(record -> request.getCategory().equals(record.getCategory()))
                    .toList();
        }
        
        if (request.getRegion() != null && !request.getRegion().trim().isEmpty()) {
            records = records.stream()
                    .filter(record -> request.getRegion().equals(record.getRegion()))
                    .toList();
        }
        
        if (request.getCustomerId() != null && !request.getCustomerId().trim().isEmpty()) {
            records = records.stream()
                    .filter(record -> request.getCustomerId().equals(record.getCustomerId()))
                    .toList();
        }
        
        return records;
    }
    
    private String generateReportId() {
        return "RPT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}