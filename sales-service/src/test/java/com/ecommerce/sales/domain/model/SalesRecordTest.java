package com.ecommerce.sales.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

/**
 * 銷售記錄領域模型測試
 * 測試銷售資料的正確記錄和業務邏輯
 */
@DisplayName("銷售記錄測試")
class SalesRecordTest {

    @Test
    @DisplayName("應該成功建立有效的銷售記錄")
    void shouldCreateValidSalesRecord() {
        // Given
        String salesRecordId = "SR-001";
        String orderId = "ORDER-001";
        String customerId = "CUST-001";
        String productId = "PROD-001";
        String productName = "iPhone 17 Pro";
        Integer quantity = 2;
        BigDecimal unitPrice = new BigDecimal("35000");
        BigDecimal discount = new BigDecimal("1000");
        String category = "Electronics";
        SalesChannel channel = SalesChannel.ONLINE;
        String region = "台北";

        // When
        SalesRecord salesRecord = SalesRecord.create(
            salesRecordId, orderId, customerId, productId, productName,
            quantity, unitPrice, discount, category, channel, region
        );

        // Then
        assertNotNull(salesRecord);
        assertEquals(salesRecordId, salesRecord.getSalesRecordId());
        assertEquals(orderId, salesRecord.getOrderId());
        assertEquals(customerId, salesRecord.getCustomerId());
        assertEquals(productId, salesRecord.getProductId());
        assertEquals(productName, salesRecord.getProductName());
        assertEquals(quantity, salesRecord.getQuantity());
        assertEquals(unitPrice, salesRecord.getUnitPrice());
        assertEquals(discount, salesRecord.getDiscount());
        assertEquals(category, salesRecord.getCategory());
        assertEquals(channel, salesRecord.getChannel());
        assertEquals(region, salesRecord.getRegion());
        
        // 驗證計算的總金額
        BigDecimal expectedTotal = unitPrice.multiply(BigDecimal.valueOf(quantity)).subtract(discount);
        assertEquals(expectedTotal, salesRecord.getTotalAmount());
        
        assertNotNull(salesRecord.getSaleDate());
    }

    @Test
    @DisplayName("應該正確計算總金額")
    void shouldCalculateTotalAmountCorrectly() {
        // Given
        BigDecimal unitPrice = new BigDecimal("1000");
        Integer quantity = 3;
        BigDecimal discount = new BigDecimal("200");

        // When
        SalesRecord salesRecord = SalesRecord.create(
            "SR-002", "ORDER-002", "CUST-002", "PROD-002", "Product",
            quantity, unitPrice, discount, "Category", SalesChannel.ONLINE, "台北"
        );

        // Then
        BigDecimal expectedTotal = new BigDecimal("2800"); // 1000 * 3 - 200
        assertEquals(expectedTotal, salesRecord.getTotalAmount());
    }

    @Test
    @DisplayName("應該正確處理無折扣的情況")
    void shouldHandleNoDiscountCorrectly() {
        // When
        SalesRecord salesRecord = SalesRecord.create(
            "SR-003", "ORDER-003", "CUST-003", "PROD-003", "Product",
            2, new BigDecimal("500"), null, "Category", SalesChannel.ONLINE, "台北"
        );

        // Then
        assertEquals(BigDecimal.ZERO, salesRecord.getDiscount());
        assertEquals(new BigDecimal("1000"), salesRecord.getTotalAmount());
    }

    @Test
    @DisplayName("應該正確識別高價值銷售")
    void shouldIdentifyHighValueSale() {
        // Given - 高價值銷售（>= 10000）
        SalesRecord highValueSale = SalesRecord.create(
            "SR-004", "ORDER-004", "CUST-004", "PROD-004", "iPhone 17 Pro",
            1, new BigDecimal("15000"), BigDecimal.ZERO, "Electronics", 
            SalesChannel.ONLINE, "台北"
        );

        // Given - 一般銷售
        SalesRecord regularSale = SalesRecord.create(
            "SR-005", "ORDER-005", "CUST-005", "PROD-005", "Accessory",
            1, new BigDecimal("500"), BigDecimal.ZERO, "Accessories", 
            SalesChannel.ONLINE, "台北"
        );

        // Then
        assertTrue(highValueSale.isHighValueSale());
        assertFalse(regularSale.isHighValueSale());
    }

