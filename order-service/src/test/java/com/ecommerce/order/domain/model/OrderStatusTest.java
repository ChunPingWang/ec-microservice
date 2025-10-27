package com.ecommerce.order.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 訂單狀態枚舉測試
 * 測試訂單狀態轉換邏輯和業務規則
 */
@DisplayName("訂單狀態枚舉測試")
class OrderStatusTest {

    @Nested
    @DisplayName("狀態轉換測試")
    class StatusTransitionTest {

        @Test
        @DisplayName("PENDING狀態應該可以轉換到CONFIRMED或CANCELLED")
        void pendingStatusShouldTransitionToConfirmedOrCancelled() {
            // Given
            OrderStatus pending = OrderStatus.PENDING;

            // Then
            assertTrue(pending.canTransitionTo(OrderStatus.CONFIRMED));
            assertTrue(pending.canTransitionTo(OrderStatus.CANCELLED));
            assertFalse(pending.canTransitionTo(OrderStatus.PAID));
            assertFalse(pending.canTransitionTo(OrderStatus.SHIPPED));
            assertFalse(pending.canTransitionTo(OrderStatus.DELIVERED));
            assertFalse(pending.canTransitionTo(OrderStatus.REFUNDED));
        }

        @Test
        @DisplayName("CONFIRMED狀態應該可以轉換到PAID或CANCELLED")
        void confirmedStatusShouldTransitionToPaidOrCancelled() {
            // Given
            OrderStatus confirmed = OrderStatus.CONFIRMED;

            // Then
            assertTrue(confirmed.canTransitionTo(OrderStatus.PAID));
            assertTrue(confirmed.canTransitionTo(OrderStatus.CANCELLED));
            assertFalse(confirmed.canTransitionTo(OrderStatus.PENDING));
            assertFalse(confirmed.canTransitionTo(OrderStatus.CONFIRMED));
            assertFalse(confirmed.canTransitionTo(OrderStatus.SHIPPED));
            assertFalse(confirmed.canTransitionTo(OrderStatus.DELIVERED));
            assertFalse(confirmed.canTransitionTo(OrderStatus.REFUNDED));
        }

        @Test
        @DisplayName("PAID狀態應該可以轉換到SHIPPED或REFUNDED")
        void paidStatusShouldTransitionToShippedOrRefunded() {
            // Given
            OrderStatus paid = OrderStatus.PAID;

            // Then
            assertTrue(paid.canTransitionTo(OrderStatus.SHIPPED));
            assertTrue(paid.canTransitionTo(OrderStatus.REFUNDED));
            assertFalse(paid.canTransitionTo(OrderStatus.PENDING));
            assertFalse(paid.canTransitionTo(OrderStatus.CONFIRMED));
            assertFalse(paid.canTransitionTo(OrderStatus.PAID));
            assertFalse(paid.canTransitionTo(OrderStatus.DELIVERED));
            assertFalse(paid.canTransitionTo(OrderStatus.CANCELLED));
        }

        @Test
        @DisplayName("SHIPPED狀態應該只能轉換到DELIVERED")
        void shippedStatusShouldOnlyTransitionToDelivered() {
            // Given
            OrderStatus shipped = OrderStatus.SHIPPED;

            // Then
            assertTrue(shipped.canTransitionTo(OrderStatus.DELIVERED));
            assertFalse(shipped.canTransitionTo(OrderStatus.PENDING));
            assertFalse(shipped.canTransitionTo(OrderStatus.CONFIRMED));
            assertFalse(shipped.canTransitionTo(OrderStatus.PAID));
            assertFalse(shipped.canTransitionTo(OrderStatus.SHIPPED));
            assertFalse(shipped.canTransitionTo(OrderStatus.CANCELLED));
            assertFalse(shipped.canTransitionTo(OrderStatus.REFUNDED));
        }

