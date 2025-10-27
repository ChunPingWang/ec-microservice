package com.ecommerce.sales.application.usecase;

import com.ecommerce.sales.application.dto.GenerateReportRequest;
import com.ecommerce.sales.application.dto.SalesReportDto;
import com.ecommerce.sales.application.mapper.SalesMapper;
import com.ecommerce.sales.application.port.out.ReportGenerationPort;
import com.ecommerce.sales.application.port.out.SalesEventPublisherPort;
import com.ecommerce.sales.application.port.out.SalesPersistencePort;
import com.ecommerce.sales.domain.exception.InvalidReportParametersException;
import com.ecommerce.sales.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 銷售報表服務測試
 * 驗證報表生成的準確性和業務邏輯
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("銷售報表服務測試")
class SalesReportServiceTest {

    @Mock
    private SalesPersistencePort salesPersistencePort;

    @Mock
    private ReportGenerationPort reportGenerationPort;

    @Mock
    private SalesEventPublisherPort salesEventPublisherPort;

    @Mock
    private SalesMapper salesMapper;

    private SalesReportService salesReportService;

    @BeforeEach
    void setUp() {
        salesReportService = new SalesReportService(
            salesPersistencePort, reportGenerationPort, salesEventPublisherPort, salesMapper
        );
    }

