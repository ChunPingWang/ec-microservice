package com.ecommerce.order.application.usecase;

import com.ecommerce.order.application.dto.CreateOrderRequest;
import com.ecommerce.order.application.dto.OrderDto;
import com.ecommerce.order.application.port.out.CartPersistencePort;
import com.ecommerce.order.application.port.out.OrderEventPort;
import com.ecommerce.order.application.port.out.OrderPersistencePort;
import com.ecommerce.order.application.port.out.ProductServicePort;
import com.ecommerce.order.domain.exception.CartNotFoundException;
import com.ecommerce.order.domain.exception.InvalidOrderStateException;
import com.ecommerce.order.domain.exception.OrderNotFoundException;
import com.ecommerce.order.domain.model.Cart;
import com.ecommerce.order.domain.model.Order;
import com.ecommerce.order.domain.model.OrderItem;
import com.ecommerce.order.domain.model.OrderStatus;
import com.ecommerce.order.domain.service.CartDomainService;
import com.ecommerce.order.domain.service.OrderDomainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 訂單管理服務測試
 * 測試訂單建立流程、庫存驗證和狀態轉換
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("訂單管理服務測試")
class OrderManagementServiceTest {

    @Mock
    private OrderPersistencePort orderPersistencePort;
    @Mock
    private CartPersistencePort cartPersistencePort;
    @Mock
    private ProductServicePort productServicePort;
    @Mock
    private OrderEventPort orderEventPort;
    @Mock
    private OrderDomainService orderDomainService;
    @Mock
    private CartDomainService cartDomainService;

    private OrderManagementService orderManagementService;

    @BeforeEach
    void setUp() {
        orderManagementService = new OrderManagementService(
            orderPersistencePort,
            cartPersistencePort,
            productServicePort,
            orderEventPort,
            orderDomainService,
            cartDomainService
        );
    }

    @Nested
    @DisplayName("從購物車建立訂單測試")
    class CreateOrderFromCartTest {

        private static final String CUSTOMER_ID = "CUST-001";
        private static final String PRODUCT_ID = "PROD-001";
        private static final String PRODUCT_NAME = "iPhone 17 Pro";

        @Test
        @DisplayName("應該成功從購物車建立訂單")
        void shouldCreateOrderFromCartSuccessfully() {
            // Given
            CreateOrderRequest request = createOrderRequest();
            Cart cart = createTestCart();
            Order expectedOrder = createTestOrder();

            when(cartPersistencePort.findByCustomerId(CUSTOMER_ID)).thenReturn(Optional.of(cart));
            when(productServicePort.isProductAvailable(PRODUCT_ID)).thenReturn(true);
            when(productServicePort.hasAvailableStock(PRODUCT_ID, 1)).thenReturn(true);
            when(orderDomainService.calculateShippingFee(any(Order.class))).thenReturn(new BigDecimal("60"));
            when(orderDomainService.calculateTaxAmount(any(Order.class))).thenReturn(new BigDecimal("1798"));
            when(orderPersistencePort.save(any(Order.class))).thenReturn(expectedOrder);

            // When
            OrderDto result = orderManagementService.createOrderFromCart(CUSTOMER_ID, request);

            // Then
            assertNotNull(result);
            assertEquals(expectedOrder.getOrderId(), result.getOrderId());
            assertEquals(CUSTOMER_ID, result.getCustomerId());
            assertEquals(OrderStatus.PENDING, result.getStatus());

            // 驗證庫存預留
            verify(productServicePort).reserveStock(PRODUCT_ID, 1);
            
            // 驗證購物車清空
            verify(cartPersistencePort).save(argThat(savedCart -> savedCart.getCartItems().isEmpty()));
            
            // 驗證事件發布
            verify(orderEventPort).publishOrderCreated(expectedOrder);
        }

