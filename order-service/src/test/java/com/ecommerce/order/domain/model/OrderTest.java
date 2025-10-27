package com.ecommerce.order.domain.model;

import com.ecommerce.common.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 訂單實體測試
 * 測試訂單的核心業務邏輯和狀態轉換
 */
@DisplayName("訂單實體測試")
class OrderTest {

    private static final String CUSTOMER_ID = "CUST-001";
    private static final String CUSTOMER_NAME = "Rex Wang";
    private static final String CUSTOMER_EMAIL = "rex@example.com";
    private static final String SHIPPING_ADDRESS = "台北市信義區信義路五段7號";
    private static final String BILLING_ADDRESS = "台北市信義區信義路五段7號";

    @Nested
    @DisplayName("訂單建立測試")
    class OrderCreationTest {

        @Test
        @DisplayName("應該成功建立有效的訂單")
        void shouldCreateValidOrder() {
            // When
            Order order = Order.create(CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_EMAIL, 
                                     SHIPPING_ADDRESS, BILLING_ADDRESS);

            // Then
            assertNotNull(order.getOrderId());
            assertEquals(CUSTOMER_ID, order.getCustomerId());
            assertEquals(CUSTOMER_NAME, order.getCustomerName());
            assertEquals(CUSTOMER_EMAIL, order.getCustomerEmail());
            assertEquals(SHIPPING_ADDRESS, order.getShippingAddress());
            assertEquals(BILLING_ADDRESS, order.getBillingAddress());
            assertEquals(OrderStatus.PENDING, order.getStatus());
            assertEquals(BigDecimal.ZERO, order.getTotalAmount());
            assertEquals(BigDecimal.ZERO, order.getFinalAmount());
            assertNotNull(order.getOrderDate());
            assertTrue(order.getOrderItems().isEmpty());
        }

        @Test
        @DisplayName("當客戶ID為空時應該拋出異常")
        void shouldThrowExceptionWhenCustomerIdIsEmpty() {
            // When & Then
            assertThrows(ValidationException.class, () -> 
                Order.create("", CUSTOMER_NAME, CUSTOMER_EMAIL, SHIPPING_ADDRESS, BILLING_ADDRESS));
        }

        @Test
        @DisplayName("當客戶姓名為空時應該拋出異常")
        void shouldThrowExceptionWhenCustomerNameIsEmpty() {
            // When & Then
            assertThrows(ValidationException.class, () -> 
                Order.create(CUSTOMER_ID, "", CUSTOMER_EMAIL, SHIPPING_ADDRESS, BILLING_ADDRESS));
        }

        @Test
        @DisplayName("當電子郵件格式無效時應該拋出異常")
        void shouldThrowExceptionWhenEmailIsInvalid() {
            // When & Then
            assertThrows(ValidationException.class, () -> 
                Order.create(CUSTOMER_ID, CUSTOMER_NAME, "invalid-email", SHIPPING_ADDRESS, BILLING_ADDRESS));
        }

        @Test
        @DisplayName("當配送地址為空時應該拋出異常")
        void shouldThrowExceptionWhenShippingAddressIsEmpty() {
            // When & Then
            assertThrows(ValidationException.class, () -> 
                Order.create(CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_EMAIL, "", BILLING_ADDRESS));
        }
    }

    @Nested
    @DisplayName("訂單項目管理測試")
    class OrderItemManagementTest {

        private Order order;
        private OrderItem orderItem;

        void setUp() {
            order = Order.create(CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_EMAIL, 
                               SHIPPING_ADDRESS, BILLING_ADDRESS);
            orderItem = OrderItem.create("PROD-001", "iPhone 17 Pro", 
                                       new BigDecimal("35900"), 1, "256GB 黑色");
        }

        @Test
        @DisplayName("應該成功新增訂單項目")
        void shouldAddOrderItemSuccessfully() {
            // Given
            setUp();

            // When
            order.addOrderItem(orderItem);

            // Then
            assertEquals(1, order.getOrderItems().size());
            assertEquals(new BigDecimal("35900"), order.getTotalAmount());
            assertEquals(new BigDecimal("35900"), order.getFinalAmount());
            assertEquals(order.getOrderId(), orderItem.getOrderId());
        }

        @Test
        @DisplayName("當訂單項目為null時應該拋出異常")
        void shouldThrowExceptionWhenOrderItemIsNull() {
            // Given
            setUp();

            // When & Then
            assertThrows(ValidationException.class, () -> order.addOrderItem(null));
        }

        @Test
        @DisplayName("當訂單狀態不允許修改時應該拋出異常")
        void shouldThrowExceptionWhenOrderStatusDoesNotAllowModification() {
            // Given
            setUp();
            order.addOrderItem(orderItem);
            order.confirm();

            // When & Then
            OrderItem newItem = OrderItem.create("PROD-002", "iPad Pro", 
                                               new BigDecimal("25900"), 1, "11吋");
            assertThrows(ValidationException.class, () -> order.addOrderItem(newItem));
        }

