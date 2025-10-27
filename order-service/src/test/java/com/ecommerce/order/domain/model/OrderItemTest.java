package com.ecommerce.order.domain.model;

import com.ecommerce.common.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 訂單項目實體測試
 * 測試訂單項目的業務邏輯和驗證規則
 */
@DisplayName("訂單項目實體測試")
class OrderItemTest {

    private static final String PRODUCT_ID = "PROD-001";
    private static final String PRODUCT_NAME = "iPhone 17 Pro";
    private static final BigDecimal UNIT_PRICE = new BigDecimal("35900");
    private static final Integer QUANTITY = 1;
    private static final String SPECIFICATIONS = "256GB 黑色";

    @Nested
    @DisplayName("訂單項目建立測試")
    class OrderItemCreationTest {

        @Test
        @DisplayName("應該成功建立有效的訂單項目")
        void shouldCreateValidOrderItem() {
            // When
            OrderItem orderItem = OrderItem.create(PRODUCT_ID, PRODUCT_NAME, 
                                                 UNIT_PRICE, QUANTITY, SPECIFICATIONS);

            // Then
            assertNotNull(orderItem.getOrderItemId());
            assertEquals(PRODUCT_ID, orderItem.getProductId());
            assertEquals(PRODUCT_NAME, orderItem.getProductName());
            assertEquals(UNIT_PRICE, orderItem.getUnitPrice());
            assertEquals(QUANTITY, orderItem.getQuantity());
            assertEquals(SPECIFICATIONS, orderItem.getProductSpecifications());
            assertEquals(UNIT_PRICE, orderItem.getTotalPrice());
        }

        @Test
        @DisplayName("當商品ID為空時應該拋出異常")
        void shouldThrowExceptionWhenProductIdIsEmpty() {
            // When & Then
            assertThrows(ValidationException.class, () -> 
                OrderItem.create("", PRODUCT_NAME, UNIT_PRICE, QUANTITY, SPECIFICATIONS));
        }

        @Test
        @DisplayName("當商品名稱為空時應該拋出異常")
        void shouldThrowExceptionWhenProductNameIsEmpty() {
            // When & Then
            assertThrows(ValidationException.class, () -> 
                OrderItem.create(PRODUCT_ID, "", UNIT_PRICE, QUANTITY, SPECIFICATIONS));
        }

        @Test
        @DisplayName("當商品名稱超過200字元時應該拋出異常")
        void shouldThrowExceptionWhenProductNameTooLong() {
            // Given
            String longName = "A".repeat(201);

            // When & Then
            assertThrows(ValidationException.class, () -> 
                OrderItem.create(PRODUCT_ID, longName, UNIT_PRICE, QUANTITY, SPECIFICATIONS));
        }

        @Test
        @DisplayName("當單價為null時應該拋出異常")
        void shouldThrowExceptionWhenUnitPriceIsNull() {
            // When & Then
            assertThrows(ValidationException.class, () -> 
                OrderItem.create(PRODUCT_ID, PRODUCT_NAME, null, QUANTITY, SPECIFICATIONS));
        }

        @Test
        @DisplayName("當單價為零或負數時應該拋出異常")
        void shouldThrowExceptionWhenUnitPriceIsZeroOrNegative() {
            // When & Then
            assertThrows(ValidationException.class, () -> 
                OrderItem.create(PRODUCT_ID, PRODUCT_NAME, BigDecimal.ZERO, QUANTITY, SPECIFICATIONS));
            
            assertThrows(ValidationException.class, () -> 
                OrderItem.create(PRODUCT_ID, PRODUCT_NAME, new BigDecimal("-100"), QUANTITY, SPECIFICATIONS));
        }

        @Test
        @DisplayName("當單價小數位數超過2位時應該拋出異常")
        void shouldThrowExceptionWhenUnitPriceHasMoreThanTwoDecimalPlaces() {
            // When & Then
            assertThrows(ValidationException.class, () -> 
                OrderItem.create(PRODUCT_ID, PRODUCT_NAME, new BigDecimal("100.123"), QUANTITY, SPECIFICATIONS));
        }