        @Test
        @DisplayName("當購物車不存在時應該拋出異常")
        void shouldThrowExceptionWhenCartNotFound() {
            // Given
            CreateOrderRequest request = createOrderRequest();
            when(cartPersistencePort.findByCustomerId(CUSTOMER_ID)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(CartNotFoundException.class, () -> 
                orderManagementService.createOrderFromCart(CUSTOMER_ID, request));
        }

        @Test
        @DisplayName("當商品不可用時應該拋出異常")
        void shouldThrowExceptionWhenProductNotAvailable() {
            // Given
            CreateOrderRequest request = createOrderRequest();
            Cart cart = createTestCart();

            when(cartPersistencePort.findByCustomerId(CUSTOMER_ID)).thenReturn(Optional.of(cart));
            when(productServicePort.isProductAvailable(PRODUCT_ID)).thenReturn(false);

            // When & Then
            assertThrows(InvalidOrderStateException.class, () -> 
                orderManagementService.createOrderFromCart(CUSTOMER_ID, request));
        }

        @Test
        @DisplayName("當庫存不足時應該拋出異常")
        void shouldThrowExceptionWhenInsufficientStock() {
            // Given
            CreateOrderRequest request = createOrderRequest();
            Cart cart = createTestCart();

            when(cartPersistencePort.findByCustomerId(CUSTOMER_ID)).thenReturn(Optional.of(cart));
            when(productServicePort.isProductAvailable(PRODUCT_ID)).thenReturn(true);
            when(productServicePort.hasAvailableStock(PRODUCT_ID, 1)).thenReturn(false);

            // When & Then
            assertThrows(InvalidOrderStateException.class, () -> 
                orderManagementService.createOrderFromCart(CUSTOMER_ID, request));
        }

        @Test
        @DisplayName("當購物車驗證失敗時應該拋出異常")
        void shouldThrowExceptionWhenCartValidationFails() {
            // Given
            CreateOrderRequest request = createOrderRequest();
            Cart cart = createTestCart();

            when(cartPersistencePort.findByCustomerId(CUSTOMER_ID)).thenReturn(Optional.of(cart));
            doThrow(new InvalidOrderStateException("Cart validation failed"))
                .when(cartDomainService).validateCartForOrder(cart);

            // When & Then
            assertThrows(InvalidOrderStateException.class, () -> 
                orderManagementService.createOrderFromCart(CUSTOMER_ID, request));
        }
    }

    @Nested
    @DisplayName("訂單狀態轉換測試")
    class OrderStatusTransitionTest {

        private static final String ORDER_ID = "ORDER-001";
        private static final String CUSTOMER_ID = "CUST-001";

        @Test
        @DisplayName("應該成功確認訂單")
        void shouldConfirmOrderSuccessfully() {
            // Given
            Order order = createTestOrder();
            when(orderPersistencePort.findById(ORDER_ID)).thenReturn(Optional.of(order));
            when(orderPersistencePort.save(any(Order.class))).thenReturn(order);

            // When
            OrderDto result = orderManagementService.confirmOrder(ORDER_ID, CUSTOMER_ID);

            // Then
            assertEquals(OrderStatus.CONFIRMED, result.getStatus());
            verify(orderEventPort).publishOrderConfirmed(order);
        }

        @Test
        @DisplayName("應該成功標記訂單為已付款")
        void shouldMarkOrderAsPaidSuccessfully() {
            // Given
            Order order = createTestOrder();
            order.confirm();
            String paymentMethod = "信用卡";

            when(orderPersistencePort.findById(ORDER_ID)).thenReturn(Optional.of(order));
            when(orderPersistencePort.save(any(Order.class))).thenReturn(order);

            // When
            OrderDto result = orderManagementService.markOrderAsPaid(ORDER_ID, CUSTOMER_ID, paymentMethod);

            // Then
            assertEquals(OrderStatus.PAID, result.getStatus());
            assertEquals(paymentMethod, result.getPaymentMethod());
            
            // 驗證庫存確認
            verify(productServicePort).confirmStockReservation(anyString(), anyInt());
            
            // 驗證事件發布
            verify(orderEventPort).publishOrderPaid(order);
        }

        @Test
        @DisplayName("應該成功出貨訂單")
        void shouldShipOrderSuccessfully() {
            // Given
            Order order = createTestOrder();
            order.confirm();
            order.markAsPaid("信用卡");

            when(orderPersistencePort.findById(ORDER_ID)).thenReturn(Optional.of(order));
            when(orderPersistencePort.save(any(Order.class))).thenReturn(order);

            // When
            OrderDto result = orderManagementService.shipOrder(ORDER_ID);

            // Then
            assertEquals(OrderStatus.SHIPPED, result.getStatus());
            verify(orderEventPort).publishOrderShipped(order);
        }

        @Test
        @DisplayName("應該成功送達訂單")
        void shouldDeliverOrderSuccessfully() {
            // Given
            Order order = createTestOrder();
            order.confirm();
            order.markAsPaid("信用卡");
            order.ship();

            when(orderPersistencePort.findById(ORDER_ID)).thenReturn(Optional.of(order));
            when(orderPersistencePort.save(any(Order.class))).thenReturn(order);

            // When
            OrderDto result = orderManagementService.deliverOrder(ORDER_ID);

            // Then
            assertEquals(OrderStatus.DELIVERED, result.getStatus());
            verify(orderEventPort).publishOrderDelivered(order);
        }