        @Test
        @DisplayName("應該成功移除訂單項目")
        void shouldRemoveOrderItemSuccessfully() {
            // Given
            setUp();
            order.addOrderItem(orderItem);
            String orderItemId = orderItem.getOrderItemId();

            // When
            order.removeOrderItem(orderItemId);

            // Then
            assertTrue(order.getOrderItems().isEmpty());
            assertEquals(BigDecimal.ZERO, order.getTotalAmount());
            assertEquals(BigDecimal.ZERO, order.getFinalAmount());
        }

        @Test
        @DisplayName("應該成功更新訂單項目數量")
        void shouldUpdateOrderItemQuantitySuccessfully() {
            // Given
            setUp();
            order.addOrderItem(orderItem);
            String orderItemId = orderItem.getOrderItemId();

            // When
            order.updateOrderItemQuantity(orderItemId, 2);

            // Then
            assertEquals(2, orderItem.getQuantity());
            assertEquals(new BigDecimal("71800"), order.getTotalAmount());
            assertEquals(new BigDecimal("71800"), order.getFinalAmount());
        }
    }

    @Nested
    @DisplayName("訂單狀態轉換測試")
    class OrderStatusTransitionTest {

        private Order order;

        void setUp() {
            order = Order.create(CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_EMAIL, 
                               SHIPPING_ADDRESS, BILLING_ADDRESS);
            OrderItem orderItem = OrderItem.create("PROD-001", "iPhone 17 Pro", 
                                                 new BigDecimal("35900"), 1, "256GB 黑色");
            order.addOrderItem(orderItem);
        }

        @Test
        @DisplayName("應該成功確認訂單")
        void shouldConfirmOrderSuccessfully() {
            // Given
            setUp();

            // When
            order.confirm();

            // Then
            assertEquals(OrderStatus.CONFIRMED, order.getStatus());
            assertNotNull(order.getConfirmedDate());
        }

        @Test
        @DisplayName("當訂單沒有項目時確認應該拋出異常")
        void shouldThrowExceptionWhenConfirmingEmptyOrder() {
            // Given
            Order emptyOrder = Order.create(CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_EMAIL, 
                                          SHIPPING_ADDRESS, BILLING_ADDRESS);

            // When & Then
            assertThrows(ValidationException.class, () -> emptyOrder.confirm());
        }

        @Test
        @DisplayName("應該成功標記為已付款")
        void shouldMarkAsPaidSuccessfully() {
            // Given
            setUp();
            order.confirm();

            // When
            order.markAsPaid("信用卡");

            // Then
            assertEquals(OrderStatus.PAID, order.getStatus());
            assertEquals("信用卡", order.getPaymentMethod());
            assertNotNull(order.getPaidDate());
        }

        @Test
        @DisplayName("應該成功出貨")
        void shouldShipOrderSuccessfully() {
            // Given
            setUp();
            order.confirm();
            order.markAsPaid("信用卡");

            // When
            order.ship();

            // Then
            assertEquals(OrderStatus.SHIPPED, order.getStatus());
            assertNotNull(order.getShippedDate());
        }

        @Test
        @DisplayName("應該成功送達")
        void shouldDeliverOrderSuccessfully() {
            // Given
            setUp();
            order.confirm();
            order.markAsPaid("信用卡");
            order.ship();

            // When
            order.deliver();

            // Then
            assertEquals(OrderStatus.DELIVERED, order.getStatus());
            assertNotNull(order.getDeliveredDate());
            assertTrue(order.isCompleted());
        }

        @Test
        @DisplayName("應該成功取消訂單")
        void shouldCancelOrderSuccessfully() {
            // Given
            setUp();
            String reason = "客戶要求取消";

            // When
            order.cancel(reason);

            // Then
            assertEquals(OrderStatus.CANCELLED, order.getStatus());
            assertEquals(reason, order.getCancellationReason());
            assertNotNull(order.getCancelledDate());
            assertTrue(order.isCancelled());
        }

        @Test
        @DisplayName("當訂單狀態不允許取消時應該拋出異常")
        void shouldThrowExceptionWhenCancellingNonCancellableOrder() {
            // Given
            setUp();
            order.confirm();
            order.markAsPaid("信用卡");
            order.ship();

            // When & Then
            assertThrows(ValidationException.class, () -> order.cancel("測試取消"));
        }

        @Test
        @DisplayName("應該成功退款")
        void shouldRefundOrderSuccessfully() {
            // Given
            setUp();
            order.confirm();
            order.markAsPaid("信用卡");

            // When
            order.refund();

            // Then
            assertEquals(OrderStatus.REFUNDED, order.getStatus());
            assertTrue(order.isRefunded());
        }

