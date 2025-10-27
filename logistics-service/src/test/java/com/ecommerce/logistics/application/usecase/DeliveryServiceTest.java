package com.ecommerce.logistics.application.usecase;

import com.ecommerce.logistics.application.dto.*;
import com.ecommerce.logistics.application.mapper.DeliveryMapper;
import com.ecommerce.logistics.application.port.out.DeliveryEventPort;
import com.ecommerce.logistics.application.port.out.DeliveryPersistencePort;
import com.ecommerce.logistics.domain.event.DeliveryCreatedEvent;
import com.ecommerce.logistics.domain.event.DeliveryStatusUpdatedEvent;
import com.ecommerce.logistics.domain.exception.DeliveryNotFoundException;
import com.ecommerce.logistics.domain.model.*;
import com.ecommerce.logistics.domain.service.DeliveryDomainService;
import com.ecommerce.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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
 * 配送服務測試
 * 測試台北地址配送建立、無效地址錯誤處理和配送狀態追蹤
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("配送服務測試")
class DeliveryServiceTest {

    @Mock
    private DeliveryDomainService deliveryDomainService;

    @Mock
    private DeliveryPersistencePort deliveryPersistencePort;

    @Mock
    private DeliveryEventPort deliveryEventPort;

    @Mock
    private DeliveryMapper deliveryMapper;

    @InjectMocks
    private DeliveryService deliveryService;

    private CreateDeliveryRequest createDeliveryRequest;
    private AddressDto taipeiAddressDto;
    private Address taipeiAddress;
    private DeliveryRequest deliveryRequest;
    private DeliveryDto deliveryDto;

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

        // 設定台北地址領域物件
        taipeiAddress = new Address(
            "台北市", "大安區", "復興南路一段390號", "106", 
            "王小明", "0912345678"
        );

        // 設定建立配送請求
        createDeliveryRequest = new CreateDeliveryRequest();
        createDeliveryRequest.setOrderId("ORDER-001");
        createDeliveryRequest.setCustomerId("CUSTOMER-001");
        createDeliveryRequest.setDeliveryAddress(taipeiAddressDto);
        createDeliveryRequest.setDeliveryType(DeliveryType.STANDARD);

        // 設定配送請求領域物件
        deliveryRequest = new DeliveryRequest(
            "DEL-12345678", "ORDER-001", "CUSTOMER-001", 
            taipeiAddress, DeliveryType.STANDARD
        );