        @Test
        @DisplayName("當數量為null或零或負數時應該拋出異常")
        void shouldThrowExceptionWhenQuantityIsInvalid() {
            // When & Then
            assertThrows(ValidationException.class, () -> 
                OrderItem.create(PRODUCT_ID, PRODUCT_NAME, UNIT_PRICE, null, SPECIFICATIONS));
            
            assertThrows(ValidationException.class, () -> 
                OrderItem.create(PRODUCT_ID, PRODUCT_NAME, UNIT_PRICE, 0, SPECIFICATIONS));
            
            assertThrows(ValidationException.class, () -> 
                OrderItem.create(PRODUCT_ID, PRODUCT_NAME, UNIT_PRICE, -1, SPECIFICATIONS));
        }

        @Test
        @DisplayName("當數量超過999時應該拋出異常")
        void shouldThrowExceptionWhenQuantityExceedsLimit() {
            // When & Then
            assertThrows(ValidationException.class, () -> 
                OrderItem.create(PRODUCT_ID, PRODUCT_NAME, UNIT_PRICE, 1000, SPECIFICATIONS));
        }

        @Test
        @DisplayName("當商品規格超過1000字元時應該拋出異常")
        void shouldThrowExceptionWhenSpecificationsTooLong() {
            // Given
            String longSpecs = "A".repeat(1001);

            // When & Then
            assertThrows(ValidationException.class, () -> 
                OrderItem.create(PRODUCT_ID, PRODUCT_NAME, UNIT_PRICE, QUANTITY, longSpecs));
        }
    }

    @Nested
    @DisplayName("訂單項目業務方法測試")
    class OrderItemBusinessMethodTest {

        @Test
        @DisplayName("應該成功更新數量並重新計算總價")
        void shouldUpdateQuantityAndRecalculateTotalPrice() {
            // Given
            OrderItem orderItem = OrderItem.create(PRODUCT_ID, PRODUCT_NAME, 
                                                 UNIT_PRICE, QUANTITY, SPECIFICATIONS);

            // When
            orderItem.updateQuantity(3);

            // Then
            assertEquals(3, orderItem.getQuantity());
            assertEquals(new BigDecimal("107700"), orderItem.getTotalPrice());
        }

        @Test
        @DisplayName("應該成功更新單價並重新計算總價")
        void shouldUpdatePriceAndRecalculateTotalPrice() {
            // Given
            OrderItem orderItem = OrderItem.create(PRODUCT_ID, PRODUCT_NAME, 
                                                 UNIT_PRICE, 2, SPECIFICATIONS);
            BigDecimal newPrice = new BigDecimal("30000");

            // When
            orderItem.updatePrice(newPrice);

            // Then
            assertEquals(newPrice, orderItem.getUnitPrice());
            assertEquals(new BigDecimal("60000"), orderItem.getTotalPrice());
        }

        @Test
        @DisplayName("當更新數量為無效值時應該拋出異常")
        void shouldThrowExceptionWhenUpdatingToInvalidQuantity() {
            // Given
            OrderItem orderItem = OrderItem.create(PRODUCT_ID, PRODUCT_NAME, 
                                                 UNIT_PRICE, QUANTITY, SPECIFICATIONS);

            // When & Then
            assertThrows(ValidationException.class, () -> orderItem.updateQuantity(0));
            assertThrows(ValidationException.class, () -> orderItem.updateQuantity(-1));
            assertThrows(ValidationException.class, () -> orderItem.updateQuantity(1000));
        }

