package com.ecommerce.logistics.application.usecase;

import com.ecommerce.logistics.application.dto.AddressDto;
import com.ecommerce.logistics.application.dto.AddressValidationResult;
import com.ecommerce.logistics.application.mapper.DeliveryMapper;
import com.ecommerce.logistics.application.port.out.AddressValidationPort;
import com.ecommerce.logistics.domain.exception.InvalidAddressException;
import com.ecommerce.logistics.domain.model.Address;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 地址服務測試
 * 測試無效地址錯誤處理和台北地址驗證
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("地址服務測試")
class AddressServiceTest {

    @Mock
    private AddressValidationPort addressValidationPort;

    @Mock
    private DeliveryMapper deliveryMapper;

    @InjectMocks
    private AddressService addressService;

    private AddressDto validTaipeiAddressDto;
    private AddressDto invalidAddressDto;
    private Address validTaipeiAddress;

    @BeforeEach
    void setUp() {
        // 設定有效的台北地址DTO
        validTaipeiAddressDto = new AddressDto();
        validTaipeiAddressDto.setCity("台北市");
        validTaipeiAddressDto.setDistrict("大安區");
        validTaipeiAddressDto.setStreet("復興南路一段390號");
        validTaipeiAddressDto.setPostalCode("106");
        validTaipeiAddressDto.setRecipientName("王小明");
        validTaipeiAddressDto.setRecipientPhone("0912345678");

        // 設定無效地址DTO
        invalidAddressDto = new AddressDto();
        invalidAddressDto.setCity("");
        invalidAddressDto.setDistrict("大安區");
        invalidAddressDto.setStreet("復興南路一段390號");
        invalidAddressDto.setPostalCode("106");
        invalidAddressDto.setRecipientName("王小明");
        invalidAddressDto.setRecipientPhone("0912345678");

        // 設定有效的台北地址領域物件
        validTaipeiAddress = new Address(
            "台北市", "大安區", "復興南路一段390號", "106", 
            "王小明", "0912345678"
        );
    }

    @Nested
    @DisplayName("地址驗證測試")
    class AddressValidationTest {

        @Test
        @DisplayName("應該成功驗證有效的台北地址")
        void shouldValidateValidTaipeiAddressSuccessfully() {
            // Given
            when(deliveryMapper.toAddressDomain(validTaipeiAddressDto)).thenReturn(validTaipeiAddress);
            when(addressValidationPort.validateAddressExists(validTaipeiAddress)).thenReturn(true);
            when(addressValidationPort.isInDeliveryRange(validTaipeiAddress)).thenReturn(true);
            when(addressValidationPort.normalizeAddress(validTaipeiAddress)).thenReturn(validTaipeiAddress);
            when(addressValidationPort.getAddressCoordinates(validTaipeiAddress)).thenReturn("25.0330,121.5654");
            when(deliveryMapper.toAddressDto(validTaipeiAddress)).thenReturn(validTaipeiAddressDto);

            // When
            AddressValidationResult result = addressService.validateAddress(validTaipeiAddressDto);

            // Then
            assertTrue(result.isValid());
            assertTrue(result.isInDeliveryRange());
            assertNotNull(result.getNormalizedAddress());
            assertEquals("25.0330,121.5654", result.getCoordinates());
            assertTrue(result.getErrors().isEmpty());

            verify(addressValidationPort).validateAddressExists(validTaipeiAddress);
            verify(addressValidationPort).isInDeliveryRange(validTaipeiAddress);
            verify(addressValidationPort).normalizeAddress(validTaipeiAddress);
            verify(addressValidationPort).getAddressCoordinates(validTaipeiAddress);
        }

        @Test
        @DisplayName("應該正確處理無效地址格式錯誤")
        void shouldHandleInvalidAddressFormatError() {
            // Given
            when(deliveryMapper.toAddressDomain(invalidAddressDto))
                .thenThrow(new InvalidAddressException("城市不能為空"));

            // When
            AddressValidationResult result = addressService.validateAddress(invalidAddressDto);

            // Then
            assertFalse(result.isValid());
            assertTrue(result.getErrors().contains("城市不能為空"));
            verify(addressValidationPort, never()).validateAddressExists(any());
        }

        @Test
        @DisplayName("應該正確處理地址不存在的情況")
        void shouldHandleAddressNotExistsError() {
            // Given
            when(deliveryMapper.toAddressDomain(validTaipeiAddressDto)).thenReturn(validTaipeiAddress);
            when(addressValidationPort.validateAddressExists(validTaipeiAddress)).thenReturn(false);
            when(addressValidationPort.isInDeliveryRange(validTaipeiAddress)).thenReturn(true);
            when(addressValidationPort.normalizeAddress(validTaipeiAddress)).thenReturn(validTaipeiAddress);
            when(addressValidationPort.getAddressCoordinates(validTaipeiAddress)).thenReturn("25.0330,121.5654");
            when(deliveryMapper.toAddressDto(validTaipeiAddress)).thenReturn(validTaipeiAddressDto);

            // When
            AddressValidationResult result = addressService.validateAddress(validTaipeiAddressDto);

            // Then
            assertFalse(result.isValid());
            assertTrue(result.getErrors().contains("地址不存在或無法驗證"));
            verify(addressValidationPort).validateAddressExists(validTaipeiAddress);
        }