        @Test
        @DisplayName("應該成功取消訂單並釋放庫存")
        void shouldCancelOrderAndReleaseStock() {
            // Given
            Order order = createTestOrder();
            String reason = "客戶要求取消";

            when(orderPersistencePort.findById(ORDER_ID)).thenReturn(Optional.of(order));
            when(orderPersistencePort.save(any(Order.class))).thenReturn(order);

            // When
            OrderDto result = orderManagementService.cancelOrder(ORDER_ID, CUSTOMER_ID, reason);

            // Then
            assertEquals(OrderStatus.CANCELLED, result.getStatus());
            
            // 驗證庫存釋放
            verify(productServicePort).releaseStockReservation(anyString(), anyInt());
            
            // 驗證事件發布
            verify(orderEventPort).publishOrderCancelled(order, reason);
        }

        @Test
        @DisplayName("當取消已付款訂單時應該拋出異常")
        void shouldThrowExceptionWhenCancellingPaidOrder() {
            // Given
            Order order = createTestOrder();
            order.confirm();
            order.markAsPaid("信用卡");
            String reason = "客戶要求取消";

            when(orderPersistencePort.findById(ORDER_ID)).thenReturn(Optional.of(order));

            // When & Then
            assertThrows(Exception.class, () -> 
                orderManagementService.cancelOrder(ORDER_ID, CUSTOMER_ID, reason));
            
            // 驗證不會釋放庫存（因為取消失敗）
            verify(productServicePort, never()).releaseStockReservation(anyString(), anyInt());
        }

        @Test
        @DisplayName("應該成功退款訂單")
        void shouldRefundOrderSuccessfully() {
            // Given
            Order order = createTestOrder();
            order.confirm();
            order.markAsPaid("信用卡");
            String reason = "商品瑕疵";

            when(orderPersistencePort.findById(ORDER_ID)).thenReturn(Optional.of(order));
            when(orderPersistencePort.save(any(Order.class))).thenReturn(order);

            // When
            OrderDto result = orderManagementService.refundOrder(ORDER_ID, reason);

            // Then
            assertEquals(OrderStatus.REFUNDED, result.getStatus());
            verify(orderEventPort).publishOrderRefunded(order, reason);
        }

        @Test
        @DisplayName("當訂單不存在時應該拋出異常")
        void shouldThrowExceptionWhenOrderNotFound() {
            // Given
            when(orderPersistencePort.findById(ORDER_ID)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(OrderNotFoundException.class, () -> 
                orderManagementService.confirmOrder(ORDER_ID, CUSTOMER_ID));
        }
    }

    @Nested
    @DisplayName("訂單查詢測試")
    class OrderQueryTest {

        private static final String ORDER_ID = "ORDER-001";
        private static final String CUSTOMER_ID = "CUST-001";

        @Test
        @DisplayName("應該成功取得訂單詳細資訊")
        void shouldGetOrderSuccessfully() {
            // Given
            Order order = createTestOrder();
            when(orderPersistencePort.findById(ORDER_ID)).thenReturn(Optional.of(order));

            // When
            OrderDto result = orderManagementService.getOrder(ORDER_ID, CUSTOMER_ID);

            // Then
            assertNotNull(result);
            assertNotNull(result.getOrderId());
            assertEquals(CUSTOMER_ID, result.getCustomerId());
        }