    @Test
    @DisplayName("應該成功生成月度銷售報表")
    void shouldGenerateMonthlyReportSuccessfully() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);
        
        GenerateReportRequest request = new GenerateReportRequest();
        request.setReportName("一月銷售報表");
        request.setReportType(ReportType.MONTHLY);
        request.setStartDate(startDate);
        request.setEndDate(endDate);

        List<SalesRecord> mockRecords = createMockSalesRecords();
        SalesReportDto expectedDto = createMockSalesReportDto();

        when(salesPersistencePort.findByDateRange(startDate, endDate)).thenReturn(mockRecords);
        when(salesMapper.toDto(any(SalesReport.class))).thenReturn(expectedDto);

        // When
        SalesReportDto result = salesReportService.generateReport(request);

        // Then
        assertNotNull(result);
        assertEquals(expectedDto, result);
        
        verify(salesPersistencePort).findByDateRange(startDate, endDate);
        verify(salesMapper).toDto(any(SalesReport.class));
        verify(salesEventPublisherPort).publishSalesReportGenerated(any());
    }

    @Test
    @DisplayName("應該成功生成分類報表")
    void shouldGenerateCategoryReportSuccessfully() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);
        String category = "Electronics";
        
        GenerateReportRequest request = new GenerateReportRequest();
        request.setReportName("電子產品銷售報表");
        request.setReportType(ReportType.PRODUCT_CATEGORY);
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        request.setCategory(category);

        List<SalesRecord> allRecords = createMockSalesRecords();
        List<SalesRecord> filteredRecords = allRecords.stream()
                .filter(record -> category.equals(record.getCategory()))
                .toList();
        
        SalesReportDto expectedDto = createMockSalesReportDto();

        when(salesPersistencePort.findByDateRange(startDate, endDate)).thenReturn(allRecords);
        when(salesMapper.toDto(any(SalesReport.class))).thenReturn(expectedDto);

        // When
        SalesReportDto result = salesReportService.generateReport(request);

        // Then
        assertNotNull(result);
        verify(salesPersistencePort).findByDateRange(startDate, endDate);
        verify(salesMapper).toDto(any(SalesReport.class));
    }

    @Test
    @DisplayName("應該成功生成區域報表")
    void shouldGenerateRegionalReportSuccessfully() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);
        String region = "台北";
        
        GenerateReportRequest request = new GenerateReportRequest();
        request.setReportName("台北地區銷售報表");
        request.setReportType(ReportType.REGIONAL);
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        request.setRegion(region);

        List<SalesRecord> mockRecords = createMockSalesRecords();
        SalesReportDto expectedDto = createMockSalesReportDto();

        when(salesPersistencePort.findByDateRange(startDate, endDate)).thenReturn(mockRecords);
        when(salesMapper.toDto(any(SalesReport.class))).thenReturn(expectedDto);

        // When
        SalesReportDto result = salesReportService.generateReport(request);

        // Then
        assertNotNull(result);
        verify(salesPersistencePort).findByDateRange(startDate, endDate);
    }

    @Test
    @DisplayName("應該成功生成客戶報表")
    void shouldGenerateCustomerReportSuccessfully() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);
        String customerId = "CUST-001";
        
        GenerateReportRequest request = new GenerateReportRequest();
        request.setReportName("客戶銷售報表");
        request.setReportType(ReportType.CUSTOMER_SEGMENT);
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        request.setCustomerId(customerId);

        List<SalesRecord> mockRecords = createMockSalesRecords();
        SalesReportDto expectedDto = createMockSalesReportDto();

        when(salesPersistencePort.findByDateRange(startDate, endDate)).thenReturn(mockRecords);
        when(salesMapper.toDto(any(SalesReport.class))).thenReturn(expectedDto);

        // When
        SalesReportDto result = salesReportService.generateReport(request);

        // Then
        assertNotNull(result);
        verify(salesPersistencePort).findByDateRange(startDate, endDate);
    }

    @Test
    @DisplayName("開始日期晚於結束日期時應拋出異常")
    void shouldThrowExceptionWhenStartDateAfterEndDate() {
        // Given
        GenerateReportRequest request = new GenerateReportRequest();
        request.setReportName("無效報表");
        request.setReportType(ReportType.DAILY);
        request.setStartDate(LocalDate.of(2024, 2, 1));
        request.setEndDate(LocalDate.of(2024, 1, 31));

        // When & Then
        InvalidReportParametersException exception = assertThrows(
            InvalidReportParametersException.class,
            () -> salesReportService.generateReport(request)
        );
        
        assertEquals("開始日期不能晚於結束日期", exception.getMessage());
        verify(salesPersistencePort, never()).findByDateRange(any(), any());
    }

    @Test
    @DisplayName("日期範圍超過一年時應拋出異常")
    void shouldThrowExceptionWhenDateRangeExceedsOneYear() {
        // Given
        GenerateReportRequest request = new GenerateReportRequest();
        request.setReportName("超長期間報表");
        request.setReportType(ReportType.CUSTOM);
        request.setStartDate(LocalDate.of(2023, 1, 1));
        request.setEndDate(LocalDate.of(2024, 12, 31));

        // When & Then
        InvalidReportParametersException exception = assertThrows(
            InvalidReportParametersException.class,
            () -> salesReportService.generateReport(request)
        );
        
        assertEquals("報表日期範圍不能超過一年", exception.getMessage());
    }

    @Test
    @DisplayName("應該成功取得指定類型的報表清單")
    void shouldGetReportsByTypeSuccessfully() {
        // Given
        ReportType reportType = ReportType.MONTHLY;
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);

        // 先生成一個報表以便測試
        GenerateReportRequest request = new GenerateReportRequest();
        request.setReportName("測試報表");
        request.setReportType(reportType);
        request.setStartDate(startDate);
        request.setEndDate(endDate);

        List<SalesRecord> mockRecords = createMockSalesRecords();
        SalesReportDto mockDto = createMockSalesReportDto();

        when(salesPersistencePort.findByDateRange(startDate, endDate)).thenReturn(mockRecords);
        when(salesMapper.toDto(any(SalesReport.class))).thenReturn(mockDto);

        // 生成報表
        salesReportService.generateReport(request);

        // When
        List<SalesReportDto> reports = salesReportService.getReports(reportType, startDate, endDate);

        // Then
        assertNotNull(reports);
        assertFalse(reports.isEmpty());
    }

    @Test
    @DisplayName("應該成功根據ID取得報表")
    void shouldGetReportByIdSuccessfully() {
        // Given
        GenerateReportRequest request = new GenerateReportRequest();
        request.setReportName("測試報表");
        request.setReportType(ReportType.DAILY);
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now());

        List<SalesRecord> mockRecords = createMockSalesRecords();

        when(salesPersistencePort.findByDateRange(any(), any())).thenReturn(mockRecords);
        when(salesMapper.toDto(any(SalesReport.class))).thenAnswer(invocation -> {
            SalesReport report = invocation.getArgument(0);
            SalesReportDto dto = createMockSalesReportDto();
            dto.setReportId(report.getReportId()); // 使用實際的報表ID
            return dto;
        });

        // 先生成報表
        SalesReportDto generatedReport = salesReportService.generateReport(request);
        String reportId = generatedReport.getReportId();

        // When
        SalesReportDto result = salesReportService.getReportById(reportId);

        // Then
        assertNotNull(result);
        assertEquals(reportId, result.getReportId());
    }

    @Test
    @DisplayName("取得不存在的報表時應拋出異常")
    void shouldThrowExceptionWhenReportNotFound() {
        // Given
        String nonExistentReportId = "RPT-NONEXISTENT";

        // When & Then
        InvalidReportParametersException exception = assertThrows(
            InvalidReportParametersException.class,
            () -> salesReportService.getReportById(nonExistentReportId)
        );
        
        assertEquals("報表不存在：" + nonExistentReportId, exception.getMessage());
    }

    @Test
    @DisplayName("應該成功匯出PDF報表")
    void shouldExportPdfReportSuccessfully() {
        // Given
        GenerateReportRequest request = new GenerateReportRequest();
        request.setReportName("PDF測試報表");
        request.setReportType(ReportType.DAILY);
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now());

        List<SalesRecord> mockRecords = createMockSalesRecords();
        byte[] mockPdfData = "PDF內容".getBytes();

        when(salesPersistencePort.findByDateRange(any(), any())).thenReturn(mockRecords);
        when(salesMapper.toDto(any(SalesReport.class))).thenAnswer(invocation -> {
            SalesReport report = invocation.getArgument(0);
            SalesReportDto dto = createMockSalesReportDto();
            dto.setReportId(report.getReportId());
            return dto;
        });
        when(reportGenerationPort.supportsFormat("pdf")).thenReturn(true);
        when(reportGenerationPort.generatePdfReport(any(SalesReport.class))).thenReturn(mockPdfData);

        // 先生成報表
        SalesReportDto generatedReport = salesReportService.generateReport(request);
        String reportId = generatedReport.getReportId();

        // When
        byte[] result = salesReportService.exportReport(reportId, "pdf");

        // Then
        assertNotNull(result);
        assertArrayEquals(mockPdfData, result);
        verify(reportGenerationPort).generatePdfReport(any(SalesReport.class));
    }

    @Test
    @DisplayName("應該成功匯出Excel報表")
    void shouldExportExcelReportSuccessfully() {
        // Given
        GenerateReportRequest request = new GenerateReportRequest();
        request.setReportName("Excel測試報表");
        request.setReportType(ReportType.DAILY);
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now());

        List<SalesRecord> mockRecords = createMockSalesRecords();
        byte[] mockExcelData = "Excel內容".getBytes();

        when(salesPersistencePort.findByDateRange(any(), any())).thenReturn(mockRecords);
        when(salesMapper.toDto(any(SalesReport.class))).thenAnswer(invocation -> {
            SalesReport report = invocation.getArgument(0);
            SalesReportDto dto = createMockSalesReportDto();
            dto.setReportId(report.getReportId());
            return dto;
        });
        when(reportGenerationPort.supportsFormat("excel")).thenReturn(true);
        when(reportGenerationPort.generateExcelReport(any(SalesReport.class))).thenReturn(mockExcelData);

        // 先生成報表
        SalesReportDto generatedReport = salesReportService.generateReport(request);
        String reportId = generatedReport.getReportId();

        // When
        byte[] result = salesReportService.exportReport(reportId, "excel");

        // Then
        assertNotNull(result);
        assertArrayEquals(mockExcelData, result);
        verify(reportGenerationPort).generateExcelReport(any(SalesReport.class));
    }

    @Test
    @DisplayName("不支援的格式應拋出異常")
    void shouldThrowExceptionForUnsupportedFormat() {
        // Given
        GenerateReportRequest request = new GenerateReportRequest();
        request.setReportName("測試報表");
        request.setReportType(ReportType.DAILY);
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now());

        List<SalesRecord> mockRecords = createMockSalesRecords();

        when(salesPersistencePort.findByDateRange(any(), any())).thenReturn(mockRecords);
        when(salesMapper.toDto(any(SalesReport.class))).thenAnswer(invocation -> {
            SalesReport report = invocation.getArgument(0);
            SalesReportDto dto = createMockSalesReportDto();
            dto.setReportId(report.getReportId());
            return dto;
        });
        when(reportGenerationPort.supportsFormat("unsupported")).thenReturn(false);

        // 先生成報表
        SalesReportDto generatedReport = salesReportService.generateReport(request);
        String reportId = generatedReport.getReportId();

        // When & Then
        InvalidReportParametersException exception = assertThrows(
            InvalidReportParametersException.class,
            () -> salesReportService.exportReport(reportId, "unsupported")
        );
        
        assertEquals("不支援的格式：unsupported", exception.getMessage());
    }

    @Test
    @DisplayName("應該成功刪除報表")
    void shouldDeleteReportSuccessfully() {
        // Given
        GenerateReportRequest request = new GenerateReportRequest();
        request.setReportName("待刪除報表");
        request.setReportType(ReportType.DAILY);
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now());

        List<SalesRecord> mockRecords = createMockSalesRecords();

        when(salesPersistencePort.findByDateRange(any(), any())).thenReturn(mockRecords);
        when(salesMapper.toDto(any(SalesReport.class))).thenAnswer(invocation -> {
            SalesReport report = invocation.getArgument(0);
            SalesReportDto dto = createMockSalesReportDto();
            dto.setReportId(report.getReportId());
            return dto;
        });

        // 先生成報表
        SalesReportDto generatedReport = salesReportService.generateReport(request);
        String reportId = generatedReport.getReportId();

        // When
        assertDoesNotThrow(() -> salesReportService.deleteReport(reportId));

        // Then - 刪除後應該無法再取得報表
        InvalidReportParametersException exception = assertThrows(
            InvalidReportParametersException.class,
            () -> salesReportService.getReportById(reportId)
        );
        assertEquals("報表不存在：" + reportId, exception.getMessage());
    }

    // 輔助方法

    private List<SalesRecord> createMockSalesRecords() {
        return List.of(
            SalesRecord.create(
                "SR-001", "ORDER-001", "CUST-001", "PROD-001", "iPhone 17 Pro",
                1, new BigDecimal("35000"), new BigDecimal("1000"), "Electronics", 
                SalesChannel.ONLINE, "台北"
            ),
            SalesRecord.create(
                "SR-002", "ORDER-002", "CUST-002", "PROD-002", "iPad Pro",
                2, new BigDecimal("25000"), BigDecimal.ZERO, "Electronics", 
                SalesChannel.MOBILE_APP, "台北"
            )
        );
    }

    private SalesReportDto createMockSalesReportDto() {
        SalesReportDto dto = new SalesReportDto();
        dto.setReportId("RPT-001");
        dto.setReportName("測試報表");
        dto.setReportType(ReportType.DAILY);
        dto.setStartDate(LocalDate.now());
        dto.setEndDate(LocalDate.now());
        return dto;
    }
}