        @Test
        @DisplayName("應該正確處理地址不在配送範圍的情況")
        void shouldHandleAddressOutOfDeliveryRange() {
            // Given
            when(deliveryMapper.toAddressDomain(validTaipeiAddressDto)).thenReturn(validTaipeiAddress);
            when(addressValidationPort.validateAddressExists(validTaipeiAddress)).thenReturn(true);
            when(addressValidationPort.isInDeliveryRange(validTaipeiAddress)).thenReturn(false);
            when(addressValidationPort.normalizeAddress(validTaipeiAddress)).thenReturn(validTaipeiAddress);
            when(addressValidationPort.getAddressCoordinates(validTaipeiAddress)).thenReturn("25.0330,121.5654");
            when(deliveryMapper.toAddressDto(validTaipeiAddress)).thenReturn(validTaipeiAddressDto);

            // When
            AddressValidationResult result = addressService.validateAddress(validTaipeiAddressDto);

            // Then
            assertFalse(result.isValid());
            assertFalse(result.isInDeliveryRange());
            assertTrue(result.getErrors().contains("地址不在配送範圍內"));
            verify(addressValidationPort).isInDeliveryRange(validTaipeiAddress);
        }

        @Test
        @DisplayName("應該正確處理地址驗證服務異常")
        void shouldHandleAddressValidationServiceException() {
            // Given
            when(deliveryMapper.toAddressDomain(validTaipeiAddressDto)).thenReturn(validTaipeiAddress);
            when(addressValidationPort.validateAddressExists(validTaipeiAddress))
                .thenThrow(new RuntimeException("外部服務異常"));

            // When
            AddressValidationResult result = addressService.validateAddress(validTaipeiAddressDto);

            // Then
            assertFalse(result.isValid());
            assertTrue(result.getErrors().contains("地址驗證服務暫時無法使用"));
            verify(addressValidationPort).validateAddressExists(validTaipeiAddress);
        }
    }

    @Nested
    @DisplayName("台北地址特殊驗證測試")
    class TaipeiAddressValidationTest {

        @Test
        @DisplayName("應該成功驗證台北地址")
        void shouldValidateTaipeiAddressSuccessfully() {
            // Given
            validTaipeiAddressDto.setCity("台北市"); // 確保是台北地址
            when(deliveryMapper.toAddressDomain(validTaipeiAddressDto)).thenReturn(validTaipeiAddress);
            when(addressValidationPort.validateTaipeiAddressFormat(validTaipeiAddress)).thenReturn(true);
            when(addressValidationPort.normalizeAddress(validTaipeiAddress)).thenReturn(validTaipeiAddress);
            when(deliveryMapper.toAddressDto(validTaipeiAddress)).thenReturn(validTaipeiAddressDto);

            // When
            AddressValidationResult result = addressService.validateTaipeiAddress(validTaipeiAddressDto);

            // Then
            assertTrue(result.isValid());
            assertTrue(result.isInDeliveryRange()); // 台北市通常都在配送範圍內
            assertNotNull(result.getNormalizedAddress());
            assertTrue(result.getErrors().isEmpty());

            verify(addressValidationPort).validateTaipeiAddressFormat(validTaipeiAddress);
            verify(addressValidationPort).normalizeAddress(validTaipeiAddress);
        }

        @Test
        @DisplayName("應該拒絕非台北地址")
        void shouldRejectNonTaipeiAddress() {
            // Given
            AddressDto nonTaipeiAddress = new AddressDto();
            nonTaipeiAddress.setCity("高雄市");
            nonTaipeiAddress.setDistrict("前金區");
            nonTaipeiAddress.setStreet("中正四路211號");
            nonTaipeiAddress.setPostalCode("801");
            nonTaipeiAddress.setRecipientName("李小華");
            nonTaipeiAddress.setRecipientPhone("0987654321");

            // When
            AddressValidationResult result = addressService.validateTaipeiAddress(nonTaipeiAddress);

            // Then
            assertFalse(result.isValid());
            assertTrue(result.getErrors().contains("不是台北市地址"));
            verify(addressValidationPort, never()).validateTaipeiAddressFormat(any());
        }

        @Test
        @DisplayName("應該正確處理台北地址格式錯誤")
        void shouldHandleTaipeiAddressFormatError() {
            // Given
            when(deliveryMapper.toAddressDomain(validTaipeiAddressDto)).thenReturn(validTaipeiAddress);
            when(addressValidationPort.validateTaipeiAddressFormat(validTaipeiAddress)).thenReturn(false);

            // When
            AddressValidationResult result = addressService.validateTaipeiAddress(validTaipeiAddressDto);

            // Then
            assertFalse(result.isValid());
            assertTrue(result.getErrors().contains("台北市地址格式不正確"));
            verify(addressValidationPort).validateTaipeiAddressFormat(validTaipeiAddress);
        }

