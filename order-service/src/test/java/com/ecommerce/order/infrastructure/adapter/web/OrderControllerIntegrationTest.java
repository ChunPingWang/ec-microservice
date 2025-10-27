package com.ecommerce.order.infrastructure.adapter.web;

import com.ecommerce.order.application.dto.CreateOrderRequest;
import com.ecommerce.order.application.port.in.OrderManagementUseCase;
import com.ecommerce.order.application.dto.OrderDto;
import com.ecommerce.order.domain.model.OrderStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 訂單控制器整合測試
 * 測試 REST API 端點的完整流程
 */
@WebMvcTest(OrderController.class)
@DisplayName("訂單控制器整合測試")
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderManagementUseCase orderManagementUseCase;

    private static final String CUSTOMER_ID = "CUST-001";
    private static final String ORDER_ID = "ORDER-001";

    @Nested
    @DisplayName("訂單建立 API 測試")
    class CreateOrderApiTest {

        @Test
        @DisplayName("應該成功建立訂單")
        void shouldCreateOrderSuccessfully() throws Exception {
            // Given
            CreateOrderRequest request = createOrderRequest();
            OrderDto expectedOrder = createOrderDto();
            
            when(orderManagementUseCase.createOrderFromCart(eq(CUSTOMER_ID), any(CreateOrderRequest.class)))
                .thenReturn(expectedOrder);

            // When & Then
            mockMvc.perform(post("/api/v1/orders")
                    .header("X-Customer-Id", CUSTOMER_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderId").value(ORDER_ID))
                .andExpect(jsonPath("$.data.customerId").value(CUSTOMER_ID))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.totalAmount").value(35900))
                .andExpect(jsonPath("$.data.finalAmount").value(37758));
        }

        @Test
        @DisplayName("當請求無效時應該回傳400錯誤")
        void shouldReturn400WhenRequestIsInvalid() throws Exception {
            // Given
            CreateOrderRequest invalidRequest = new CreateOrderRequest();
            // 不設定必要欄位

            // When & Then
            mockMvc.perform(post("/api/v1/orders")
                    .header("X-Customer-Id", CUSTOMER_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("當缺少客戶ID標頭時應該回傳400錯誤")
        void shouldReturn400WhenCustomerIdHeaderMissing() throws Exception {
            // Given
            CreateOrderRequest request = createOrderRequest();

            // When & Then
            mockMvc.perform(post("/api/v1/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("當庫存不足時應該回傳400錯誤")
        void shouldReturn400WhenInsufficientStock() throws Exception {
            // Given
            CreateOrderRequest request = createOrderRequest();
            
            when(orderManagementUseCase.createOrderFromCart(eq(CUSTOMER_ID), any(CreateOrderRequest.class)))
                .thenThrow(new IllegalStateException("Insufficient stock"));

            // When & Then
            mockMvc.perform(post("/api/v1/orders")
                    .header("X-Customer-Id", CUSTOMER_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.message").exists());
        }
    }

    @Nested
    @DisplayName("訂單狀態更新 API 測試")
    class OrderStatusUpdateApiTest {

        @Test
        @DisplayName("應該成功確認訂單")
        void shouldConfirmOrderSuccessfully() throws Exception {
            // Given
            OrderDto confirmedOrder = createOrderDto();
            confirmedOrder.setStatus(OrderStatus.CONFIRMED);
            
            when(orderManagementUseCase.confirmOrder(ORDER_ID, CUSTOMER_ID))
                .thenReturn(confirmedOrder);

            // When & Then
            mockMvc.perform(patch("/api/v1/orders/{orderId}/confirm", ORDER_ID)
                    .header("X-Customer-Id", CUSTOMER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"));
        }

        @Test
        @DisplayName("應該成功標記訂單為已付款")
        void shouldMarkOrderAsPaidSuccessfully() throws Exception {
            // Given
            OrderDto paidOrder = createOrderDto();
            paidOrder.setStatus(OrderStatus.PAID);
            paidOrder.setPaymentMethod("信用卡");
            
            when(orderManagementUseCase.markOrderAsPaid(ORDER_ID, CUSTOMER_ID, "信用卡"))
                .thenReturn(paidOrder);

            // When & Then
            mockMvc.perform(patch("/api/v1/orders/{orderId}/pay", ORDER_ID)
                    .header("X-Customer-Id", CUSTOMER_ID)
                    .param("paymentMethod", "信用卡"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PAID"))
                .andExpect(jsonPath("$.data.paymentMethod").value("信用卡"));
        }

        @Test
        @DisplayName("應該成功取消訂單")
        void shouldCancelOrderSuccessfully() throws Exception {
            // Given
            OrderDto cancelledOrder = createOrderDto();
            cancelledOrder.setStatus(OrderStatus.CANCELLED);
            
            when(orderManagementUseCase.cancelOrder(ORDER_ID, CUSTOMER_ID, "客戶要求取消"))
                .thenReturn(cancelledOrder);

            // When & Then
            mockMvc.perform(patch("/api/v1/orders/{orderId}/cancel", ORDER_ID)
                    .header("X-Customer-Id", CUSTOMER_ID)
                    .param("reason", "客戶要求取消"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
        }

        @Test
        @DisplayName("當訂單不存在時應該回傳404錯誤")
        void shouldReturn404WhenOrderNotFound() throws Exception {
            // Given
            when(orderManagementUseCase.confirmOrder(ORDER_ID, CUSTOMER_ID))
                .thenThrow(new RuntimeException("Order not found"));

            // When & Then
            mockMvc.perform(patch("/api/v1/orders/{orderId}/confirm", ORDER_ID)
                    .header("X-Customer-Id", CUSTOMER_ID))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("當狀態轉換無效時應該回傳400錯誤")
        void shouldReturn400WhenStatusTransitionInvalid() throws Exception {
            // Given
            when(orderManagementUseCase.confirmOrder(ORDER_ID, CUSTOMER_ID))
                .thenThrow(new IllegalStateException("Invalid status transition"));

            // When & Then
            mockMvc.perform(patch("/api/v1/orders/{orderId}/confirm", ORDER_ID)
                    .header("X-Customer-Id", CUSTOMER_ID))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("訂單查詢 API 測試")
    class OrderQueryApiTest {

        @Test
        @DisplayName("應該成功取得訂單詳細資訊")
        void shouldGetOrderSuccessfully() throws Exception {
            // Given
            OrderDto order = createOrderDto();
            when(orderManagementUseCase.getOrder(ORDER_ID, CUSTOMER_ID)).thenReturn(order);

            // When & Then
            mockMvc.perform(get("/api/v1/orders/{orderId}", ORDER_ID)
                    .header("X-Customer-Id", CUSTOMER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderId").value(ORDER_ID))
                .andExpect(jsonPath("$.data.customerId").value(CUSTOMER_ID));
        }

        @Test
        @DisplayName("應該成功取得客戶所有訂單")
        void shouldGetCustomerOrdersSuccessfully() throws Exception {
            // Given
            List<OrderDto> orders = Arrays.asList(createOrderDto(), createOrderDto());
            when(orderManagementUseCase.getCustomerOrders(CUSTOMER_ID)).thenReturn(orders);

            // When & Then
            mockMvc.perform(get("/api/v1/orders")
                    .header("X-Customer-Id", CUSTOMER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
        }

        @Test
        @DisplayName("應該成功取得客戶指定狀態的訂單")
        void shouldGetCustomerOrdersByStatusSuccessfully() throws Exception {
            // Given
            List<OrderDto> orders = Arrays.asList(createOrderDto());
            when(orderManagementUseCase.getCustomerOrdersByStatus(CUSTOMER_ID, OrderStatus.PENDING))
                .thenReturn(orders);

            // When & Then
            mockMvc.perform(get("/api/v1/orders")
                    .header("X-Customer-Id", CUSTOMER_ID)
                    .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].status").value("PENDING"));
        }

        @Test
        @DisplayName("應該成功取得客戶最新訂單")
        void shouldGetLatestCustomerOrderSuccessfully() throws Exception {
            // Given
            OrderDto order = createOrderDto();
            when(orderManagementUseCase.getLatestCustomerOrder(CUSTOMER_ID)).thenReturn(order);

            // When & Then
            mockMvc.perform(get("/api/v1/orders/latest")
                    .header("X-Customer-Id", CUSTOMER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderId").value(ORDER_ID));
        }

        @Test
        @DisplayName("當客戶沒有訂單時應該回傳404錯誤")
        void shouldReturn404WhenCustomerHasNoOrders() throws Exception {
            // Given
            when(orderManagementUseCase.getLatestCustomerOrder(CUSTOMER_ID))
                .thenThrow(new RuntimeException("No orders found"));

            // When & Then
            mockMvc.perform(get("/api/v1/orders/latest")
                    .header("X-Customer-Id", CUSTOMER_ID))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("訂單取消檢查 API 測試")
    class OrderCancellationCheckApiTest {

        @Test
        @DisplayName("應該正確回傳訂單是否可以取消")
        void shouldReturnWhetherOrderCanBeCancelled() throws Exception {
            // Given
            when(orderManagementUseCase.canCancelOrder(ORDER_ID, CUSTOMER_ID)).thenReturn(true);

            // When & Then
            mockMvc.perform(get("/api/v1/orders/{orderId}/can-cancel", ORDER_ID)
                    .header("X-Customer-Id", CUSTOMER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        @DisplayName("當訂單不可取消時應該回傳false")
        void shouldReturnFalseWhenOrderCannotBeCancelled() throws Exception {
            // Given
            when(orderManagementUseCase.canCancelOrder(ORDER_ID, CUSTOMER_ID)).thenReturn(false);

            // When & Then
            mockMvc.perform(get("/api/v1/orders/{orderId}/can-cancel", ORDER_ID)
                    .header("X-Customer-Id", CUSTOMER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(false));
        }
    }

    @Nested
    @DisplayName("管理員 API 測試")
    class AdminApiTest {

        @Test
        @DisplayName("管理員應該成功出貨訂單")
        void adminShouldShipOrderSuccessfully() throws Exception {
            // Given
            OrderDto shippedOrder = createOrderDto();
            shippedOrder.setStatus(OrderStatus.SHIPPED);
            
            when(orderManagementUseCase.shipOrder(ORDER_ID)).thenReturn(shippedOrder);

            // When & Then
            mockMvc.perform(patch("/api/v1/admin/orders/{orderId}/ship", ORDER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("SHIPPED"));
        }

        @Test
        @DisplayName("管理員應該成功標記訂單為已送達")
        void adminShouldDeliverOrderSuccessfully() throws Exception {
            // Given
            OrderDto deliveredOrder = createOrderDto();
            deliveredOrder.setStatus(OrderStatus.DELIVERED);
            
            when(orderManagementUseCase.deliverOrder(ORDER_ID)).thenReturn(deliveredOrder);

            // When & Then
            mockMvc.perform(patch("/api/v1/admin/orders/{orderId}/deliver", ORDER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("DELIVERED"));
        }

        @Test
        @DisplayName("管理員應該成功退款訂單")
        void adminShouldRefundOrderSuccessfully() throws Exception {
            // Given
            OrderDto refundedOrder = createOrderDto();
            refundedOrder.setStatus(OrderStatus.REFUNDED);
            
            when(orderManagementUseCase.refundOrder(ORDER_ID, "商品瑕疵")).thenReturn(refundedOrder);

            // When & Then
            mockMvc.perform(patch("/api/v1/admin/orders/{orderId}/refund", ORDER_ID)
                    .param("reason", "商品瑕疵"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("REFUNDED"));
        }

        @Test
        @DisplayName("管理員應該成功取消超時訂單")
        void adminShouldCancelExpiredOrdersSuccessfully() throws Exception {
            // Given
            List<OrderDto> cancelledOrders = Arrays.asList(createOrderDto());
            when(orderManagementUseCase.cancelExpiredOrders(24)).thenReturn(cancelledOrders);

            // When & Then
            mockMvc.perform(post("/api/v1/admin/orders/cancel-expired")
                    .param("timeoutHours", "24"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));
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

    private OrderDto createOrderDto() {
        OrderDto order = new OrderDto();
        order.setOrderId(ORDER_ID);
        order.setCustomerId(CUSTOMER_ID);
        order.setCustomerName("Rex Wang");
        order.setCustomerEmail("rex@example.com");
        order.setShippingAddress("台北市信義區信義路五段7號");
        order.setBillingAddress("台北市信義區信義路五段7號");
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(new BigDecimal("35900"));
        order.setShippingFee(new BigDecimal("60"));
        order.setTaxAmount(new BigDecimal("1798"));
        order.setFinalAmount(new BigDecimal("37758"));
        order.setOrderDate(LocalDateTime.now());
        return order;
    }
}