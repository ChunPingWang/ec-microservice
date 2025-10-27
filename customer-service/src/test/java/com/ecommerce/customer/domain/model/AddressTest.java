package com.ecommerce.customer.domain.model;

import com.ecommerce.common.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Address value object
 * Tests Taiwan/Taipei address validation logic
 */
@DisplayName("Address Value Object Tests")
class AddressTest {

    @Nested
    @DisplayName("Address Creation Tests")
    class AddressCreationTests {

        @Test
        @DisplayName("Should create valid Taipei address")
        void shouldCreateValidTaipeiAddress() {
            // Given
            String street = "忠孝東路一段1號";
            String district = "中正區";
            String postalCode = "100";
            AddressType type = AddressType.HOME;

            // When
            Address address = Address.createTaipeiAddress(street, district, postalCode, type);

            // Then
            assertNotNull(address);
            assertNotNull(address.getAddressId());
            assertEquals(street, address.getStreet());
            assertEquals("台北市", address.getCity());
            assertEquals(district, address.getDistrict());
            assertEquals(postalCode, address.getPostalCode());
            assertEquals("台灣", address.getCountry());
            assertEquals(type, address.getType());
            assertFalse(address.isPrimary());
            assertTrue(address.isTaiwanAddress());
            assertTrue(address.isTaipeiAddress());
        }

        @Test
        @DisplayName("Should create valid general address")
        void shouldCreateValidGeneralAddress() {
            // Given
            String street = "123 Main Street";
            String city = "New York";
            String district = "Manhattan";
            String postalCode = "10001";
            String country = "USA";
            AddressType type = AddressType.WORK;

            // When
            Address address = Address.create(street, city, district, postalCode, country, type);

            // Then
            assertNotNull(address);
            assertEquals(street, address.getStreet());
            assertEquals(city, address.getCity());
            assertEquals(district, address.getDistrict());
            assertEquals(postalCode, address.getPostalCode());
            assertEquals(country, address.getCountry());
            assertEquals(type, address.getType());
            assertFalse(address.isTaiwanAddress());
            assertFalse(address.isTaipeiAddress());
        }
    }

    @Nested
    @DisplayName("Taipei Address Validation Tests")
    class TaipeiAddressValidationTests {

        @Test
        @DisplayName("Should validate all Taipei districts correctly")
        void shouldValidateAllTaipeiDistrictsCorrectly() {
            String[] districts = {"中正區", "大同區", "中山區", "松山區", "大安區", "萬華區",
                                "信義區", "士林區", "北投區", "內湖區", "南港區", "文山區"};
            String[] postalCodes = {"100", "103", "104", "105", "106", "108",
                                  "110", "111", "112", "114", "115", "116"};

            for (int i = 0; i < districts.length; i++) {
                // When
                Address address = Address.createTaipeiAddress("測試路1號", districts[i], postalCodes[i], AddressType.HOME);
                
                // Then
                assertDoesNotThrow(address::validate);
                assertEquals(districts[i], address.getDistrict());
                assertEquals(postalCodes[i], address.getPostalCode());
            }
        }

