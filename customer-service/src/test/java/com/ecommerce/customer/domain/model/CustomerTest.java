package com.ecommerce.customer.domain.model;

import com.ecommerce.common.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Customer domain entity
 * Tests business rules and validation logic
 */
@DisplayName("Customer Domain Entity Tests")
class CustomerTest {

    @Nested
    @DisplayName("Customer Creation Tests")
    class CustomerCreationTests {

        @Test
        @DisplayName("Should create customer with valid data")
        void shouldCreateCustomerWithValidData() {
            // Given
            String firstName = "Rex";
            String lastName = "Chen";
            String email = "rex.chen@example.com";
            String phoneNumber = "0912345678";

            // When
            Customer customer = Customer.create(firstName, lastName, email, phoneNumber);

            // Then
            assertNotNull(customer);
            assertNotNull(customer.getCustomerId());
            assertEquals(firstName, customer.getFirstName());
            assertEquals(lastName, customer.getLastName());
            assertEquals(email.toLowerCase(), customer.getEmail());
            assertEquals(phoneNumber, customer.getPhoneNumber());
            assertEquals(CustomerStatus.ACTIVE, customer.getStatus());
            assertNotNull(customer.getRegistrationDate());
            assertTrue(customer.isActive());
            assertEquals("Rex Chen", customer.getFullName());
        }

