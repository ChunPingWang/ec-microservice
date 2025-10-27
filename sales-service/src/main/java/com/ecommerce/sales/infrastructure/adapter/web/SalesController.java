package com.ecommerce.sales.infrastructure.adapter.web;

import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.sales.application.dto.*;
import com.ecommerce.sales.application.port.in.SalesAnalysisUseCase;
import com.ecommerce.sales.application.port.in.SalesRecordUseCase;
import com.ecommerce.sales.application.port.in.SalesReportUseCase;
import com.ecommerce.sales.domain.model.ReportType;
import com.ecommerce.sales.domain.model.SalesChannel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 銷售控制器 - 提供銷售相關的 REST API
 * 遵循 SRP：只負責 HTTP 請求處理和回應
 * 遵循 DIP：依賴使用案例介面而非具體實作
 */
@RestController
@RequestMapping("/api/v1/sales")
@Tag(name = "Sales", description = "銷售管理 API")
public class SalesController {
    
    private final SalesRecordUseCase salesRecordUseCase;
    private final SalesAnalysisUseCase salesAnalysisUseCase;
    private final SalesReportUseCase salesReportUseCase;
    
    public SalesController(SalesRecordUseCase salesRecordUseCase,
                         SalesAnalysisUseCase salesAnalysisUseCase,
                         SalesReportUseCase salesReportUseCase) {
        this.salesRecordUseCase = salesRecordUseCase;
        this.salesAnalysisUseCase = salesAnalysisUseCase;
        this.salesReportUseCase = salesReportUseCase;
    }
    
    // 銷售記錄相關 API
    
    @PostMapping("/records")
    @Operation(summary = "建立銷售記錄", description = "根據訂單資訊建立新的銷售記錄")
    public ResponseEntity<ApiResponse<SalesRecordDto>> createSalesRecord(
            @Valid @RequestBody CreateSalesRecordRequest request) {
        
        SalesRecordDto salesRecord = salesRecordUseCase.createSalesRecord(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("銷售記錄建立成功", salesRecord));
    }
    
    @GetMapping("/records/{salesRecordId}")
    @Operation(summary = "取得銷售記錄", description = "根據ID取得銷售記錄詳細資訊")
    public ResponseEntity<ApiResponse<SalesRecordDto>> getSalesRecord(
            @PathVariable String salesRecordId) {
        
        SalesRecordDto salesRecord = salesRecordUseCase.getSalesRecordById(salesRecordId);
        
        return ResponseEntity.ok(ApiResponse.success(salesRecord));
    }
    
    @GetMapping("/records")
    @Operation(summary = "查詢銷售記錄", description = "根據條件查詢銷售記錄")
    public ResponseEntity<ApiResponse<List<SalesRecordDto>>> getSalesRecords(
            @RequestParam(required = false) String orderId,
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) SalesChannel channel,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        List<SalesRecordDto> salesRecords;
        
