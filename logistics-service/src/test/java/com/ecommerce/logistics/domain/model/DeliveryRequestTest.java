package com.ecommerce.logistics.domain.model;

import com.ecommerce.logistics.domain.exception.InvalidDeliveryStateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 配送請求領域模型測試
 * 測試配送狀態追蹤和業務邏輯
 */
@DisplayName("配送請求領域模型測試")
class DeliveryRequestTest {

    private Address validTaipeiAddress;
    private DeliveryRequest deliveryRequest;

    @BeforeEach
    void setUp() {
        validTaipeiAddress = new Address(
            "台北市", "大安區", "復興南路一段390號", "106", 
            "王小明", "0912345678"
        );
        
        deliveryRequest = new DeliveryRequest(
            "DEL-12345678", "ORDER-001", "CUSTOMER-001", 
            validTaipeiAddress, DeliveryType.STANDARD
        );
    }

    @Nested
    @DisplayName("配送請求建立測試")
    class DeliveryRequestCreationTest {

        @Test
        @DisplayName("應該成功建立台北地址的配送請求")
        void shouldCreateDeliveryRequestWithTaipeiAddress() {
            // When
            DeliveryRequest request = new DeliveryRequest(
                "DEL-87654321", "ORDER-002", "CUSTOMER-002", 
                validTaipeiAddress, DeliveryType.EXPRESS
            );

            // Then
            assertNotNull(request);
            assertEquals("DEL-87654321", request.getDeliveryId());
            assertEquals("ORDER-002", request.getOrderId());
            assertEquals("CUSTOMER-002", request.getCustomerId());
            assertEquals(validTaipeiAddress, request.getDeliveryAddress());
            assertEquals(DeliveryType.EXPRESS, request.getDeliveryType());
            assertEquals(DeliveryStatus.PENDING, request.getStatus());
            assertEquals(DeliveryType.EXPRESS.getFee(), request.getDeliveryFee());
            assertNotNull(request.getEstimatedDeliveryDate());
        }

        @Test
        @DisplayName("應該正確計算預計配送日期")
        void shouldCalculateCorrectEstimatedDeliveryDate() {
            // Given
            LocalDateTime beforeCreation = LocalDateTime.now();
            
            // When
            DeliveryRequest request = new DeliveryRequest(
                "DEL-12345678", "ORDER-001", "CUSTOMER-001", 
                validTaipeiAddress, DeliveryType.SAME_DAY
            );
            
            // Then
            LocalDateTime estimatedDate = request.getEstimatedDeliveryDate();
            assertNotNull(estimatedDate);
            assertTrue(estimatedDate.isAfter(beforeCreation));
            // 當日配送應該是0天後
            assertTrue(estimatedDate.isBefore(beforeCreation.plusDays(1)));
        }
    }

    @Nested
    @DisplayName("配送狀態追蹤測試")
    class DeliveryStatusTrackingTest {

        @Test
        @DisplayName("應該正確更新配送狀態從待配送到配送中")
        void shouldUpdateStatusFromPendingToInTransit() {
            // Given
            assertEquals(DeliveryStatus.PENDING, deliveryRequest.getStatus());

            // When
            deliveryRequest.updateStatus(DeliveryStatus.IN_TRANSIT);

            // Then
            assertEquals(DeliveryStatus.IN_TRANSIT, deliveryRequest.getStatus());
        }

        @Test
        @DisplayName("應該正確追蹤完整的配送狀態流程")
        void shouldTrackCompleteDeliveryStatusFlow() {
            // Given
            assertEquals(DeliveryStatus.PENDING, deliveryRequest.getStatus());

            // When & Then - 完整的狀態轉換流程
            deliveryRequest.updateStatus(DeliveryStatus.IN_TRANSIT);
            assertEquals(DeliveryStatus.IN_TRANSIT, deliveryRequest.getStatus());

            deliveryRequest.updateStatus(DeliveryStatus.OUT_FOR_DELIVERY);
            assertEquals(DeliveryStatus.OUT_FOR_DELIVERY, deliveryRequest.getStatus());

            deliveryRequest.updateStatus(DeliveryStatus.DELIVERED);
            assertEquals(DeliveryStatus.DELIVERED, deliveryRequest.getStatus());
            assertNotNull(deliveryRequest.getActualDeliveryDate());
        }

        @Test
        @DisplayName("應該拒絕無效的狀態轉換")
        void shouldRejectInvalidStatusTransition() {
            // Given
            assertEquals(DeliveryStatus.PENDING, deliveryRequest.getStatus());

            // When & Then
            InvalidDeliveryStateException exception = assertThrows(
                InvalidDeliveryStateException.class, 
                () -> deliveryRequest.updateStatus(DeliveryStatus.DELIVERED)
            );

            assertTrue(exception.getMessage().contains("無法從"));
            assertTrue(exception.getMessage().contains("狀態轉換到"));
        }

        @Test
        @DisplayName("應該正確設定追蹤號碼")
        void shouldSetTrackingNumberCorrectly() {
            // Given
            deliveryRequest.updateStatus(DeliveryStatus.IN_TRANSIT);

            // When
            deliveryRequest.setTrackingNumber("TRK-123456789");

            // Then
            assertEquals("TRK-123456789", deliveryRequest.getTrackingNumber());
        }