        @Test
        @DisplayName("當訂單狀態不允許退款時應該拋出異常")
        void shouldThrowExceptionWhenRefundingInvalidStatus() {
            // Given
            setUp();

            // When & Then
            assertThrows(ValidationException.class, () -> order.refund());
        }
    }

    @Nested
    @DisplayName("訂單金額計算測試")
    class OrderAmountCalculationTest {

        @Test
        @DisplayName("應該正確計算運費和稅額")
        void shouldCalculateShippingAndTaxCorrectly() {
            // Given
            Order order = Order.create(CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_EMAIL, 
                                     SHIPPING_ADDRESS, BILLING_ADDRESS);
            OrderItem orderItem = OrderItem.create("PROD-001", "iPhone 17 Pro", 
                                                 new BigDecimal("35900"), 1, "256GB 黑色");
            order.addOrderItem(orderItem);

            // When
            order.setShippingFee(new BigDecimal("60"));
            order.setTaxAmount(new BigDecimal("1798"));

            // Then
            assertEquals(new BigDecimal("35900"), order.getTotalAmount());
            assertEquals(new BigDecimal("60"), order.getShippingFee());
            assertEquals(new BigDecimal("1798"), order.getTaxAmount());
            assertEquals(new BigDecimal("37758"), order.getFinalAmount());
        }

        @Test
        @DisplayName("當運費為負數時應該拋出異常")
        void shouldThrowExceptionWhenShippingFeeIsNegative() {
            // Given
            Order order = Order.create(CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_EMAIL, 
                                     SHIPPING_ADDRESS, BILLING_ADDRESS);

            // When & Then
            assertThrows(ValidationException.class, () -> 
                order.setShippingFee(new BigDecimal("-10")));
        }

        @Test
        @DisplayName("當稅額為負數時應該拋出異常")
        void shouldThrowExceptionWhenTaxAmountIsNegative() {
            // Given
            Order order = Order.create(CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_EMAIL, 
                                     SHIPPING_ADDRESS, BILLING_ADDRESS);

            // When & Then
            assertThrows(ValidationException.class, () -> 
                order.setTaxAmount(new BigDecimal("-10")));
        }
    }

    @Nested
    @DisplayName("訂單查詢方法測試")
    class OrderQueryMethodTest {

        @Test
        @DisplayName("應該正確判斷是否可以修改項目")
        void shouldCorrectlyDetermineIfCanModifyItems() {
            // Given
            Order order = Order.create(CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_EMAIL, 
                                     SHIPPING_ADDRESS, BILLING_ADDRESS);

            // Then
            assertTrue(order.canModifyItems());

            // When
            OrderItem orderItem = OrderItem.create("PROD-001", "iPhone 17 Pro", 
                                                 new BigDecimal("35900"), 1, "256GB 黑色");
            order.addOrderItem(orderItem);
            order.confirm();

            // Then
            assertFalse(order.canModifyItems());
        }

        @Test
        @DisplayName("應該正確計算訂單項目總數")
        void shouldCorrectlyCalculateTotalItemCount() {
            // Given
            Order order = Order.create(CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_EMAIL, 
                                     SHIPPING_ADDRESS, BILLING_ADDRESS);
            OrderItem item1 = OrderItem.create("PROD-001", "iPhone 17 Pro", 
                                             new BigDecimal("35900"), 2, "256GB 黑色");
            OrderItem item2 = OrderItem.create("PROD-002", "iPad Pro", 
                                             new BigDecimal("25900"), 1, "11吋");

            // When
            order.addOrderItem(item1);
            order.addOrderItem(item2);

            // Then
            assertEquals(3, order.getTotalItemCount());
        }

        @Test
        @DisplayName("應該成功找到訂單項目")
        void shouldFindOrderItemSuccessfully() {
            // Given
            Order order = Order.create(CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_EMAIL, 
                                     SHIPPING_ADDRESS, BILLING_ADDRESS);
            OrderItem orderItem = OrderItem.create("PROD-001", "iPhone 17 Pro", 
                                                 new BigDecimal("35900"), 1, "256GB 黑色");
            order.addOrderItem(orderItem);

            // When
            OrderItem foundItem = order.findOrderItem(orderItem.getOrderItemId());

            // Then
            assertEquals(orderItem, foundItem);
        }

        @Test
        @DisplayName("當找不到訂單項目時應該拋出異常")
        void shouldThrowExceptionWhenOrderItemNotFound() {
            // Given
            Order order = Order.create(CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_EMAIL, 
                                     SHIPPING_ADDRESS, BILLING_ADDRESS);

            // When & Then
            assertThrows(ValidationException.class, () -> 
                order.findOrderItem("NON-EXISTENT-ID"));
        }
    }
}