        if (orderId != null) {
            salesRecords = salesRecordUseCase.getSalesRecordsByOrderId(orderId);
        } else if (customerId != null) {
            salesRecords = salesRecordUseCase.getSalesRecordsByCustomerId(customerId);
        } else if (category != null) {
            salesRecords = salesRecordUseCase.getSalesRecordsByCategory(category);
        } else if (channel != null) {
            salesRecords = salesRecordUseCase.getSalesRecordsByChannel(channel);
        } else if (startDate != null && endDate != null) {
            salesRecords = salesRecordUseCase.getSalesRecordsByDateRange(startDate, endDate);
        } else {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("INVALID_PARAMETERS", "請提供查詢條件"));
        }
        
        return ResponseEntity.ok(ApiResponse.success(salesRecords));
    }
    
    // 銷售分析相關 API
    
    @PostMapping("/analysis/trend")
    @Operation(summary = "分析銷售趨勢", description = "分析指定期間的銷售趨勢")
    public ResponseEntity<ApiResponse<SalesAnalysisResponse>> analyzeSalesTrend(
            @Valid @RequestBody SalesAnalysisRequest request) {
        
        SalesAnalysisResponse analysis = salesAnalysisUseCase.analyzeSalesTrend(request);
        
        return ResponseEntity.ok(ApiResponse.success(analysis));
    }
    
    @GetMapping("/analysis/customer/{customerId}")
    @Operation(summary = "分析客戶行為", description = "分析指定客戶的購買行為")
    public ResponseEntity<ApiResponse<CustomerBehaviorAnalysisResponse>> analyzeCustomerBehavior(
            @PathVariable String customerId) {
        
        CustomerBehaviorAnalysisResponse analysis = salesAnalysisUseCase.analyzeCustomerBehavior(customerId);
        
        return ResponseEntity.ok(ApiResponse.success(analysis));
    }
    
    @GetMapping("/analysis/product")
    @Operation(summary = "分析商品績效", description = "分析指定分類的商品銷售績效")
    public ResponseEntity<ApiResponse<ProductPerformanceAnalysisResponse>> analyzeProductPerformance(
            @RequestParam String category,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        ProductPerformanceAnalysisResponse analysis = salesAnalysisUseCase.analyzeProductPerformance(
                category, startDate, endDate);
        
        return ResponseEntity.ok(ApiResponse.success(analysis));
    }
    
    @GetMapping("/analysis/channel")
    @Operation(summary = "分析通道績效", description = "分析各銷售通道的績效表現")
    public ResponseEntity<ApiResponse<Map<SalesChannel, BigDecimal>>> analyzeChannelPerformance(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        Map<SalesChannel, BigDecimal> analysis = salesAnalysisUseCase.analyzeChannelPerformance(
                startDate, endDate);
        
        return ResponseEntity.ok(ApiResponse.success(analysis));
    }
    
    @GetMapping("/analysis/restock-alert/{productId}")
    @Operation(summary = "檢查補貨警告", description = "檢查指定商品是否需要補貨")
    public ResponseEntity<ApiResponse<Boolean>> checkRestockAlert(
            @PathVariable String productId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        boolean shouldRestock = salesAnalysisUseCase.shouldTriggerRestockAlert(
                productId, startDate, endDate);
        
        return ResponseEntity.ok(ApiResponse.success(shouldRestock));
    }
    
    @GetMapping("/analysis/summary")
    @Operation(summary = "取得銷售摘要", description = "取得指定期間的銷售統計摘要")
    public ResponseEntity<ApiResponse<SalesAnalysisResponse>> getSalesSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        SalesAnalysisResponse summary = salesAnalysisUseCase.getSalesSummary(startDate, endDate);
        
        return ResponseEntity.ok(ApiResponse.success(summary));
    }
    
    // 銷售報表相關 API
    
    @PostMapping("/reports")
    @Operation(summary = "生成銷售報表", description = "根據指定條件生成銷售報表")
    public ResponseEntity<ApiResponse<SalesReportDto>> generateReport(
            @Valid @RequestBody GenerateReportRequest request) {
        
        SalesReportDto report = salesReportUseCase.generateReport(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("報表生成成功", report));
    }
    
    @GetMapping("/reports")
    @Operation(summary = "取得報表清單", description = "取得符合條件的報表清單")
    public ResponseEntity<ApiResponse<List<SalesReportDto>>> getReports(
            @RequestParam(required = false) ReportType reportType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        List<SalesReportDto> reports = salesReportUseCase.getReports(reportType, startDate, endDate);
        
        return ResponseEntity.ok(ApiResponse.success(reports));
    }
    
    @GetMapping("/reports/{reportId}")
    @Operation(summary = "取得報表詳細資訊", description = "根據ID取得報表詳細資訊")
    public ResponseEntity<ApiResponse<SalesReportDto>> getReport(
            @PathVariable String reportId) {
        
        SalesReportDto report = salesReportUseCase.getReportById(reportId);
        
        return ResponseEntity.ok(ApiResponse.success(report));
    }
    
    @GetMapping("/reports/{reportId}/export")
    @Operation(summary = "匯出報表", description = "將報表匯出為指定格式")
    public ResponseEntity<byte[]> exportReport(
            @PathVariable String reportId,
            @RequestParam(defaultValue = "pdf") String format) {
        
        byte[] reportData = salesReportUseCase.exportReport(reportId, format);
        
        String filename = "sales-report-" + reportId + "." + format.toLowerCase();
        MediaType mediaType = getMediaTypeForFormat(format);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(mediaType)
                .body(reportData);
    }
    
    @DeleteMapping("/reports/{reportId}")
    @Operation(summary = "刪除報表", description = "刪除指定的報表")
    public ResponseEntity<ApiResponse<Void>> deleteReport(
            @PathVariable String reportId) {
        
        salesReportUseCase.deleteReport(reportId);
        
        return ResponseEntity.ok(ApiResponse.success("報表刪除成功", (Void) null));
    }
    
    // 私有輔助方法
    
    private MediaType getMediaTypeForFormat(String format) {
        return switch (format.toLowerCase()) {
            case "pdf" -> MediaType.APPLICATION_PDF;
            case "excel", "xlsx" -> MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            case "csv" -> MediaType.parseMediaType("text/csv");
            default -> MediaType.APPLICATION_OCTET_STREAM;
        };
    }
}