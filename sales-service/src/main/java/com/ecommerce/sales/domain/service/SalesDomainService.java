package com.ecommerce.sales.domain.service;

import com.ecommerce.common.architecture.DomainService;
import com.ecommerce.sales.domain.model.*;
import com.ecommerce.sales.domain.repository.SalesRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 銷售領域服務 - 包含複雜的銷售業務邏輯和分析規則
 * 遵循 SRP：只負責銷售相關的領域邏輯
 * 遵循 DIP：依賴抽象的倉儲介面
 */
@Service
@DomainService
public class SalesDomainService {
    
    private final SalesRepository salesRepository;
    
    public SalesDomainService(SalesRepository salesRepository) {
        this.salesRepository = salesRepository;
    }
    
    /**
     * 建立銷售記錄的業務邏輯
     * 包含重複檢查和業務規則驗證
     */
    public SalesRecord createSalesRecord(String orderId, String customerId, String productId,
                                       String productName, Integer quantity, BigDecimal unitPrice,
                                       BigDecimal discount, String category, SalesChannel channel,
                                       String region) {
        // 檢查是否已存在該訂單的銷售記錄
        if (salesRepository.existsByOrderId(orderId)) {
            throw new IllegalStateException("訂單 " + orderId + " 的銷售記錄已存在");
        }
        
        // 生成銷售記錄ID
        String salesRecordId = generateSalesRecordId(orderId);
        
        // 建立銷售記錄
        SalesRecord salesRecord = SalesRecord.create(
            salesRecordId, orderId, customerId, productId, productName,
            quantity, unitPrice, discount, category, channel, region
        );
        
        return salesRepository.save(salesRecord);
    }
    