        @Test
        @DisplayName("應該正確處理台北地址驗證異常")
        void shouldHandleTaipeiAddressValidationException() {
            // Given
            when(deliveryMapper.toAddressDomain(validTaipeiAddressDto)).thenReturn(validTaipeiAddress);
            when(addressValidationPort.validateTaipeiAddressFormat(validTaipeiAddress))
                .thenThrow(new RuntimeException("台北地址驗證服務異常"));

            // When
            AddressValidationResult result = addressService.validateTaipeiAddress(validTaipeiAddressDto);

            // Then
            assertFalse(result.isValid());
            assertTrue(result.getErrors().contains("台北地址驗證服務暫時無法使用"));
            verify(addressValidationPort).validateTaipeiAddressFormat(validTaipeiAddress);
        }
    }

    @Nested
    @DisplayName("地址標準化測試")
    class AddressNormalizationTest {

        @Test
        @DisplayName("應該成功標準化地址")
        void shouldNormalizeAddressSuccessfully() {
            // Given
            Address normalizedAddress = new Address(
                "台北市", "大安區", "復興南路一段390號", "106", 
                "王小明", "0912345678"
            );
            AddressDto normalizedAddressDto = new AddressDto();
            normalizedAddressDto.setCity("台北市");
            normalizedAddressDto.setDistrict("大安區");
            normalizedAddressDto.setStreet("復興南路一段390號");

            when(deliveryMapper.toAddressDomain(validTaipeiAddressDto)).thenReturn(validTaipeiAddress);
            when(addressValidationPort.normalizeAddress(validTaipeiAddress)).thenReturn(normalizedAddress);
            when(deliveryMapper.toAddressDto(normalizedAddress)).thenReturn(normalizedAddressDto);

            // When
            AddressDto result = addressService.normalizeAddress(validTaipeiAddressDto);

            // Then
            assertNotNull(result);
            assertEquals("台北市", result.getCity());
            assertEquals("大安區", result.getDistrict());
            verify(addressValidationPort).normalizeAddress(validTaipeiAddress);
        }

        @Test
        @DisplayName("應該在標準化失敗時返回原地址")
        void shouldReturnOriginalAddressWhenNormalizationFails() {
            // Given
            when(deliveryMapper.toAddressDomain(validTaipeiAddressDto)).thenReturn(validTaipeiAddress);
            when(addressValidationPort.normalizeAddress(validTaipeiAddress))
                .thenThrow(new RuntimeException("標準化服務異常"));

            // When
            AddressDto result = addressService.normalizeAddress(validTaipeiAddressDto);

            // Then
            assertNotNull(result);
            assertEquals(validTaipeiAddressDto, result);
            verify(addressValidationPort).normalizeAddress(validTaipeiAddress);
        }
    }

    @Nested
    @DisplayName("配送範圍檢查測試")
    class DeliveryRangeCheckTest {

        @Test
        @DisplayName("應該正確檢查地址在配送範圍內")
        void shouldCheckAddressInDeliveryRange() {
            // Given
            when(deliveryMapper.toAddressDomain(validTaipeiAddressDto)).thenReturn(validTaipeiAddress);
            when(addressValidationPort.isInDeliveryRange(validTaipeiAddress)).thenReturn(true);

            // When
            boolean result = addressService.isAddressInDeliveryRange(validTaipeiAddressDto);

            // Then
            assertTrue(result);
            verify(addressValidationPort).isInDeliveryRange(validTaipeiAddress);
        }

        @Test
        @DisplayName("應該正確檢查地址不在配送範圍內")
        void shouldCheckAddressOutOfDeliveryRange() {
            // Given
            when(deliveryMapper.toAddressDomain(validTaipeiAddressDto)).thenReturn(validTaipeiAddress);
            when(addressValidationPort.isInDeliveryRange(validTaipeiAddress)).thenReturn(false);

            // When
            boolean result = addressService.isAddressInDeliveryRange(validTaipeiAddressDto);

            // Then
            assertFalse(result);
            verify(addressValidationPort).isInDeliveryRange(validTaipeiAddress);
        }

        @Test
        @DisplayName("應該在檢查失敗時返回false")
        void shouldReturnFalseWhenRangeCheckFails() {
            // Given
            when(deliveryMapper.toAddressDomain(validTaipeiAddressDto)).thenReturn(validTaipeiAddress);
            when(addressValidationPort.isInDeliveryRange(validTaipeiAddress))
                .thenThrow(new RuntimeException("配送範圍檢查服務異常"));

            // When
            boolean result = addressService.isAddressInDeliveryRange(validTaipeiAddressDto);

            // Then
            assertFalse(result);
            verify(addressValidationPort).isInDeliveryRange(validTaipeiAddress);
        }
    }
}