        @Test
        @DisplayName("應該成功取得客戶所有訂單")
        void shouldGetCustomerOrdersSuccessfully() {
            // Given
            List<Order> orders = Arrays.asList(createTestOrder(), createTestOrder());
            when(orderPersistencePort.findByCustomerId(CUSTOMER_ID)).thenReturn(orders);

            // When
            List<OrderDto> result = orderManagementService.getCustomerOrders(CUSTOMER_ID);

            // Then
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("應該成功取得客戶指定狀態的訂單")
        void shouldGetCustomerOrdersByStatusSuccessfully() {
            // Given
            List<Order> orders = Arrays.asList(createTestOrder());
            when(orderPersistencePort.findByCustomerIdAndStatus(CUSTOMER_ID, OrderStatus.PENDING))
                .thenReturn(orders);

            // When
            List<OrderDto> result = orderManagementService.getCustomerOrdersByStatus(CUSTOMER_ID, OrderStatus.PENDING);

            // Then
            assertEquals(1, result.size());
            assertEquals(OrderStatus.PENDING, result.get(0).getStatus());
        }

        @Test
        @DisplayName("應該成功取得客戶指定日期範圍的訂單")
        void shouldGetCustomerOrdersByDateRangeSuccessfully() {
            // Given
            LocalDateTime startDate = LocalDateTime.now().minusDays(7);
            LocalDateTime endDate = LocalDateTime.now();
            List<Order> orders = Arrays.asList(createTestOrder());
            when(orderPersistencePort.findByCustomerIdAndOrderDateBetween(CUSTOMER_ID, startDate, endDate))
                .thenReturn(orders);

            // When
            List<OrderDto> result = orderManagementService.getCustomerOrdersByDateRange(CUSTOMER_ID, startDate, endDate);

            // Then
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("應該成功取得客戶最新訂單")
        void shouldGetLatestCustomerOrderSuccessfully() {
            // Given
            Order order = createTestOrder();
            when(orderPersistencePort.findLatestOrderByCustomerId(CUSTOMER_ID))
                .thenReturn(Optional.of(order));

            // When
            OrderDto result = orderManagementService.getLatestCustomerOrder(CUSTOMER_ID);

            // Then
            assertNotNull(result);
            assertEquals(CUSTOMER_ID, result.getCustomerId());
        }

        @Test
        @DisplayName("當客戶沒有訂單時應該拋出異常")
        void shouldThrowExceptionWhenCustomerHasNoOrders() {
            // Given
            when(orderPersistencePort.findLatestOrderByCustomerId(CUSTOMER_ID))
                .thenReturn(Optional.empty());

            // When & Then
            assertThrows(OrderNotFoundException.class, () -> 
                orderManagementService.getLatestCustomerOrder(CUSTOMER_ID));
        }
    }

    @Nested
    @DisplayName("訂單取消檢查測試")
    class OrderCancellationCheckTest {

        private static final String ORDER_ID = "ORDER-001";
        private static final String CUSTOMER_ID = "CUST-001";

        @Test
        @DisplayName("應該正確檢查訂單是否可以取消")
        void shouldCheckIfOrderCanBeCancelled() {
            // Given
            Order order = createTestOrder();
            when(orderPersistencePort.findById(ORDER_ID)).thenReturn(Optional.of(order));
            when(orderDomainService.canCancelOrder(ORDER_ID)).thenReturn(true);

            // When
            boolean canCancel = orderManagementService.canCancelOrder(ORDER_ID, CUSTOMER_ID);

            // Then
            assertTrue(canCancel);
        }
    }

    @Nested
    @DisplayName("超時訂單處理測試")
    class ExpiredOrderHandlingTest {

        @Test
        @DisplayName("應該成功取消超時訂單並釋放庫存")
        void shouldCancelExpiredOrdersAndReleaseStock() {
            // Given
            List<Order> expiredOrders = Arrays.asList(createTestOrder(), createTestOrder());
            when(orderDomainService.cancelExpiredOrders(24)).thenReturn(expiredOrders);

            // When
            List<OrderDto> result = orderManagementService.cancelExpiredOrders(24);

            // Then
            assertEquals(2, result.size());
            
            // 驗證每個訂單都釋放了庫存
            verify(productServicePort, times(2)).releaseStockReservation(anyString(), anyInt());
            
            // 驗證每個訂單都發布了取消事件
            verify(orderEventPort, times(2)).publishOrderCancelled(any(Order.class), eq("自動取消：超時未付款"));
        }
    }

    // Helper methods for creating test data
    private CreateOrderRequest createOrderRequest() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerName("Rex Wang");
        request.setCustomerEmail("rex@example.com");
        request.setShippingAddress("台北市信義區信義路五段7號");
        request.setBillingAddress("台北市信義區信義路五段7號");
        request.setNotes("測試訂單");
        return request;
    }

    private Cart createTestCart() {
        Cart cart = Cart.create("CUST-001");
        cart.addItem("PROD-001", "iPhone 17 Pro", new BigDecimal("35900"), 1, "256GB 黑色");
        return cart;
    }

    private Order createTestOrder() {
        Order order = Order.create("CUST-001", "Rex Wang", "rex@example.com",
                                 "台北市信義區信義路五段7號", "台北市信義區信義路五段7號");
        OrderItem orderItem = OrderItem.create("PROD-001", "iPhone 17 Pro",
                                             new BigDecimal("35900"), 1, "256GB 黑色");
        order.addOrderItem(orderItem);
        return order;
    }
}