    @Test
    @DisplayName("應該正確識別促銷銷售")
    void shouldIdentifyPromotionalSale() {
        // Given - 有折扣的銷售
        SalesRecord promotionalSale = SalesRecord.create(
            "SR-006", "ORDER-006", "CUST-006", "PROD-006", "Product",
            1, new BigDecimal("1000"), new BigDecimal("100"), "Category", 
            SalesChannel.ONLINE, "台北"
        );

        // Given - 無折扣的銷售
        SalesRecord regularSale = SalesRecord.create(
            "SR-007", "ORDER-007", "CUST-007", "PROD-007", "Product",
            1, new BigDecimal("1000"), BigDecimal.ZERO, "Category", 
            SalesChannel.ONLINE, "台北"
        );

        // Then
        assertTrue(promotionalSale.isPromotionalSale());
        assertFalse(regularSale.isPromotionalSale());
    }

    @Test
    @DisplayName("應該正確計算折扣率")
    void shouldCalculateDiscountRateCorrectly() {
        // Given
        SalesRecord salesRecord = SalesRecord.create(
            "SR-008", "ORDER-008", "CUST-008", "PROD-008", "Product",
            2, new BigDecimal("1000"), new BigDecimal("200"), "Category", 
            SalesChannel.ONLINE, "台北"
        );

        // When
        BigDecimal discountRate = salesRecord.getDiscountRate();

        // Then
        // 折扣率 = 200 / (1000 * 2) = 0.1 (10%)
        assertEquals(new BigDecimal("0.1000"), discountRate);
    }

    @Test
    @DisplayName("無折扣時折扣率應為零")
    void shouldReturnZeroDiscountRateWhenNoDiscount() {
        // Given
        SalesRecord salesRecord = SalesRecord.create(
            "SR-009", "ORDER-009", "CUST-009", "PROD-009", "Product",
            1, new BigDecimal("1000"), BigDecimal.ZERO, "Category", 
            SalesChannel.ONLINE, "台北"
        );

        // When
        BigDecimal discountRate = salesRecord.getDiscountRate();

        // Then
        assertEquals(BigDecimal.ZERO, discountRate);
    }

    @Test
    @DisplayName("銷售記錄ID為空時應拋出異常")
    void shouldThrowExceptionWhenSalesRecordIdIsEmpty() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            SalesRecord.create(
                "", "ORDER-010", "CUST-010", "PROD-010", "Product",
                1, new BigDecimal("1000"), BigDecimal.ZERO, "Category", 
                SalesChannel.ONLINE, "台北"
            );
        });
        
        assertEquals("銷售記錄ID不能為空", exception.getMessage());
    }

    @Test
    @DisplayName("數量為零或負數時應拋出異常")
    void shouldThrowExceptionWhenQuantityIsInvalid() {
        // When & Then - 數量為零
        IllegalArgumentException exception1 = assertThrows(IllegalArgumentException.class, () -> {
            SalesRecord.create(
                "SR-011", "ORDER-011", "CUST-011", "PROD-011", "Product",
                0, new BigDecimal("1000"), BigDecimal.ZERO, "Category", 
                SalesChannel.ONLINE, "台北"
            );
        });
        assertEquals("數量必須大於0", exception1.getMessage());

        // When & Then - 數量為負數
        IllegalArgumentException exception2 = assertThrows(IllegalArgumentException.class, () -> {
            SalesRecord.create(
                "SR-012", "ORDER-012", "CUST-012", "PROD-012", "Product",
                -1, new BigDecimal("1000"), BigDecimal.ZERO, "Category", 
                SalesChannel.ONLINE, "台北"
            );
        });
        assertEquals("數量必須大於0", exception2.getMessage());
    }

    @Test
    @DisplayName("單價為負數時應拋出異常")
    void shouldThrowExceptionWhenUnitPriceIsNegative() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            SalesRecord.create(
                "SR-013", "ORDER-013", "CUST-013", "PROD-013", "Product",
                1, new BigDecimal("-100"), BigDecimal.ZERO, "Category", 
                SalesChannel.ONLINE, "台北"
            );
        });
        
        assertEquals("單價不能為負數", exception.getMessage());
    }

    @Test
    @DisplayName("折扣為負數時應拋出異常")
    void shouldThrowExceptionWhenDiscountIsNegative() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            SalesRecord.create(
                "SR-014", "ORDER-014", "CUST-014", "PROD-014", "Product",
                1, new BigDecimal("1000"), new BigDecimal("-100"), "Category", 
                SalesChannel.ONLINE, "台北"
            );
        });
        
        assertEquals("折扣不能為負數", exception.getMessage());
    }
}