        @Test
        @DisplayName("應該拒絕在待配送狀態設定追蹤號碼")
        void shouldRejectSettingTrackingNumberInPendingStatus() {
            // Given
            assertEquals(DeliveryStatus.PENDING, deliveryRequest.getStatus());

            // When & Then
            InvalidDeliveryStateException exception = assertThrows(
                InvalidDeliveryStateException.class,
                () -> deliveryRequest.setTrackingNumber("TRK-123456789")
            );

            assertTrue(exception.getMessage().contains("尚未開始處理"));
        }
    }

    @Nested
    @DisplayName("配送失敗處理測試")
    class DeliveryFailureHandlingTest {

        @Test
        @DisplayName("應該正確標記配送失敗")
        void shouldMarkDeliveryAsFailed() {
            // Given
            String failureReason = "地址不存在";

            // When
            deliveryRequest.markAsFailed(failureReason);

            // Then
            assertEquals(DeliveryStatus.FAILED, deliveryRequest.getStatus());
            assertEquals(failureReason, deliveryRequest.getFailureReason());
        }

        @Test
        @DisplayName("應該允許從失敗狀態重新開始配送")
        void shouldAllowRestartFromFailedStatus() {
            // Given
            deliveryRequest.markAsFailed("地址錯誤");
            assertEquals(DeliveryStatus.FAILED, deliveryRequest.getStatus());

            // When
            deliveryRequest.updateStatus(DeliveryStatus.IN_TRANSIT);

            // Then
            assertEquals(DeliveryStatus.IN_TRANSIT, deliveryRequest.getStatus());
        }
    }

    @Nested
    @DisplayName("地址更新測試")
    class AddressUpdateTest {

        @Test
        @DisplayName("應該允許在待配送狀態更新地址")
        void shouldAllowAddressUpdateInPendingStatus() {
            // Given
            assertEquals(DeliveryStatus.PENDING, deliveryRequest.getStatus());
            Address newAddress = new Address(
                "台北市", "信義區", "市府路1號", "110", 
                "李小華", "0987654321"
            );

            // When
            deliveryRequest.updateDeliveryAddress(newAddress);

            // Then
            assertEquals(newAddress, deliveryRequest.getDeliveryAddress());
        }

        @Test
        @DisplayName("應該拒絕在配送中狀態更新地址")
        void shouldRejectAddressUpdateInTransitStatus() {
            // Given
            deliveryRequest.updateStatus(DeliveryStatus.IN_TRANSIT);
            Address newAddress = new Address(
                "台北市", "信義區", "市府路1號", "110", 
                "李小華", "0987654321"
            );

            // When & Then
            InvalidDeliveryStateException exception = assertThrows(
                InvalidDeliveryStateException.class,
                () -> deliveryRequest.updateDeliveryAddress(newAddress)
            );

            assertTrue(exception.getMessage().contains("只有待配送狀態才能更新配送地址"));
        }
    }

    @Nested
    @DisplayName("配送取消測試")
    class DeliveryCancellationTest {

        @Test
        @DisplayName("應該允許取消待配送的請求")
        void shouldAllowCancelPendingDelivery() {
            // Given
            assertEquals(DeliveryStatus.PENDING, deliveryRequest.getStatus());
            assertTrue(deliveryRequest.canBeCancelled());

            // When
            deliveryRequest.cancel();

            // Then
            assertEquals(DeliveryStatus.CANCELLED, deliveryRequest.getStatus());
        }

        @Test
        @DisplayName("應該拒絕取消已完成的配送")
        void shouldRejectCancelCompletedDelivery() {
            // Given
            deliveryRequest.updateStatus(DeliveryStatus.IN_TRANSIT);
            deliveryRequest.updateStatus(DeliveryStatus.OUT_FOR_DELIVERY);
            deliveryRequest.updateStatus(DeliveryStatus.DELIVERED);
            assertFalse(deliveryRequest.canBeCancelled());

            // When & Then
            InvalidDeliveryStateException exception = assertThrows(
                InvalidDeliveryStateException.class,
                () -> deliveryRequest.cancel()
            );

            assertTrue(exception.getMessage().contains("當前狀態無法取消配送"));
        }
    }

    @Nested
    @DisplayName("配送完成檢查測試")
    class DeliveryCompletionTest {

        @Test
        @DisplayName("應該正確識別已完成的配送")
        void shouldIdentifyCompletedDelivery() {
            // Given
            deliveryRequest.updateStatus(DeliveryStatus.IN_TRANSIT);
            deliveryRequest.updateStatus(DeliveryStatus.OUT_FOR_DELIVERY);
            deliveryRequest.updateStatus(DeliveryStatus.DELIVERED);

            // When & Then
            assertTrue(deliveryRequest.isCompleted());
        }

        @Test
        @DisplayName("應該正確識別未完成的配送")
        void shouldIdentifyIncompleteDelivery() {
            // Given
            deliveryRequest.updateStatus(DeliveryStatus.IN_TRANSIT);

            // When & Then
            assertFalse(deliveryRequest.isCompleted());
        }

        @Test
        @DisplayName("應該正確識別取消的配送為已完成")
        void shouldIdentifyCancelledDeliveryAsCompleted() {
            // Given
            deliveryRequest.cancel();

            // When & Then
            assertTrue(deliveryRequest.isCompleted());
        }
    }
}