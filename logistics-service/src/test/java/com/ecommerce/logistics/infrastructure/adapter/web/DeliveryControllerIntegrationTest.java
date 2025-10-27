package com.ecommerce.logistics.infrastructure.adapter.web;

import com.ecommerce.logistics.application.dto.*;
import com.ecommerce.logistics.application.port.in.DeliveryManagementUseCase;
import com.ecommerce.logistics.domain.model.DeliveryStatus;
import com.ecommerce.logistics.domain.model.DeliveryType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 配送控制器整合測試
 * 測試台北地址配送建立和配送狀態追蹤的API端點
 */
@WebMvcTest(DeliveryController.class)
@DisplayName("配送控制器整合測試")
class DeliveryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DeliveryManagementUseCase deliveryManagementUseCase;

    @Autowired
    private ObjectMapper objectMapper;

    private CreateDeliveryRequest createDeliveryRequest;
    private DeliveryDto deliveryDto;
    private AddressDto taipeiAddressDto;

    @BeforeEach
    void setUp() {
        // 設定台北地址DTO
        taipeiAddressDto = new AddressDto();
        taipeiAddressDto.setCity("台北市");
        taipeiAddressDto.setDistrict("大安區");
        taipeiAddressDto.setStreet("復興南路一段390號");
        taipeiAddressDto.setPostalCode("106");
        taipeiAddressDto.setRecipientName("王小明");
        taipeiAddressDto.setRecipientPhone("0912345678");

        // 設定建立配送請求
        createDeliveryRequest = new CreateDeliveryRequest();
        createDeliveryRequest.setOrderId("ORDER-001");
        createDeliveryRequest.setCustomerId("CUSTOMER-001");
        createDeliveryRequest.setDeliveryAddress(taipeiAddressDto);
        createDeliveryRequest.setDeliveryType(DeliveryType.STANDARD);

        // 設定配送DTO
        deliveryDto = new DeliveryDto();
        deliveryDto.setDeliveryId("DEL-12345678");
        deliveryDto.setOrderId("ORDER-001");
        deliveryDto.setCustomerId("CUSTOMER-001");
        deliveryDto.setDeliveryAddress(taipeiAddressDto);
        deliveryDto.setDeliveryType(DeliveryType.STANDARD);
        deliveryDto.setStatus(DeliveryStatus.PENDING);
        deliveryDto.setDeliveryFee(BigDecimal.valueOf(60));
        deliveryDto.setEstimatedDeliveryDate(LocalDateTime.now().plusDays(3));
    }

    @Nested
    @DisplayName("台北地址配送建立API測試")
    class TaipeiDeliveryCreationApiTest {

        @Test
        @DisplayName("應該成功建立台北地址的配送請求")
        void shouldCreateTaipeiDeliveryRequestSuccessfully() throws Exception {
            // Given
            when(deliveryManagementUseCase.createDeliveryRequest(any(CreateDeliveryRequest.class)))
                .thenReturn(deliveryDto);

            // When & Then
            mockMvc.perform(post("/api/v1/deliveries")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createDeliveryRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.deliveryId").value("DEL-12345678"))
                .andExpect(jsonPath("$.data.orderId").value("ORDER-001"))
                .andExpect(jsonPath("$.data.customerId").value("CUSTOMER-001"))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.deliveryFee").value(60))
                .andExpect(jsonPath("$.data.deliveryAddress.city").value("台北市"))
                .andExpect(jsonPath("$.data.deliveryAddress.district").value("大安區"))
                .andExpect(jsonPath("$.data.deliveryAddress.recipientName").value("王小明"));
        }

        @Test
        @DisplayName("應該拒絕無效的配送請求")
        void shouldRejectInvalidDeliveryRequest() throws Exception {
            // Given
            CreateDeliveryRequest invalidRequest = new CreateDeliveryRequest();
            invalidRequest.setOrderId(""); // 無效的訂單ID
            invalidRequest.setCustomerId("CUSTOMER-001");
            invalidRequest.setDeliveryAddress(taipeiAddressDto);
            invalidRequest.setDeliveryType(DeliveryType.STANDARD);

            // When & Then
            mockMvc.perform(post("/api/v1/deliveries")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("應該拒絕缺少必要欄位的請求")
        void shouldRejectRequestWithMissingFields() throws Exception {
            // Given
            CreateDeliveryRequest incompleteRequest = new CreateDeliveryRequest();
            incompleteRequest.setOrderId("ORDER-001");
            // 缺少 customerId, deliveryAddress, deliveryType

            // When & Then
            mockMvc.perform(post("/api/v1/deliveries")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(incompleteRequest)))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("配送狀態追蹤API測試")
    class DeliveryStatusTrackingApiTest {

        @Test
        @DisplayName("應該成功更新配送狀態")
        void shouldUpdateDeliveryStatusSuccessfully() throws Exception {
            // Given
            UpdateDeliveryStatusRequest updateRequest = new UpdateDeliveryStatusRequest();
            updateRequest.setDeliveryId("DEL-12345678");
            updateRequest.setStatus(DeliveryStatus.IN_TRANSIT);
            updateRequest.setTrackingNumber("TRK-123456789");
            updateRequest.setNotes("配送已開始");

            DeliveryDto updatedDeliveryDto = new DeliveryDto();
            updatedDeliveryDto.setDeliveryId("DEL-12345678");
            updatedDeliveryDto.setStatus(DeliveryStatus.IN_TRANSIT);
            updatedDeliveryDto.setTrackingNumber("TRK-123456789");

            when(deliveryManagementUseCase.updateDeliveryStatus(any(UpdateDeliveryStatusRequest.class)))
                .thenReturn(updatedDeliveryDto);

            // When & Then
            mockMvc.perform(put("/api/v1/deliveries/DEL-12345678/status")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.deliveryId").value("DEL-12345678"))
                .andExpect(jsonPath("$.data.status").value("IN_TRANSIT"))
                .andExpect(jsonPath("$.data.trackingNumber").value("TRK-123456789"));
        }

        @Test
        @DisplayName("應該成功根據配送ID查詢配送狀態")
        void shouldGetDeliveryStatusByIdSuccessfully() throws Exception {
            // Given
            when(deliveryManagementUseCase.getDeliveryById("DEL-12345678"))
                .thenReturn(deliveryDto);

            // When & Then
            mockMvc.perform(get("/api/v1/deliveries/DEL-12345678"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.deliveryId").value("DEL-12345678"))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.orderId").value("ORDER-001"));
        }

        @Test
        @DisplayName("應該成功根據訂單ID查詢配送狀態")
        void shouldGetDeliveryStatusByOrderIdSuccessfully() throws Exception {
            // Given
            when(deliveryManagementUseCase.getDeliveryByOrderId("ORDER-001"))
                .thenReturn(deliveryDto);

            // When & Then
            mockMvc.perform(get("/api/v1/deliveries/order/ORDER-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.deliveryId").value("DEL-12345678"))
                .andExpect(jsonPath("$.data.orderId").value("ORDER-001"))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
        }

        @Test
        @DisplayName("應該成功根據追蹤號碼查詢配送狀態")
        void shouldGetDeliveryStatusByTrackingNumberSuccessfully() throws Exception {
            // Given
            deliveryDto.setTrackingNumber("TRK-123456789");
            when(deliveryManagementUseCase.getDeliveryByTrackingNumber("TRK-123456789"))
                .thenReturn(deliveryDto);

            // When & Then
            mockMvc.perform(get("/api/v1/deliveries/tracking/TRK-123456789"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.deliveryId").value("DEL-12345678"))
                .andExpect(jsonPath("$.data.trackingNumber").value("TRK-123456789"))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
        }

        @Test
        @DisplayName("應該成功根據客戶ID查詢配送列表")
        void shouldGetDeliveriesByCustomerIdSuccessfully() throws Exception {
            // Given
            List<DeliveryDto> deliveries = Arrays.asList(deliveryDto);
            when(deliveryManagementUseCase.getDeliveriesByCustomerId("CUSTOMER-001"))
                .thenReturn(deliveries);

            // When & Then
            mockMvc.perform(get("/api/v1/deliveries/customer/CUSTOMER-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].deliveryId").value("DEL-12345678"))
                .andExpect(jsonPath("$.data[0].customerId").value("CUSTOMER-001"));
        }

        @Test
        @DisplayName("應該成功根據狀態查詢配送列表")
        void shouldGetDeliveriesByStatusSuccessfully() throws Exception {
            // Given
            List<DeliveryDto> deliveries = Arrays.asList(deliveryDto);
            when(deliveryManagementUseCase.getDeliveriesByStatus(DeliveryStatus.PENDING))
                .thenReturn(deliveries);

            // When & Then
            mockMvc.perform(get("/api/v1/deliveries/status/PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].deliveryId").value("DEL-12345678"))
                .andExpect(jsonPath("$.data[0].status").value("PENDING"));
        }
    }

    @Nested
    @DisplayName("配送地址更新API測試")
    class DeliveryAddressUpdateApiTest {

        @Test
        @DisplayName("應該成功更新配送地址")
        void shouldUpdateDeliveryAddressSuccessfully() throws Exception {
            // Given
            AddressDto newAddressDto = new AddressDto();
            newAddressDto.setCity("台北市");
            newAddressDto.setDistrict("信義區");
            newAddressDto.setStreet("市府路1號");
            newAddressDto.setPostalCode("110");
            newAddressDto.setRecipientName("李小華");
            newAddressDto.setRecipientPhone("0987654321");

            UpdateDeliveryAddressRequest updateRequest = new UpdateDeliveryAddressRequest();
            updateRequest.setDeliveryId("DEL-12345678");
            updateRequest.setNewAddress(newAddressDto);

            DeliveryDto updatedDeliveryDto = new DeliveryDto();
            updatedDeliveryDto.setDeliveryId("DEL-12345678");
            updatedDeliveryDto.setDeliveryAddress(newAddressDto);

            when(deliveryManagementUseCase.updateDeliveryAddress(any(UpdateDeliveryAddressRequest.class)))
                .thenReturn(updatedDeliveryDto);

            // When & Then
            mockMvc.perform(put("/api/v1/deliveries/DEL-12345678/address")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.deliveryId").value("DEL-12345678"))
                .andExpect(jsonPath("$.data.deliveryAddress.city").value("台北市"))
                .andExpect(jsonPath("$.data.deliveryAddress.district").value("信義區"))
                .andExpect(jsonPath("$.data.deliveryAddress.recipientName").value("李小華"));
        }
    }

    @Nested
    @DisplayName("配送操作API測試")
    class DeliveryOperationApiTest {

        @Test
        @DisplayName("應該成功開始配送")
        void shouldStartDeliverySuccessfully() throws Exception {
            // Given
            DeliveryDto startedDeliveryDto = new DeliveryDto();
            startedDeliveryDto.setDeliveryId("DEL-12345678");
            startedDeliveryDto.setStatus(DeliveryStatus.IN_TRANSIT);
            startedDeliveryDto.setTrackingNumber("TRK-123456789");

            when(deliveryManagementUseCase.startDelivery("DEL-12345678", "TRK-123456789"))
                .thenReturn(startedDeliveryDto);

            // When & Then
            mockMvc.perform(post("/api/v1/deliveries/DEL-12345678/start")
                    .param("trackingNumber", "TRK-123456789"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.deliveryId").value("DEL-12345678"))
                .andExpect(jsonPath("$.data.status").value("IN_TRANSIT"))
                .andExpect(jsonPath("$.data.trackingNumber").value("TRK-123456789"));
        }

        @Test
        @DisplayName("應該成功完成配送")
        void shouldCompleteDeliverySuccessfully() throws Exception {
            // Given
            DeliveryDto completedDeliveryDto = new DeliveryDto();
            completedDeliveryDto.setDeliveryId("DEL-12345678");
            completedDeliveryDto.setStatus(DeliveryStatus.DELIVERED);

            when(deliveryManagementUseCase.completeDelivery("DEL-12345678"))
                .thenReturn(completedDeliveryDto);

            // When & Then
            mockMvc.perform(post("/api/v1/deliveries/DEL-12345678/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.deliveryId").value("DEL-12345678"))
                .andExpect(jsonPath("$.data.status").value("DELIVERED"));
        }

        @Test
        @DisplayName("應該成功取消配送")
        void shouldCancelDeliverySuccessfully() throws Exception {
            // Given
            DeliveryDto cancelledDeliveryDto = new DeliveryDto();
            cancelledDeliveryDto.setDeliveryId("DEL-12345678");
            cancelledDeliveryDto.setStatus(DeliveryStatus.CANCELLED);

            when(deliveryManagementUseCase.cancelDelivery("DEL-12345678"))
                .thenReturn(cancelledDeliveryDto);

            // When & Then
            mockMvc.perform(post("/api/v1/deliveries/DEL-12345678/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.deliveryId").value("DEL-12345678"))
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
        }

        @Test
        @DisplayName("應該成功標記配送失敗")
        void shouldMarkDeliveryAsFailedSuccessfully() throws Exception {
            // Given
            DeliveryDto failedDeliveryDto = new DeliveryDto();
            failedDeliveryDto.setDeliveryId("DEL-12345678");
            failedDeliveryDto.setStatus(DeliveryStatus.FAILED);

            when(deliveryManagementUseCase.markDeliveryAsFailed("DEL-12345678", "地址不存在"))
                .thenReturn(failedDeliveryDto);

            // When & Then
            mockMvc.perform(post("/api/v1/deliveries/DEL-12345678/fail")
                    .param("failureReason", "地址不存在"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.deliveryId").value("DEL-12345678"))
                .andExpect(jsonPath("$.data.status").value("FAILED"));
        }
    }
}