        // 設定配送DTO
        deliveryDto = new DeliveryDto();
        deliveryDto.setDeliveryId("DEL-12345678");
        deliveryDto.setOrderId("ORDER-001");
        deliveryDto.setCustomerId("CUSTOMER-001");
        deliveryDto.setDeliveryAddress(taipeiAddressDto);
        deliveryDto.setDeliveryType(DeliveryType.STANDARD);
        deliveryDto.setStatus(DeliveryStatus.PENDING);
        deliveryDto.setDeliveryFee(BigDecimal.valueOf(60));
    }

    @Nested
    @DisplayName("台北地址配送建立測試")
    class TaipeiDeliveryCreationTest {

        @Test
        @DisplayName("應該成功建立台北地址的配送請求")
        void shouldCreateDeliveryRequestWithTaipeiAddress() {
            // Given
            when(deliveryPersistencePort.existsByOrderId("ORDER-001")).thenReturn(false);
            when(deliveryMapper.toAddressDomain(taipeiAddressDto)).thenReturn(taipeiAddress);
            when(deliveryDomainService.createDeliveryRequest(
                "ORDER-001", "CUSTOMER-001", taipeiAddress, DeliveryType.STANDARD
            )).thenReturn(deliveryRequest);
            when(deliveryPersistencePort.save(deliveryRequest)).thenReturn(deliveryRequest);
            when(deliveryMapper.toDto(deliveryRequest)).thenReturn(deliveryDto);

            // When
            DeliveryDto result = deliveryService.createDeliveryRequest(createDeliveryRequest);

            // Then
            assertNotNull(result);
            assertEquals("DEL-12345678", result.getDeliveryId());
            assertEquals("ORDER-001", result.getOrderId());
            assertEquals("CUSTOMER-001", result.getCustomerId());
            assertEquals(DeliveryStatus.PENDING, result.getStatus());
            assertEquals(BigDecimal.valueOf(60), result.getDeliveryFee());

            // 驗證方法調用
            verify(deliveryPersistencePort).existsByOrderId("ORDER-001");
            verify(deliveryMapper).toAddressDomain(taipeiAddressDto);
            verify(deliveryDomainService).createDeliveryRequest(
                "ORDER-001", "CUSTOMER-001", taipeiAddress, DeliveryType.STANDARD
            );
            verify(deliveryPersistencePort).save(deliveryRequest);
            verify(deliveryEventPort).publishDeliveryCreatedEvent(any(DeliveryCreatedEvent.class));
        }

        @Test
        @DisplayName("應該拒絕重複的訂單配送請求")
        void shouldRejectDuplicateOrderDeliveryRequest() {
            // Given
            when(deliveryPersistencePort.existsByOrderId("ORDER-001")).thenReturn(true);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                deliveryService.createDeliveryRequest(createDeliveryRequest);
            });

            assertTrue(exception.getMessage().contains("訂單已存在配送請求"));
            verify(deliveryPersistencePort).existsByOrderId("ORDER-001");
            verify(deliveryDomainService, never()).createDeliveryRequest(any(), any(), any(), any());
        }

        @Test
        @DisplayName("應該正確發布配送建立事件")
        void shouldPublishDeliveryCreatedEvent() {
            // Given
            when(deliveryPersistencePort.existsByOrderId("ORDER-001")).thenReturn(false);
            when(deliveryMapper.toAddressDomain(taipeiAddressDto)).thenReturn(taipeiAddress);
            when(deliveryDomainService.createDeliveryRequest(any(), any(), any(), any())).thenReturn(deliveryRequest);
            when(deliveryPersistencePort.save(deliveryRequest)).thenReturn(deliveryRequest);
            when(deliveryMapper.toDto(deliveryRequest)).thenReturn(deliveryDto);

            // When
            deliveryService.createDeliveryRequest(createDeliveryRequest);

            // Then
            verify(deliveryEventPort).publishDeliveryCreatedEvent(argThat(event -> 
                event.getDeliveryId().equals("DEL-12345678") &&
                event.getOrderId().equals("ORDER-001") &&
                event.getCustomerId().equals("CUSTOMER-001") &&
                event.getDeliveryType() == DeliveryType.STANDARD
            ));
        }
    }

    @Nested
    @DisplayName("配送狀態追蹤測試")
    class DeliveryStatusTrackingTest {

        @Test
        @DisplayName("應該成功更新配送狀態")
        void shouldUpdateDeliveryStatusSuccessfully() {
            // Given
            UpdateDeliveryStatusRequest request = new UpdateDeliveryStatusRequest();
            request.setDeliveryId("DEL-12345678");
            request.setStatus(DeliveryStatus.IN_TRANSIT);
            request.setTrackingNumber("TRK-123456789");
            request.setNotes("配送已開始");

            when(deliveryPersistencePort.findById("DEL-12345678")).thenReturn(Optional.of(deliveryRequest));
            when(deliveryPersistencePort.save(deliveryRequest)).thenReturn(deliveryRequest);
            when(deliveryMapper.toDto(deliveryRequest)).thenReturn(deliveryDto);

            // When
            DeliveryDto result = deliveryService.updateDeliveryStatus(request);

            // Then
            assertNotNull(result);
            verify(deliveryDomainService).updateDeliveryStatus(deliveryRequest, DeliveryStatus.IN_TRANSIT, "配送已開始");
            verify(deliveryRequest).setTrackingNumber("TRK-123456789");
            verify(deliveryPersistencePort).save(deliveryRequest);
            verify(deliveryEventPort).publishDeliveryStatusUpdatedEvent(any(DeliveryStatusUpdatedEvent.class));
        }

        @Test
        @DisplayName("應該拒絕更新不存在的配送狀態")
        void shouldRejectUpdateNonExistentDeliveryStatus() {
            // Given
            UpdateDeliveryStatusRequest request = new UpdateDeliveryStatusRequest();
            request.setDeliveryId("DEL-NONEXISTENT");
            request.setStatus(DeliveryStatus.IN_TRANSIT);

            when(deliveryPersistencePort.findById("DEL-NONEXISTENT")).thenReturn(Optional.empty());

            // When & Then
            DeliveryNotFoundException exception = assertThrows(DeliveryNotFoundException.class, () -> {
                deliveryService.updateDeliveryStatus(request);
            });

            assertNotNull(exception);
            verify(deliveryDomainService, never()).updateDeliveryStatus(any(), any(), any());
        }

        @Test
        @DisplayName("應該正確追蹤配送狀態變更歷程")
        void shouldTrackDeliveryStatusChangeHistory() {
            // Given
            UpdateDeliveryStatusRequest request1 = new UpdateDeliveryStatusRequest();
            request1.setDeliveryId("DEL-12345678");
            request1.setStatus(DeliveryStatus.IN_TRANSIT);

            UpdateDeliveryStatusRequest request2 = new UpdateDeliveryStatusRequest();
            request2.setDeliveryId("DEL-12345678");
            request2.setStatus(DeliveryStatus.OUT_FOR_DELIVERY);

            when(deliveryPersistencePort.findById("DEL-12345678")).thenReturn(Optional.of(deliveryRequest));
            when(deliveryPersistencePort.save(deliveryRequest)).thenReturn(deliveryRequest);
            when(deliveryMapper.toDto(deliveryRequest)).thenReturn(deliveryDto);

            // When
            deliveryService.updateDeliveryStatus(request1);
            deliveryService.updateDeliveryStatus(request2);

            // Then
            verify(deliveryDomainService).updateDeliveryStatus(deliveryRequest, DeliveryStatus.IN_TRANSIT, null);
            verify(deliveryDomainService).updateDeliveryStatus(deliveryRequest, DeliveryStatus.OUT_FOR_DELIVERY, null);
            verify(deliveryEventPort, times(2)).publishDeliveryStatusUpdatedEvent(any(DeliveryStatusUpdatedEvent.class));
        }
    }

    @Nested
    @DisplayName("配送查詢測試")
    class DeliveryQueryTest {

        @Test
        @DisplayName("應該成功根據配送ID查詢配送資訊")
        void shouldGetDeliveryByIdSuccessfully() {
            // Given
            when(deliveryPersistencePort.findById("DEL-12345678")).thenReturn(Optional.of(deliveryRequest));
            when(deliveryMapper.toDto(deliveryRequest)).thenReturn(deliveryDto);

            // When
            DeliveryDto result = deliveryService.getDeliveryById("DEL-12345678");

            // Then
            assertNotNull(result);
            assertEquals("DEL-12345678", result.getDeliveryId());
            verify(deliveryPersistencePort).findById("DEL-12345678");
        }

        @Test
        @DisplayName("應該成功根據訂單ID查詢配送資訊")
        void shouldGetDeliveryByOrderIdSuccessfully() {
            // Given
            when(deliveryPersistencePort.findByOrderId("ORDER-001")).thenReturn(Optional.of(deliveryRequest));
            when(deliveryMapper.toDto(deliveryRequest)).thenReturn(deliveryDto);

            // When
            DeliveryDto result = deliveryService.getDeliveryByOrderId("ORDER-001");

            // Then
            assertNotNull(result);
            assertEquals("ORDER-001", result.getOrderId());
            verify(deliveryPersistencePort).findByOrderId("ORDER-001");
        }

        @Test
        @DisplayName("應該成功根據客戶ID查詢配送列表")
        void shouldGetDeliveriesByCustomerIdSuccessfully() {
            // Given
            List<DeliveryRequest> deliveries = Arrays.asList(deliveryRequest);
            List<DeliveryDto> deliveryDtos = Arrays.asList(deliveryDto);
            
            when(deliveryPersistencePort.findByCustomerId("CUSTOMER-001")).thenReturn(deliveries);
            when(deliveryMapper.toDtoList(deliveries)).thenReturn(deliveryDtos);

            // When
            List<DeliveryDto> result = deliveryService.getDeliveriesByCustomerId("CUSTOMER-001");

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("CUSTOMER-001", result.get(0).getCustomerId());
            verify(deliveryPersistencePort).findByCustomerId("CUSTOMER-001");
        }

        @Test
        @DisplayName("應該拋出異常當配送不存在時")
        void shouldThrowExceptionWhenDeliveryNotFound() {
            // Given
            when(deliveryPersistencePort.findById("DEL-NONEXISTENT")).thenReturn(Optional.empty());

            // When & Then
            DeliveryNotFoundException exception = assertThrows(DeliveryNotFoundException.class, () -> {
                deliveryService.getDeliveryById("DEL-NONEXISTENT");
            });

            assertNotNull(exception);
            verify(deliveryPersistencePort).findById("DEL-NONEXISTENT");
        }
    }

    @Nested
    @DisplayName("配送地址更新測試")
    class DeliveryAddressUpdateTest {

        @Test
        @DisplayName("應該成功更新配送地址")
        void shouldUpdateDeliveryAddressSuccessfully() {
            // Given
            AddressDto newAddressDto = new AddressDto();
            newAddressDto.setCity("台北市");
            newAddressDto.setDistrict("信義區");
            newAddressDto.setStreet("市府路1號");
            newAddressDto.setPostalCode("110");
            newAddressDto.setRecipientName("李小華");
            newAddressDto.setRecipientPhone("0987654321");

            Address newAddress = new Address(
                "台北市", "信義區", "市府路1號", "110", 
                "李小華", "0987654321"
            );

            UpdateDeliveryAddressRequest request = new UpdateDeliveryAddressRequest();
            request.setDeliveryId("DEL-12345678");
            request.setNewAddress(newAddressDto);

            when(deliveryPersistencePort.findById("DEL-12345678")).thenReturn(Optional.of(deliveryRequest));
            when(deliveryMapper.toAddressDomain(newAddressDto)).thenReturn(newAddress);
            when(deliveryPersistencePort.save(deliveryRequest)).thenReturn(deliveryRequest);
            when(deliveryMapper.toDto(deliveryRequest)).thenReturn(deliveryDto);

            // When
            DeliveryDto result = deliveryService.updateDeliveryAddress(request);

            // Then
            assertNotNull(result);
            verify(deliveryDomainService).validateAddressUpdate(deliveryRequest, newAddress);
            verify(deliveryRequest).updateDeliveryAddress(newAddress);
            verify(deliveryPersistencePort).save(deliveryRequest);
        }

        @Test
        @DisplayName("應該拒絕更新不存在的配送地址")
        void shouldRejectUpdateNonExistentDeliveryAddress() {
            // Given
            UpdateDeliveryAddressRequest request = new UpdateDeliveryAddressRequest();
            request.setDeliveryId("DEL-NONEXISTENT");
            request.setNewAddress(taipeiAddressDto);

            when(deliveryPersistencePort.findById("DEL-NONEXISTENT")).thenReturn(Optional.empty());

            // When & Then
            DeliveryNotFoundException exception = assertThrows(DeliveryNotFoundException.class, () -> {
                deliveryService.updateDeliveryAddress(request);
            });

            assertNotNull(exception);
            verify(deliveryDomainService, never()).validateAddressUpdate(any(), any());
        }
    }

    @Nested
    @DisplayName("配送操作測試")
    class DeliveryOperationTest {

        @Test
        @DisplayName("應該成功開始配送")
        void shouldStartDeliverySuccessfully() {
            // Given
            String trackingNumber = "TRK-123456789";
            when(deliveryPersistencePort.findById("DEL-12345678")).thenReturn(Optional.of(deliveryRequest));
            when(deliveryPersistencePort.save(deliveryRequest)).thenReturn(deliveryRequest);
            when(deliveryMapper.toDto(deliveryRequest)).thenReturn(deliveryDto);

            // When
            DeliveryDto result = deliveryService.startDelivery("DEL-12345678", trackingNumber);

            // Then
            assertNotNull(result);
            verify(deliveryDomainService).startDelivery(deliveryRequest, trackingNumber);
            verify(deliveryPersistencePort).save(deliveryRequest);
            verify(deliveryEventPort).publishDeliveryStatusUpdatedEvent(any(DeliveryStatusUpdatedEvent.class));
        }

        @Test
        @DisplayName("應該成功完成配送")
        void shouldCompleteDeliverySuccessfully() {
            // Given
            when(deliveryPersistencePort.findById("DEL-12345678")).thenReturn(Optional.of(deliveryRequest));
            when(deliveryPersistencePort.save(deliveryRequest)).thenReturn(deliveryRequest);
            when(deliveryMapper.toDto(deliveryRequest)).thenReturn(deliveryDto);

            // When
            DeliveryDto result = deliveryService.completeDelivery("DEL-12345678");

            // Then
            assertNotNull(result);
            verify(deliveryDomainService).completeDelivery(deliveryRequest);
            verify(deliveryPersistencePort).save(deliveryRequest);
        }

        @Test
        @DisplayName("應該成功取消配送")
        void shouldCancelDeliverySuccessfully() {
            // Given
            when(deliveryPersistencePort.findById("DEL-12345678")).thenReturn(Optional.of(deliveryRequest));
            when(deliveryPersistencePort.save(deliveryRequest)).thenReturn(deliveryRequest);
            when(deliveryMapper.toDto(deliveryRequest)).thenReturn(deliveryDto);

            // When
            DeliveryDto result = deliveryService.cancelDelivery("DEL-12345678");

            // Then
            assertNotNull(result);
            verify(deliveryRequest).cancel();
            verify(deliveryPersistencePort).save(deliveryRequest);
            verify(deliveryEventPort).publishDeliveryStatusUpdatedEvent(any(DeliveryStatusUpdatedEvent.class));
        }

        @Test
        @DisplayName("應該成功標記配送失敗")
        void shouldMarkDeliveryAsFailedSuccessfully() {
            // Given
            String failureReason = "地址不存在";
            when(deliveryPersistencePort.findById("DEL-12345678")).thenReturn(Optional.of(deliveryRequest));
            when(deliveryPersistencePort.save(deliveryRequest)).thenReturn(deliveryRequest);
            when(deliveryMapper.toDto(deliveryRequest)).thenReturn(deliveryDto);

            // When
            DeliveryDto result = deliveryService.markDeliveryAsFailed("DEL-12345678", failureReason);

            // Then
            assertNotNull(result);
            verify(deliveryDomainService).handleDeliveryFailure(deliveryRequest, failureReason);
            verify(deliveryPersistencePort).save(deliveryRequest);
        }
    }
}