        @Test
        @DisplayName("當更新單價為無效值時應該拋出異常")
        void shouldThrowExceptionWhenUpdatingToInvalidPrice() {
            // Given
            OrderItem orderItem = OrderItem.create(PRODUCT_ID, PRODUCT_NAME, 
                                                 UNIT_PRICE, QUANTITY, SPECIFICATIONS);

            // When & Then
            assertThrows(ValidationException.class, () -> 
                orderItem.updatePrice(BigDecimal.ZERO));
            assertThrows(ValidationException.class, () -> 
                orderItem.updatePrice(new BigDecimal("-100")));
            assertThrows(ValidationException.class, () -> 
                orderItem.updatePrice(new BigDecimal("100.123")));
        }
    }

    @Nested
    @DisplayName("訂單項目總價計算測試")
    class OrderItemTotalPriceCalculationTest {

        @Test
        @DisplayName("應該正確計算單一商品的總價")
        void shouldCalculateTotalPriceForSingleItem() {
            // Given & When
            OrderItem orderItem = OrderItem.create(PRODUCT_ID, PRODUCT_NAME, 
                                                 new BigDecimal("1000"), 1, SPECIFICATIONS);

            // Then
            assertEquals(new BigDecimal("1000"), orderItem.getTotalPrice());
        }

        @Test
        @DisplayName("應該正確計算多個商品的總價")
        void shouldCalculateTotalPriceForMultipleItems() {
            // Given & When
            OrderItem orderItem = OrderItem.create(PRODUCT_ID, PRODUCT_NAME, 
                                                 new BigDecimal("1000"), 5, SPECIFICATIONS);

            // Then
            assertEquals(new BigDecimal("5000"), orderItem.getTotalPrice());
        }

        @Test
        @DisplayName("應該正確計算帶小數點的總價")
        void shouldCalculateTotalPriceWithDecimals() {
            // Given & When
            OrderItem orderItem = OrderItem.create(PRODUCT_ID, PRODUCT_NAME, 
                                                 new BigDecimal("99.99"), 3, SPECIFICATIONS);

            // Then
            assertEquals(new BigDecimal("299.97"), orderItem.getTotalPrice());
        }
    }

    @Nested
    @DisplayName("訂單項目相等性測試")
    class OrderItemEqualityTest {

        @Test
        @DisplayName("相同ID的訂單項目應該相等")
        void shouldBeEqualWhenSameId() {
            // Given
            OrderItem item1 = OrderItem.create(PRODUCT_ID, PRODUCT_NAME, 
                                             UNIT_PRICE, QUANTITY, SPECIFICATIONS);
            OrderItem item2 = OrderItem.create("PROD-002", "iPad Pro", 
                                             new BigDecimal("25900"), 1, "11吋");
            
            // 設定相同的ID（模擬從資料庫載入的情況）
            String sameId = item1.getOrderItemId();
            
            // When & Then
            assertEquals(item1, item1);
            assertNotEquals(item1, item2);
            assertEquals(item1.hashCode(), item1.hashCode());
        }

        @Test
        @DisplayName("與null或不同類型物件比較應該不相等")
        void shouldNotBeEqualToNullOrDifferentType() {
            // Given
            OrderItem orderItem = OrderItem.create(PRODUCT_ID, PRODUCT_NAME, 
                                                 UNIT_PRICE, QUANTITY, SPECIFICATIONS);

            // When & Then
            assertNotEquals(orderItem, null);
            assertNotEquals(orderItem, "string");
            assertNotEquals(orderItem, new Object());
        }
    }

    @Nested
    @DisplayName("訂單項目字串表示測試")
    class OrderItemToStringTest {

        @Test
        @DisplayName("toString方法應該包含關鍵資訊")
        void shouldContainKeyInformationInToString() {
            // Given
            OrderItem orderItem = OrderItem.create(PRODUCT_ID, PRODUCT_NAME, 
                                                 UNIT_PRICE, QUANTITY, SPECIFICATIONS);

            // When
            String toString = orderItem.toString();

            // Then
            assertTrue(toString.contains(PRODUCT_ID));
            assertTrue(toString.contains(PRODUCT_NAME));
            assertTrue(toString.contains(UNIT_PRICE.toString()));
            assertTrue(toString.contains(QUANTITY.toString()));
        }
    }
}