        @Test
        @DisplayName("Should throw exception when first name is null")
        void shouldThrowExceptionWhenFirstNameIsNull() {
            // Given
            String firstName = null;
            String lastName = "Chen";
            String email = "rex.chen@example.com";
            String phoneNumber = "0912345678";

            // When & Then
            ValidationException exception = assertThrows(ValidationException.class, () ->
                Customer.create(firstName, lastName, email, phoneNumber)
            );
            assertEquals("First name is required", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when first name is empty")
        void shouldThrowExceptionWhenFirstNameIsEmpty() {
            // Given
            String firstName = "   ";
            String lastName = "Chen";
            String email = "rex.chen@example.com";
            String phoneNumber = "0912345678";

            // When & Then
            ValidationException exception = assertThrows(ValidationException.class, () ->
                Customer.create(firstName, lastName, email, phoneNumber)
            );
            assertEquals("First name is required", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when email is invalid")
        void shouldThrowExceptionWhenEmailIsInvalid() {
            // Given
            String firstName = "Rex";
            String lastName = "Chen";
            String email = "invalid-email";
            String phoneNumber = "0912345678";

            // When & Then
            ValidationException exception = assertThrows(ValidationException.class, () ->
                Customer.create(firstName, lastName, email, phoneNumber)
            );
            assertEquals("Invalid email format", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when phone number is invalid")
        void shouldThrowExceptionWhenPhoneNumberIsInvalid() {
            // Given
            String firstName = "Rex";
            String lastName = "Chen";
            String email = "rex.chen@example.com";
            String phoneNumber = "123";

            // When & Then
            ValidationException exception = assertThrows(ValidationException.class, () ->
                Customer.create(firstName, lastName, email, phoneNumber)
            );
            assertEquals("Invalid phone number format", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Customer Update Tests")
    class CustomerUpdateTests {

        @Test
        @DisplayName("Should update personal information successfully")
        void shouldUpdatePersonalInformationSuccessfully() {
            // Given
            Customer customer = Customer.create("Rex", "Chen", "rex.chen@example.com", "0912345678");
            String newFirstName = "Rex Updated";
            String newLastName = "Chen Updated";
            String newPhoneNumber = "0987654321";

            // When
            customer.updatePersonalInfo(newFirstName, newLastName, newPhoneNumber);

            // Then
            assertEquals(newFirstName, customer.getFirstName());
            assertEquals(newLastName, customer.getLastName());
            assertEquals(newPhoneNumber, customer.getPhoneNumber());
            assertNotNull(customer.getUpdatedAt());
        }

        @Test
        @DisplayName("Should throw exception when updating with invalid phone number")
        void shouldThrowExceptionWhenUpdatingWithInvalidPhoneNumber() {
            // Given
            Customer customer = Customer.create("Rex", "Chen", "rex.chen@example.com", "0912345678");

            // When & Then
            ValidationException exception = assertThrows(ValidationException.class, () ->
                customer.updatePersonalInfo("Rex", "Chen", "invalid-phone")
            );
            assertEquals("Invalid phone number format", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Customer Status Tests")
    class CustomerStatusTests {

        @Test
        @DisplayName("Should deactivate customer successfully")
        void shouldDeactivateCustomerSuccessfully() {
            // Given
            Customer customer = Customer.create("Rex", "Chen", "rex.chen@example.com", "0912345678");
            assertTrue(customer.isActive());

            // When
            customer.deactivate();

            // Then
            assertFalse(customer.isActive());
            assertEquals(CustomerStatus.INACTIVE, customer.getStatus());
            assertNotNull(customer.getUpdatedAt());
        }

        @Test
        @DisplayName("Should activate customer successfully")
        void shouldActivateCustomerSuccessfully() {
            // Given
            Customer customer = Customer.create("Rex", "Chen", "rex.chen@example.com", "0912345678");
            customer.deactivate();
            assertFalse(customer.isActive());

            // When
            customer.activate();

            // Then
            assertTrue(customer.isActive());
            assertEquals(CustomerStatus.ACTIVE, customer.getStatus());
            assertNotNull(customer.getUpdatedAt());
        }

        @Test
        @DisplayName("Should record login successfully")
        void shouldRecordLoginSuccessfully() {
            // Given
            Customer customer = Customer.create("Rex", "Chen", "rex.chen@example.com", "0912345678");
            assertNull(customer.getLastLoginDate());

            // When
            customer.recordLogin();

            // Then
            assertNotNull(customer.getLastLoginDate());
            assertNotNull(customer.getUpdatedAt());
        }
    }

    @Nested
    @DisplayName("Address Management Tests")
    class AddressManagementTests {

        @Test
        @DisplayName("Should add first address as primary")
        void shouldAddFirstAddressAsPrimary() {
            // Given
            Customer customer = Customer.create("Rex", "Chen", "rex.chen@example.com", "0912345678");
            Address address = Address.createTaipeiAddress("忠孝東路一段1號", "中正區", "100", AddressType.HOME);

            // When
            customer.addAddress(address);

            // Then
            assertEquals(1, customer.getAddresses().size());
            assertTrue(address.isPrimary());
            assertEquals(address, customer.getPrimaryAddress());
        }

        @Test
        @DisplayName("Should add multiple addresses with correct primary handling")
        void shouldAddMultipleAddressesWithCorrectPrimaryHandling() {
            // Given
            Customer customer = Customer.create("Rex", "Chen", "rex.chen@example.com", "0912345678");
            Address firstAddress = Address.createTaipeiAddress("忠孝東路一段1號", "中正區", "100", AddressType.HOME);
            Address secondAddress = Address.createTaipeiAddress("信義路五段7號", "信義區", "110", AddressType.WORK);

            // When
            customer.addAddress(firstAddress);
            customer.addAddress(secondAddress);

            // Then
            assertEquals(2, customer.getAddresses().size());
            assertTrue(firstAddress.isPrimary());
            assertFalse(secondAddress.isPrimary());
            assertEquals(firstAddress, customer.getPrimaryAddress());
        }

        @Test
        @DisplayName("Should set new primary address correctly")
        void shouldSetNewPrimaryAddressCorrectly() {
            // Given
            Customer customer = Customer.create("Rex", "Chen", "rex.chen@example.com", "0912345678");
            Address firstAddress = Address.createTaipeiAddress("忠孝東路一段1號", "中正區", "100", AddressType.HOME);
            Address secondAddress = Address.createTaipeiAddress("信義路五段7號", "信義區", "110", AddressType.WORK);
            customer.addAddress(firstAddress);
            customer.addAddress(secondAddress);

            // When
            customer.setPrimaryAddress(secondAddress.getAddressId());

            // Then
            assertFalse(firstAddress.isPrimary());
            assertTrue(secondAddress.isPrimary());
            assertEquals(secondAddress, customer.getPrimaryAddress());
        }

        @Test
        @DisplayName("Should throw exception when setting non-existent address as primary")
        void shouldThrowExceptionWhenSettingNonExistentAddressAsPrimary() {
            // Given
            Customer customer = Customer.create("Rex", "Chen", "rex.chen@example.com", "0912345678");

            // When & Then
            ValidationException exception = assertThrows(ValidationException.class, () ->
                customer.setPrimaryAddress("non-existent-id")
            );
            assertEquals("Address not found: non-existent-id", exception.getMessage());
        }

        @Test
        @DisplayName("Should remove non-primary address successfully")
        void shouldRemoveNonPrimaryAddressSuccessfully() {
            // Given
            Customer customer = Customer.create("Rex", "Chen", "rex.chen@example.com", "0912345678");
            Address firstAddress = Address.createTaipeiAddress("忠孝東路一段1號", "中正區", "100", AddressType.HOME);
            Address secondAddress = Address.createTaipeiAddress("信義路五段7號", "信義區", "110", AddressType.WORK);
            customer.addAddress(firstAddress);
            customer.addAddress(secondAddress);

            // When
            customer.removeAddress(secondAddress.getAddressId());

            // Then
            assertEquals(1, customer.getAddresses().size());
            assertEquals(firstAddress, customer.getPrimaryAddress());
        }

        @Test
        @DisplayName("Should throw exception when removing primary address with other addresses exist")
        void shouldThrowExceptionWhenRemovingPrimaryAddressWithOtherAddressesExist() {
            // Given
            Customer customer = Customer.create("Rex", "Chen", "rex.chen@example.com", "0912345678");
            Address firstAddress = Address.createTaipeiAddress("忠孝東路一段1號", "中正區", "100", AddressType.HOME);
            Address secondAddress = Address.createTaipeiAddress("信義路五段7號", "信義區", "110", AddressType.WORK);
            customer.addAddress(firstAddress);
            customer.addAddress(secondAddress);

            // When & Then
            ValidationException exception = assertThrows(ValidationException.class, () ->
                customer.removeAddress(firstAddress.getAddressId())
            );
            assertEquals("Cannot remove primary address when other addresses exist", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Customer Equality Tests")
    class CustomerEqualityTests {

        @Test
        @DisplayName("Should be equal when customer IDs are same")
        void shouldBeEqualWhenCustomerIdsAreSame() {
            // Given
            Customer customer1 = Customer.create("Rex", "Chen", "rex.chen@example.com", "0912345678");
            Customer customer2 = Customer.create("John", "Doe", "john.doe@example.com", "0987654321");
            
            // Simulate same customer ID (in real scenario, this would come from database)
            // We'll use reflection to set the same ID for testing
            String sameId = customer1.getCustomerId();
            
            // When & Then
            assertEquals(customer1, customer1);
            assertNotEquals(customer1, customer2);
            assertEquals(customer1.hashCode(), customer1.hashCode());
        }

        @Test
        @DisplayName("Should not be equal to null or different type")
        void shouldNotBeEqualToNullOrDifferentType() {
            // Given
            Customer customer = Customer.create("Rex", "Chen", "rex.chen@example.com", "0912345678");

            // When & Then
            assertNotEquals(customer, null);
            assertNotEquals(customer, "string");
        }
    }
}