        @Test
        @DisplayName("DELIVERED狀態應該可以轉換到REFUNDED")
        void deliveredStatusShouldTransitionToRefunded() {
            // Given
            OrderStatus delivered = OrderStatus.DELIVERED;

            // Then
            assertTrue(delivered.canTransitionTo(OrderStatus.REFUNDED));
            assertFalse(delivered.canTransitionTo(OrderStatus.PENDING));
            assertFalse(delivered.canTransitionTo(OrderStatus.CONFIRMED));
            assertFalse(delivered.canTransitionTo(OrderStatus.PAID));
            assertFalse(delivered.canTransitionTo(OrderStatus.SHIPPED));
            assertFalse(delivered.canTransitionTo(OrderStatus.DELIVERED));
            assertFalse(delivered.canTransitionTo(OrderStatus.CANCELLED));
        }

        @Test
        @DisplayName("CANCELLED狀態不應該轉換到任何其他狀態")
        void cancelledStatusShouldNotTransitionToAnyStatus() {
            // Given
            OrderStatus cancelled = OrderStatus.CANCELLED;

            // Then
            assertFalse(cancelled.canTransitionTo(OrderStatus.PENDING));
            assertFalse(cancelled.canTransitionTo(OrderStatus.CONFIRMED));
            assertFalse(cancelled.canTransitionTo(OrderStatus.PAID));
            assertFalse(cancelled.canTransitionTo(OrderStatus.SHIPPED));
            assertFalse(cancelled.canTransitionTo(OrderStatus.DELIVERED));
            assertFalse(cancelled.canTransitionTo(OrderStatus.CANCELLED));
            assertFalse(cancelled.canTransitionTo(OrderStatus.REFUNDED));
        }

        @Test
        @DisplayName("REFUNDED狀態不應該轉換到任何其他狀態")
        void refundedStatusShouldNotTransitionToAnyStatus() {
            // Given
            OrderStatus refunded = OrderStatus.REFUNDED;

            // Then
            assertFalse(refunded.canTransitionTo(OrderStatus.PENDING));
            assertFalse(refunded.canTransitionTo(OrderStatus.CONFIRMED));
            assertFalse(refunded.canTransitionTo(OrderStatus.PAID));
            assertFalse(refunded.canTransitionTo(OrderStatus.SHIPPED));
            assertFalse(refunded.canTransitionTo(OrderStatus.DELIVERED));
            assertFalse(refunded.canTransitionTo(OrderStatus.CANCELLED));
            assertFalse(refunded.canTransitionTo(OrderStatus.REFUNDED));
        }
    }

    @Nested
    @DisplayName("最終狀態測試")
    class FinalStatusTest {

        @Test
        @DisplayName("DELIVERED應該是最終狀態")
        void deliveredShouldBeFinalStatus() {
            assertTrue(OrderStatus.DELIVERED.isFinalStatus());
        }

        @Test
        @DisplayName("CANCELLED應該是最終狀態")
        void cancelledShouldBeFinalStatus() {
            assertTrue(OrderStatus.CANCELLED.isFinalStatus());
        }

        @Test
        @DisplayName("REFUNDED應該是最終狀態")
        void refundedShouldBeFinalStatus() {
            assertTrue(OrderStatus.REFUNDED.isFinalStatus());
        }

        @ParameterizedTest
        @EnumSource(value = OrderStatus.class, names = {"PENDING", "CONFIRMED", "PAID", "SHIPPED"})
        @DisplayName("進行中的狀態不應該是最終狀態")
        void inProgressStatusesShouldNotBeFinalStatus(OrderStatus status) {
            assertFalse(status.isFinalStatus());
        }
    }

    @Nested
    @DisplayName("可取消狀態測試")
    class CancellableStatusTest {

        @Test
        @DisplayName("PENDING狀態應該可以取消")
        void pendingStatusShouldBeCancellable() {
            assertTrue(OrderStatus.PENDING.isCancellable());
        }

        @Test
        @DisplayName("CONFIRMED狀態應該可以取消")
        void confirmedStatusShouldBeCancellable() {
            assertTrue(OrderStatus.CONFIRMED.isCancellable());
        }

