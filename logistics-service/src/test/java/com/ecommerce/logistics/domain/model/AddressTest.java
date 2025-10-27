package com.ecommerce.logistics.domain.model;

import com.ecommerce.logistics.domain.exception.InvalidAddressException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 地址領域模型測試
 * 測試台北地址驗證和錯誤處理
 */
@DisplayName("地址領域模型測試")
class AddressTest {

    @Nested
    @DisplayName("台北地址驗證測試")
    class TaipeiAddressValidationTest {

        @Test
        @DisplayName("應該成功建立有效的台北地址")
        void shouldCreateValidTaipeiAddress() {
            // Given
            String city = "台北市";
            String district = "大安區";
            String street = "復興南路一段390號";
            String postalCode = "106";
            String recipientName = "王小明";
            String recipientPhone = "0912345678";

            // When
            Address address = new Address(city, district, street, postalCode, recipientName, recipientPhone);

            // Then
            assertNotNull(address);
            assertEquals(city, address.getCity());
            assertEquals(district, address.getDistrict());
            assertEquals(street, address.getStreet());
            assertEquals(postalCode, address.getPostalCode());
            assertEquals(recipientName, address.getRecipientName());
            assertEquals(recipientPhone, address.getRecipientPhone());
            assertTrue(address.isTaipeiAddress());
        }

        @Test
        @DisplayName("應該成功建立繁體台北地址")
        void shouldCreateValidTraditionalTaipeiAddress() {
            // Given
            String city = "臺北市";
            String district = "信義區";
            String street = "市府路1號";
            String postalCode = "110";
            String recipientName = "李小華";
            String recipientPhone = "0987654321";

            // When
            Address address = new Address(city, district, street, postalCode, recipientName, recipientPhone);

            // Then
            assertNotNull(address);
            assertTrue(address.isTaipeiAddress());
            assertEquals("臺北市信義區市府路1號 (110)", address.getFullAddress());
        }

        @Test
        @DisplayName("應該拒絕無效的台北郵遞區號")
        void shouldRejectInvalidTaipeiPostalCode() {
            // Given
            String city = "台北市";
            String district = "大安區";
            String street = "復興南路一段390號";
            String invalidPostalCode = "200"; // 不是台北市郵遞區號範圍
            String recipientName = "王小明";
            String recipientPhone = "0912345678";

            // When & Then
            InvalidAddressException exception = assertThrows(InvalidAddressException.class, () -> {
                new Address(city, district, street, invalidPostalCode, recipientName, recipientPhone);
            });

            assertTrue(exception.getMessage().contains("台北市郵遞區號格式不正確"));
        }

        @Test
        @DisplayName("應該拒絕無效的台北行政區")
        void shouldRejectInvalidTaipeiDistrict() {
            // Given
            String city = "台北市";
            String invalidDistrict = "無效區";
            String street = "復興南路一段390號";
            String postalCode = "106";
            String recipientName = "王小明";
            String recipientPhone = "0912345678";

            // When & Then
            InvalidAddressException exception = assertThrows(InvalidAddressException.class, () -> {
                new Address(city, invalidDistrict, street, postalCode, recipientName, recipientPhone);
            });

            assertTrue(exception.getMessage().contains("無效的台北市行政區"));
        }
    }

    @Nested
    @DisplayName("地址驗證錯誤處理測試")
    class AddressValidationErrorTest {

        @Test
        @DisplayName("應該拒絕空的城市")
        void shouldRejectEmptyCity() {
            // When & Then
            InvalidAddressException exception = assertThrows(InvalidAddressException.class, () -> {
                new Address("", "大安區", "復興南路一段390號", "106", "王小明", "0912345678");
            });

            assertEquals("城市不能為空", exception.getMessage());
        }

        @Test
        @DisplayName("應該拒絕空的區域")
        void shouldRejectEmptyDistrict() {
            // When & Then
            InvalidAddressException exception = assertThrows(InvalidAddressException.class, () -> {
                new Address("台北市", "", "復興南路一段390號", "106", "王小明", "0912345678");
            });

            assertEquals("區域不能為空", exception.getMessage());
        }

        @Test
        @DisplayName("應該拒絕空的街道地址")
        void shouldRejectEmptyStreet() {
            // When & Then
            InvalidAddressException exception = assertThrows(InvalidAddressException.class, () -> {
                new Address("台北市", "大安區", "", "106", "王小明", "0912345678");
            });

            assertEquals("街道地址不能為空", exception.getMessage());
        }

        @Test
        @DisplayName("應該拒絕無效的手機號碼格式")
        void shouldRejectInvalidPhoneFormat() {
            // When & Then
            InvalidAddressException exception = assertThrows(InvalidAddressException.class, () -> {
                new Address("台北市", "大安區", "復興南路一段390號", "106", "王小明", "12345678");
            });

            assertTrue(exception.getMessage().contains("手機號碼格式不正確"));
        }

        @Test
        @DisplayName("應該拒絕空的收件人姓名")
        void shouldRejectEmptyRecipientName() {
            // When & Then
            InvalidAddressException exception = assertThrows(InvalidAddressException.class, () -> {
                new Address("台北市", "大安區", "復興南路一段390號", "106", "", "0912345678");
            });

            assertEquals("收件人姓名不能為空", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("地址功能測試")
    class AddressFunctionalityTest {

        @Test
        @DisplayName("應該正確生成完整地址字串")
        void shouldGenerateCorrectFullAddress() {
            // Given
            Address address = new Address("台北市", "大安區", "復興南路一段390號", "106", "王小明", "0912345678");

            // When
            String fullAddress = address.getFullAddress();

            // Then
            assertEquals("台北市大安區復興南路一段390號 (106)", fullAddress);
        }

        @Test
        @DisplayName("應該正確識別台北地址")
        void shouldCorrectlyIdentifyTaipeiAddress() {
            // Given
            Address taipeiAddress = new Address("台北市", "大安區", "復興南路一段390號", "106", "王小明", "0912345678");
            Address nonTaipeiAddress = new Address("高雄市", "前金區", "中正四路211號", "801", "李小華", "0987654321");

            // When & Then
            assertTrue(taipeiAddress.isTaipeiAddress());
            assertFalse(nonTaipeiAddress.isTaipeiAddress());
        }
    }
}