        @Test
        @DisplayName("Should throw exception for invalid Taipei district")
        void shouldThrowExceptionForInvalidTaipeiDistrict() {
            // Given
            String invalidDistrict = "無效區";
            String postalCode = "100";

            // When
            Address address = Address.createTaipeiAddress("測試路1號", invalidDistrict, postalCode, AddressType.HOME);

            // Then
            ValidationException exception = assertThrows(ValidationException.class, address::validate);
            assertEquals("Invalid Taipei district: " + invalidDistrict, exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for mismatched district and postal code")
        void shouldThrowExceptionForMismatchedDistrictAndPostalCode() {
            // Given - 中正區 should be 100, not 103
            String district = "中正區";
            String wrongPostalCode = "103";

            // When
            Address address = Address.createTaipeiAddress("測試路1號", district, wrongPostalCode, AddressType.HOME);

            // Then
            ValidationException exception = assertThrows(ValidationException.class, address::validate);
            assertEquals("中正區 postal code should be 100", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for invalid Taiwan postal code format")
        void shouldThrowExceptionForInvalidTaiwanPostalCodeFormat() {
            // Given
            String invalidPostalCode = "12345678"; // Too long

            // When
            Address address = Address.createTaipeiAddress("測試路1號", "中正區", invalidPostalCode, AddressType.HOME);

            // Then
            ValidationException exception = assertThrows(ValidationException.class, address::validate);
            assertEquals("Invalid Taiwan postal code format. Must be 5 digits.", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for out of range Taipei postal code")
        void shouldThrowExceptionForOutOfRangeTaipeiPostalCode() {
            // Given
            String outOfRangePostalCode = "200"; // Outside 100-116 range

            // When
            Address address = Address.createTaipeiAddress("測試路1號", "中正區", outOfRangePostalCode, AddressType.HOME);

            // Then
            ValidationException exception = assertThrows(ValidationException.class, address::validate);
            assertEquals("Invalid Taipei postal code: " + outOfRangePostalCode, exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Address Validation Tests")
    class AddressValidationTests {

        @Test
        @DisplayName("Should throw exception when street is null")
        void shouldThrowExceptionWhenStreetIsNull() {
            // When & Then
            ValidationException exception = assertThrows(ValidationException.class, () ->
                Address.create(null, "台北市", "中正區", "100", "台灣", AddressType.HOME)
            );
            assertEquals("Street address is required", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when city is empty")
        void shouldThrowExceptionWhenCityIsEmpty() {
            // When & Then
            ValidationException exception = assertThrows(ValidationException.class, () ->
                Address.create("測試路1號", "   ", "中正區", "100", "台灣", AddressType.HOME)
            );
            assertEquals("City is required", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when district is null")
        void shouldThrowExceptionWhenDistrictIsNull() {
            // When & Then
            ValidationException exception = assertThrows(ValidationException.class, () ->
                Address.create("測試路1號", "台北市", null, "100", "台灣", AddressType.HOME)
            );
            assertEquals("District is required", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when postal code is empty")
        void shouldThrowExceptionWhenPostalCodeIsEmpty() {
            // When & Then
            ValidationException exception = assertThrows(ValidationException.class, () ->
                Address.create("測試路1號", "台北市", "中正區", "", "台灣", AddressType.HOME)
            );
            assertEquals("Postal code is required", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when country is null")
        void shouldThrowExceptionWhenCountryIsNull() {
            // When & Then
            ValidationException exception = assertThrows(ValidationException.class, () ->
                Address.create("測試路1號", "台北市", "中正區", "100", null, AddressType.HOME)
            );
            assertEquals("Country is required", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when street exceeds maximum length")
        void shouldThrowExceptionWhenStreetExceedsMaximumLength() {
            // Given
            String longStreet = "a".repeat(201); // Exceeds 200 character limit

            // When & Then
            ValidationException exception = assertThrows(ValidationException.class, () ->
                Address.create(longStreet, "台北市", "中正區", "100", "台灣", AddressType.HOME)
            );
            assertEquals("Street address cannot exceed 200 characters", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Address Business Logic Tests")
    class AddressBusinessLogicTests {

        @Test
        @DisplayName("Should update address details correctly")
        void shouldUpdateAddressDetailsCorrectly() {
            // Given
            Address address = Address.createTaipeiAddress("忠孝東路一段1號", "中正區", "100", AddressType.HOME);
            String newStreet = "信義路五段7號";
            String newCity = "台北市";
            String newDistrict = "信義區";
            String newPostalCode = "110";
            String newCountry = "台灣";

            // When
            address.updateDetails(newStreet, newCity, newDistrict, newPostalCode, newCountry);

            // Then
            assertEquals(newStreet, address.getStreet());
            assertEquals(newCity, address.getCity());
            assertEquals(newDistrict, address.getDistrict());
            assertEquals(newPostalCode, address.getPostalCode());
            assertEquals(newCountry, address.getCountry());
            assertNotNull(address.getUpdatedAt());
        }

        @Test
        @DisplayName("Should set primary flag correctly")
        void shouldSetPrimaryFlagCorrectly() {
            // Given
            Address address = Address.createTaipeiAddress("忠孝東路一段1號", "中正區", "100", AddressType.HOME);
            assertFalse(address.isPrimary());

            // When
            address.setPrimary(true);

            // Then
            assertTrue(address.isPrimary());
            assertNotNull(address.getUpdatedAt());
        }

        @Test
        @DisplayName("Should format Taiwan address correctly")
        void shouldFormatTaiwanAddressCorrectly() {
            // Given
            Address address = Address.createTaipeiAddress("忠孝東路一段1號", "中正區", "100", AddressType.HOME);

            // When
            String formattedAddress = address.getFormattedAddress();

            // Then
            assertEquals("台灣台北市中正區100 忠孝東路一段1號", formattedAddress);
        }

        @Test
        @DisplayName("Should format international address correctly")
        void shouldFormatInternationalAddressCorrectly() {
            // Given
            Address address = Address.create("123 Main Street", "New York", "Manhattan", "10001", "USA", AddressType.WORK);

            // When
            String formattedAddress = address.getFormattedAddress();

            // Then
            assertEquals("123 Main Street, Manhattan, New York 10001, USA", formattedAddress);
        }

        @Test
        @DisplayName("Should identify Taiwan address correctly")
        void shouldIdentifyTaiwanAddressCorrectly() {
            // Given
            Address taiwanAddress = Address.createTaipeiAddress("忠孝東路一段1號", "中正區", "100", AddressType.HOME);
            Address usAddress = Address.create("123 Main Street", "New York", "Manhattan", "10001", "USA", AddressType.WORK);

            // When & Then
            assertTrue(taiwanAddress.isTaiwanAddress());
            assertFalse(usAddress.isTaiwanAddress());
        }

        @Test
        @DisplayName("Should identify Taipei address correctly")
        void shouldIdentifyTaipeiAddressCorrectly() {
            // Given
            Address taipeiAddress = Address.createTaipeiAddress("忠孝東路一段1號", "中正區", "100", AddressType.HOME);
            Address kaohsiungAddress = Address.create("中山路1號", "高雄市", "前金區", "80041", "台灣", AddressType.HOME);

            // When & Then
            assertTrue(taipeiAddress.isTaipeiAddress());
            assertFalse(kaohsiungAddress.isTaipeiAddress());
        }
    }

    @Nested
    @DisplayName("Address Equality Tests")
    class AddressEqualityTests {

        @Test
        @DisplayName("Should be equal when address IDs are same")
        void shouldBeEqualWhenAddressIdsAreSame() {
            // Given
            Address address1 = Address.createTaipeiAddress("忠孝東路一段1號", "中正區", "100", AddressType.HOME);
            Address address2 = Address.createTaipeiAddress("信義路五段7號", "信義區", "110", AddressType.WORK);

            // When & Then
            assertEquals(address1, address1);
            assertNotEquals(address1, address2);
            assertEquals(address1.hashCode(), address1.hashCode());
        }

        @Test
        @DisplayName("Should not be equal to null or different type")
        void shouldNotBeEqualToNullOrDifferentType() {
            // Given
            Address address = Address.createTaipeiAddress("忠孝東路一段1號", "中正區", "100", AddressType.HOME);

            // When & Then
            assertNotEquals(address, null);
            assertNotEquals(address, "string");
        }
    }
}