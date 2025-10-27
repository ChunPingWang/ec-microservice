package com.ecommerce.sales.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 銷售報表領域模型測試
 * 驗證報表生成的準確性和業務邏輯
 */
@DisplayName("銷售報表測試")
class SalesReportTest {

    @Test
    @DisplayName("應該成功建立有效的銷售報表")
    void shouldCreateValidSalesReport() {
        // Given
        List<SalesRecord> records = createSampleSalesRecords();
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);

        // When
        SalesReport report = new SalesReport.Builder()
                .reportId("RPT-001")
                .reportName("一月銷售報表")
                .dateRange(startDate, endDate)
                .reportType(ReportType.MONTHLY)
                .records(records)
                .build();

        // Then
        assertNotNull(report);
        assertEquals("RPT-001", report.getReportId());
        assertEquals("一月銷售報表", report.getReportName());
        assertEquals(startDate, report.getStartDate());
        assertEquals(endDate, report.getEndDate());
        assertEquals(ReportType.MONTHLY, report.getReportType());
        assertEquals(records.size(), report.getRecords().size());
        assertNotNull(report.getGeneratedAt());
        assertNotNull(report.getMetrics());
    }

    @Test
    @DisplayName("應該正確計算銷售指標")
    void shouldCalculateMetricsCorrectly() {
        // Given
        List<SalesRecord> records = List.of(
            createSalesRecord("SR-001", "iPhone 17 Pro", 1, new BigDecimal("35000"), new BigDecimal("1000")),
            createSalesRecord("SR-002", "iPad Pro", 2, new BigDecimal("25000"), new BigDecimal("500")),
            createSalesRecord("SR-003", "MacBook Pro", 1, new BigDecimal("50000"), BigDecimal.ZERO)
        );

        // When
        SalesReport report = new SalesReport.Builder()
                .reportId("RPT-002")
                .reportName("測試報表")
                .dateRange(LocalDate.now(), LocalDate.now())
                .reportType(ReportType.DAILY)
                .records(records)
                .build();

        // Then
        SalesMetrics metrics = report.getMetrics();
        
        // 總收入 = (35000 - 1000) + (25000 * 2 - 500) + 50000 = 34000 + 49500 + 50000 = 133500
        assertEquals(new BigDecimal("133500"), metrics.getTotalRevenue());
        
        // 總數量 = 1 + 2 + 1 = 4
        assertEquals(Integer.valueOf(4), metrics.getTotalQuantity());
        
        // 平均訂單價值 = 133500 / 3 = 44500
        assertEquals(new BigDecimal("44500.00"), metrics.getAverageOrderValue());
        
        // 總折扣 = 1000 + 500 + 0 = 1500
        assertEquals(new BigDecimal("1500"), metrics.getTotalDiscount());
        
        // 高價值銷售率 = 3/3 = 1.0 (所有銷售都 >= 10000)
        assertEquals(new BigDecimal("1.0000"), metrics.getHighValueSalesRate());
    }

    @Test
    @DisplayName("空報表應該有零指標")
    void shouldHaveZeroMetricsForEmptyReport() {
        // Given
        List<SalesRecord> emptyRecords = List.of();

        // When
        SalesReport report = new SalesReport.Builder()
                .reportId("RPT-003")
                .reportName("空報表")
                .dateRange(LocalDate.now(), LocalDate.now())
                .reportType(ReportType.DAILY)
                .records(emptyRecords)
                .build();

        // Then
        SalesMetrics metrics = report.getMetrics();
        assertEquals(BigDecimal.ZERO, metrics.getTotalRevenue());
        assertEquals(Integer.valueOf(0), metrics.getTotalQuantity());
        assertEquals(BigDecimal.ZERO, metrics.getAverageOrderValue());
        assertEquals(BigDecimal.ZERO, metrics.getTotalDiscount());
        assertEquals(BigDecimal.ZERO, metrics.getHighValueSalesRate());
        
        assertTrue(report.isEmpty());
    }

    @Test
    @DisplayName("應該正確檢查日期範圍包含")
    void shouldCheckDateRangeContainment() {
        // Given
        LocalDate reportStart = LocalDate.of(2024, 1, 1);
        LocalDate reportEnd = LocalDate.of(2024, 1, 31);
        
        SalesReport report = new SalesReport.Builder()
                .reportId("RPT-004")
                .reportName("測試報表")
                .dateRange(reportStart, reportEnd)
                .reportType(ReportType.MONTHLY)
                .records(List.of())
                .build();

        // Then
        // 完全包含
        assertTrue(report.containsDateRange(LocalDate.of(2024, 1, 10), LocalDate.of(2024, 1, 20)));
        
        // 部分重疊
        assertTrue(report.containsDateRange(LocalDate.of(2023, 12, 25), LocalDate.of(2024, 1, 5)));
        assertTrue(report.containsDateRange(LocalDate.of(2024, 1, 25), LocalDate.of(2024, 2, 5)));
        
        // 完全不重疊
        assertFalse(report.containsDateRange(LocalDate.of(2023, 12, 1), LocalDate.of(2023, 12, 31)));
        assertFalse(report.containsDateRange(LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 28)));
    }

    @Test
    @DisplayName("應該正確過濾分類記錄")
    void shouldFilterRecordsByCategory() {
        // Given
        List<SalesRecord> records = List.of(
            createSalesRecordWithCategory("SR-001", "Electronics"),
            createSalesRecordWithCategory("SR-002", "Electronics"),
            createSalesRecordWithCategory("SR-003", "Accessories"),
            createSalesRecordWithCategory("SR-004", "Books")
        );

        SalesReport report = new SalesReport.Builder()
                .reportId("RPT-005")
                .reportName("分類測試報表")
                .dateRange(LocalDate.now(), LocalDate.now())
                .reportType(ReportType.DAILY)
                .records(records)
                .build();

        // When
        List<SalesRecord> electronicsRecords = report.getRecordsByCategory("Electronics");
        List<SalesRecord> accessoriesRecords = report.getRecordsByCategory("Accessories");
        List<SalesRecord> nonExistentRecords = report.getRecordsByCategory("NonExistent");

        // Then
        assertEquals(2, electronicsRecords.size());
        assertEquals(1, accessoriesRecords.size());
        assertEquals(0, nonExistentRecords.size());
    }

    @Test
    @DisplayName("應該正確過濾通道記錄")
    void shouldFilterRecordsByChannel() {
        // Given
        List<SalesRecord> records = List.of(
            createSalesRecordWithChannel("SR-001", SalesChannel.ONLINE),
            createSalesRecordWithChannel("SR-002", SalesChannel.ONLINE),
            createSalesRecordWithChannel("SR-003", SalesChannel.MOBILE_APP),
            createSalesRecordWithChannel("SR-004", SalesChannel.PHYSICAL_STORE)
        );

        SalesReport report = new SalesReport.Builder()
                .reportId("RPT-006")
                .reportName("通道測試報表")
                .dateRange(LocalDate.now(), LocalDate.now())
                .reportType(ReportType.DAILY)
                .records(records)
                .build();

        // When
        List<SalesRecord> onlineRecords = report.getRecordsByChannel(SalesChannel.ONLINE);
        List<SalesRecord> mobileRecords = report.getRecordsByChannel(SalesChannel.MOBILE_APP);

        // Then
        assertEquals(2, onlineRecords.size());
        assertEquals(1, mobileRecords.size());
    }

    @Test
    @DisplayName("報表ID為空時應拋出異常")
    void shouldThrowExceptionWhenReportIdIsEmpty() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new SalesReport.Builder()
                    .reportId("")
                    .reportName("測試報表")
                    .dateRange(LocalDate.now(), LocalDate.now())
                    .reportType(ReportType.DAILY)
                    .records(List.of())
                    .build();
        });
        
        assertEquals("報表ID不能為空", exception.getMessage());
    }

    @Test
    @DisplayName("開始日期晚於結束日期時應拋出異常")
    void shouldThrowExceptionWhenStartDateAfterEndDate() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new SalesReport.Builder()
                    .reportId("RPT-007")
                    .reportName("測試報表")
                    .dateRange(LocalDate.of(2024, 2, 1), LocalDate.of(2024, 1, 31))
                    .reportType(ReportType.DAILY)
                    .records(List.of())
                    .build();
        });
        
        assertEquals("開始日期不能晚於結束日期", exception.getMessage());
    }

    @Test
    @DisplayName("應該正確處理額外資料")
    void shouldHandleAdditionalDataCorrectly() {
        // Given
        Map<String, Object> additionalData = Map.of(
            "generatedBy", "SYSTEM",
            "version", "1.0",
            "notes", "測試報表"
        );

        // When
        SalesReport report = new SalesReport.Builder()
                .reportId("RPT-008")
                .reportName("額外資料測試報表")
                .dateRange(LocalDate.now(), LocalDate.now())
                .reportType(ReportType.DAILY)
                .records(List.of())
                .additionalData(additionalData)
                .build();

        // Then
        assertEquals(additionalData, report.getAdditionalData());
        assertEquals("SYSTEM", report.getAdditionalData().get("generatedBy"));
        assertEquals("1.0", report.getAdditionalData().get("version"));
        assertEquals("測試報表", report.getAdditionalData().get("notes"));
    }

    // 輔助方法

    private List<SalesRecord> createSampleSalesRecords() {
        return List.of(
            createSalesRecord("SR-001", "iPhone 17 Pro", 1, new BigDecimal("35000"), new BigDecimal("1000")),
            createSalesRecord("SR-002", "iPad Pro", 1, new BigDecimal("25000"), BigDecimal.ZERO)
        );
    }

    private SalesRecord createSalesRecord(String id, String productName, int quantity, 
                                        BigDecimal unitPrice, BigDecimal discount) {
        return SalesRecord.create(
            id, "ORDER-" + id, "CUST-001", "PROD-" + id, productName,
            quantity, unitPrice, discount, "Electronics", SalesChannel.ONLINE, "台北"
        );
    }

    private SalesRecord createSalesRecordWithCategory(String id, String category) {
        return SalesRecord.create(
            id, "ORDER-" + id, "CUST-001", "PROD-" + id, "Product",
            1, new BigDecimal("1000"), BigDecimal.ZERO, category, SalesChannel.ONLINE, "台北"
        );
    }

    private SalesRecord createSalesRecordWithChannel(String id, SalesChannel channel) {
        return SalesRecord.create(
            id, "ORDER-" + id, "CUST-001", "PROD-" + id, "Product",
            1, new BigDecimal("1000"), BigDecimal.ZERO, "Electronics", channel, "台北"
        );
    }
}