        @ParameterizedTest
        @EnumSource(value = OrderStatus.class, names = {"PAID", "SHIPPED", "DELIVERED", "CANCELLED", "REFUNDED"})
        @DisplayName("已付款後的狀態不應該可以取消")
        void postPaymentStatusesShouldNotBeCancellable(OrderStatus status) {
            assertFalse(status.isCancellable());
        }
    }

    @Nested
    @DisplayName("狀態描述測試")
    class StatusDescriptionTest {

        @Test
        @DisplayName("所有狀態都應該有中文描述")
        void allStatusesShouldHaveChineseDescription() {
            assertEquals("待處理", OrderStatus.PENDING.getDescription());
            assertEquals("已確認", OrderStatus.CONFIRMED.getDescription());
            assertEquals("已付款", OrderStatus.PAID.getDescription());
            assertEquals("已出貨", OrderStatus.SHIPPED.getDescription());
            assertEquals("已送達", OrderStatus.DELIVERED.getDescription());
            assertEquals("已取消", OrderStatus.CANCELLED.getDescription());
            assertEquals("已退款", OrderStatus.REFUNDED.getDescription());
        }

        @ParameterizedTest
        @EnumSource(OrderStatus.class)
        @DisplayName("所有狀態描述都不應該為空")
        void allStatusDescriptionsShouldNotBeEmpty(OrderStatus status) {
            assertNotNull(status.getDescription());
            assertFalse(status.getDescription().trim().isEmpty());
        }
    }

    @Nested
    @DisplayName("狀態轉換邏輯完整性測試")
    class StatusTransitionCompletenessTest {

        @Test
        @DisplayName("每個狀態都不應該能轉換到自己")
        void noStatusShouldTransitionToItself() {
            for (OrderStatus status : OrderStatus.values()) {
                assertFalse(status.canTransitionTo(status), 
                    "Status " + status + " should not transition to itself");
            }
        }

        @Test
        @DisplayName("驗證完整的訂單生命週期路徑")
        void shouldValidateCompleteOrderLifecyclePath() {
            // 正常完成路徑: PENDING -> CONFIRMED -> PAID -> SHIPPED -> DELIVERED
            assertTrue(OrderStatus.PENDING.canTransitionTo(OrderStatus.CONFIRMED));
            assertTrue(OrderStatus.CONFIRMED.canTransitionTo(OrderStatus.PAID));
            assertTrue(OrderStatus.PAID.canTransitionTo(OrderStatus.SHIPPED));
            assertTrue(OrderStatus.SHIPPED.canTransitionTo(OrderStatus.DELIVERED));

            // 取消路徑: PENDING -> CANCELLED 或 CONFIRMED -> CANCELLED
            assertTrue(OrderStatus.PENDING.canTransitionTo(OrderStatus.CANCELLED));
            assertTrue(OrderStatus.CONFIRMED.canTransitionTo(OrderStatus.CANCELLED));

            // 退款路徑: PAID -> REFUNDED 或 DELIVERED -> REFUNDED
            assertTrue(OrderStatus.PAID.canTransitionTo(OrderStatus.REFUNDED));
            assertTrue(OrderStatus.DELIVERED.canTransitionTo(OrderStatus.REFUNDED));
        }

        @Test
        @DisplayName("驗證不允許的狀態跳躍")
        void shouldValidateDisallowedStatusJumps() {
            // 不能直接從 PENDING 跳到 PAID
            assertFalse(OrderStatus.PENDING.canTransitionTo(OrderStatus.PAID));
            
            // 不能直接從 PENDING 跳到 SHIPPED
            assertFalse(OrderStatus.PENDING.canTransitionTo(OrderStatus.SHIPPED));
            
            // 不能直接從 CONFIRMED 跳到 SHIPPED
            assertFalse(OrderStatus.CONFIRMED.canTransitionTo(OrderStatus.SHIPPED));
            
            // 不能從 SHIPPED 回到 PAID
            assertFalse(OrderStatus.SHIPPED.canTransitionTo(OrderStatus.PAID));
        }
    }
}