    /**
     * 分析銷售趨勢的業務邏輯
     */
    public SalesTrendAnalysis analyzeSalesTrend(LocalDate startDate, LocalDate endDate) {
        List<SalesRecord> records = salesRepository.findByDateRange(startDate, endDate);
        
        if (records.isEmpty()) {
            return new SalesTrendAnalysis(TrendDirection.STABLE, BigDecimal.ZERO, "無銷售資料");
        }
        
        // 計算期間總收入
        BigDecimal totalRevenue = records.stream()
                .map(SalesRecord::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 計算平均日收入
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        BigDecimal averageDailyRevenue = totalRevenue.divide(
                BigDecimal.valueOf(daysBetween), 2, BigDecimal.ROUND_HALF_UP);
        
        // 分析趨勢方向
        TrendDirection direction = analyzeTrendDirection(records, startDate, endDate);
        
        // 生成分析說明
        String analysis = generateTrendAnalysis(direction, totalRevenue, averageDailyRevenue, records.size());
        
        return new SalesTrendAnalysis(direction, averageDailyRevenue, analysis);
    }
    
    /**
     * 分析客戶購買行為
     */
    public CustomerBehaviorAnalysis analyzeCustomerBehavior(String customerId) {
        List<SalesRecord> customerRecords = salesRepository.findByCustomerId(customerId);
        
        if (customerRecords.isEmpty()) {
            return new CustomerBehaviorAnalysis(customerId, CustomerSegment.NEW, BigDecimal.ZERO, 0);
        }
        
        // 計算客戶總消費金額
        BigDecimal totalSpent = customerRecords.stream()
                .map(SalesRecord::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 計算購買次數
        int purchaseCount = customerRecords.size();
        
        // 分析客戶分群
        CustomerSegment segment = determineCustomerSegment(totalSpent, purchaseCount);
        
        return new CustomerBehaviorAnalysis(customerId, segment, totalSpent, purchaseCount);
    }
    
    /**
     * 分析商品績效
     */
    public ProductPerformanceAnalysis analyzeProductPerformance(String category, LocalDate startDate, LocalDate endDate) {
        List<SalesRecord> categoryRecords = salesRepository.findByCategory(category).stream()
                .filter(record -> !record.getSaleDate().toLocalDate().isBefore(startDate) &&
                                !record.getSaleDate().toLocalDate().isAfter(endDate))
                .toList();
        
        if (categoryRecords.isEmpty()) {
            return new ProductPerformanceAnalysis(category, BigDecimal.ZERO, 0, PerformanceLevel.LOW);
        }
        
        // 計算分類總收入
        BigDecimal totalRevenue = categoryRecords.stream()
                .map(SalesRecord::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 計算總銷量
        int totalQuantity = categoryRecords.stream()
                .mapToInt(SalesRecord::getQuantity)
                .sum();
        
        // 評估績效等級
        PerformanceLevel performanceLevel = evaluatePerformanceLevel(totalRevenue, totalQuantity);
        
        return new ProductPerformanceAnalysis(category, totalRevenue, totalQuantity, performanceLevel);
    }
    
    /**
     * 分析通道績效
     */
    public Map<SalesChannel, BigDecimal> analyzeChannelPerformance(LocalDate startDate, LocalDate endDate) {
        List<SalesRecord> records = salesRepository.findByDateRange(startDate, endDate);
        
        return records.stream()
                .collect(Collectors.groupingBy(
                    SalesRecord::getChannel,
                    Collectors.reducing(BigDecimal.ZERO, SalesRecord::getTotalAmount, BigDecimal::add)
                ));
    }
    
    /**
     * 檢查是否需要補貨警告
     */
    public boolean shouldTriggerRestockAlert(String productId, LocalDate startDate, LocalDate endDate) {
        List<SalesRecord> productRecords = salesRepository.findByProductId(productId).stream()
                .filter(record -> !record.getSaleDate().toLocalDate().isBefore(startDate) &&
                                !record.getSaleDate().toLocalDate().isAfter(endDate))
                .toList();
        
        if (productRecords.isEmpty()) {
            return false;
        }
        
        // 計算平均日銷量
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        int totalQuantity = productRecords.stream().mapToInt(SalesRecord::getQuantity).sum();
        double averageDailySales = (double) totalQuantity / daysBetween;
        
        // 如果平均日銷量超過10件，建議補貨
        return averageDailySales > 10.0;
    }
    
    // 私有輔助方法
    
    private String generateSalesRecordId(String orderId) {
        return "SR-" + orderId + "-" + System.currentTimeMillis();
    }
    
    private TrendDirection analyzeTrendDirection(List<SalesRecord> records, LocalDate startDate, LocalDate endDate) {
        // 簡化的趨勢分析：比較前半期和後半期的收入
        LocalDate midDate = startDate.plusDays(ChronoUnit.DAYS.between(startDate, endDate) / 2);
        
        BigDecimal firstHalfRevenue = records.stream()
                .filter(record -> !record.getSaleDate().toLocalDate().isAfter(midDate))
                .map(SalesRecord::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal secondHalfRevenue = records.stream()
                .filter(record -> record.getSaleDate().toLocalDate().isAfter(midDate))
                .map(SalesRecord::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if (secondHalfRevenue.compareTo(firstHalfRevenue.multiply(new BigDecimal("1.1"))) > 0) {
            return TrendDirection.UPWARD;
        } else if (secondHalfRevenue.compareTo(firstHalfRevenue.multiply(new BigDecimal("0.9"))) < 0) {
            return TrendDirection.DOWNWARD;
        } else {
            return TrendDirection.STABLE;
        }
    }
    
    private String generateTrendAnalysis(TrendDirection direction, BigDecimal totalRevenue, 
                                       BigDecimal averageDailyRevenue, int recordCount) {
        return switch (direction) {
            case UPWARD -> String.format("銷售呈上升趨勢，總收入 %s，平均日收入 %s，共 %d 筆交易", 
                                       totalRevenue, averageDailyRevenue, recordCount);
            case DOWNWARD -> String.format("銷售呈下降趨勢，總收入 %s，平均日收入 %s，共 %d 筆交易", 
                                         totalRevenue, averageDailyRevenue, recordCount);
            case STABLE -> String.format("銷售保持穩定，總收入 %s，平均日收入 %s，共 %d 筆交易", 
                                        totalRevenue, averageDailyRevenue, recordCount);
        };
    }
    
    private CustomerSegment determineCustomerSegment(BigDecimal totalSpent, int purchaseCount) {
        if (totalSpent.compareTo(new BigDecimal("50000")) >= 0 && purchaseCount >= 10) {
            return CustomerSegment.VIP;
        } else if (totalSpent.compareTo(new BigDecimal("20000")) >= 0 && purchaseCount >= 5) {
            return CustomerSegment.LOYAL;
        } else if (purchaseCount >= 2) {
            return CustomerSegment.REGULAR;
        } else {
            return CustomerSegment.NEW;
        }
    }
    
    private PerformanceLevel evaluatePerformanceLevel(BigDecimal totalRevenue, int totalQuantity) {
        if (totalRevenue.compareTo(new BigDecimal("100000")) >= 0 && totalQuantity >= 100) {
            return PerformanceLevel.HIGH;
        } else if (totalRevenue.compareTo(new BigDecimal("50000")) >= 0 && totalQuantity >= 50) {
            return PerformanceLevel.MEDIUM;
        } else {
            return PerformanceLevel.LOW;
        }
    }
    
    // 內部類別定義分析結果
    
    public static class SalesTrendAnalysis {
        private final TrendDirection direction;
        private final BigDecimal averageDailyRevenue;
        private final String analysis;
        
        public SalesTrendAnalysis(TrendDirection direction, BigDecimal averageDailyRevenue, String analysis) {
            this.direction = direction;
            this.averageDailyRevenue = averageDailyRevenue;
            this.analysis = analysis;
        }
        
        public TrendDirection getDirection() { return direction; }
        public BigDecimal getAverageDailyRevenue() { return averageDailyRevenue; }
        public String getAnalysis() { return analysis; }
    }
    
    public static class CustomerBehaviorAnalysis {
        private final String customerId;
        private final CustomerSegment segment;
        private final BigDecimal totalSpent;
        private final int purchaseCount;
        
        public CustomerBehaviorAnalysis(String customerId, CustomerSegment segment, 
                                      BigDecimal totalSpent, int purchaseCount) {
            this.customerId = customerId;
            this.segment = segment;
            this.totalSpent = totalSpent;
            this.purchaseCount = purchaseCount;
        }
        
        public String getCustomerId() { return customerId; }
        public CustomerSegment getSegment() { return segment; }
        public BigDecimal getTotalSpent() { return totalSpent; }
        public int getPurchaseCount() { return purchaseCount; }
    }
    
    public static class ProductPerformanceAnalysis {
        private final String category;
        private final BigDecimal totalRevenue;
        private final int totalQuantity;
        private final PerformanceLevel performanceLevel;
        
        public ProductPerformanceAnalysis(String category, BigDecimal totalRevenue, 
                                        int totalQuantity, PerformanceLevel performanceLevel) {
            this.category = category;
            this.totalRevenue = totalRevenue;
            this.totalQuantity = totalQuantity;
            this.performanceLevel = performanceLevel;
        }
        
        public String getCategory() { return category; }
        public BigDecimal getTotalRevenue() { return totalRevenue; }
        public int getTotalQuantity() { return totalQuantity; }
        public PerformanceLevel getPerformanceLevel() { return performanceLevel; }
    }
    
    // 枚舉定義
    
    public enum TrendDirection {
        UPWARD, DOWNWARD, STABLE
    }
    
    public enum CustomerSegment {
        NEW, REGULAR, LOYAL, VIP
    }
    
    public enum PerformanceLevel {
        LOW, MEDIUM, HIGH
    }
}