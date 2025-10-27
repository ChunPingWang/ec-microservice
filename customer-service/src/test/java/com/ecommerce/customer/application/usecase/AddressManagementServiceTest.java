package com.ecommerce.customer.application.usecase;

import com.ecommerce.customer.application.dto.CreateAddressRequest;
import com.ecommerce.customer.application.port.out.CustomerPersistencePort;
import com.ecommerce.customer.domain.model.Address;
import com.ecommerce.customer.domain.model.AddressType;
import com.ecommerce.customer.domain.model.Customer;
import com.ecommerce.customer.domain.exception.CustomerNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AddressManagementService
 * Tests address management use cases
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Address Management Service Tests")
class AddressManagementServiceTest {

    @Mock
    private CustomerPersistencePort customerPersistencePort;

    private AddressManagementService addressManagementService;

    @BeforeEach
    void setUp() {
        addressManagementService = new AddressManagementService(customerPersistencePort);
    }

    @Nested
    @DisplayName("Add Address Tests")
    class AddAddressTests {

        @Test
        @DisplayName("Should add address to customer successfully")
        void shouldAddAddressToCustomerSuccessfully() {
            // Given
            String customerId = "CUST-123";
            Customer customer = Customer.create("Rex", "Chen", "rex.chen@example.com", "0912345678");
            
            CreateAddressRequest request = new CreateAddressRequest();
            request.setStreet("忠孝東路一段1號");
            request.setCity("台北市");
            request.setDistrict("中正區");
            request.setPostalCode("100");
            request.setCountry("台灣");
            request.setType(AddressType.HOME);

            when(customerPersistencePort.findById(customerId)).thenReturn(Optional.of(customer));
            when(customerPersistencePort.save(any(Customer.class))).thenReturn(customer);

            // When
            addressManagementService.addAddress(customerId, request);

            // Then
            verify(customerPersistencePort).findById(customerId);
            verify(customerPersistencePort).save(customer);
            assertEquals(1, customer.getAddresses().size());
            
            Address addedAddress = customer.getAddresses().get(0);
            assertEquals("忠孝東路一段1號", addedAddress.getStreet());
            assertEquals("中正區", addedAddress.getDistrict());
            assertEquals("100", addedAddress.getPostalCode());
            assertTrue(addedAddress.isPrimary()); // First address should be primary
        }

        @Test
        @DisplayName("Should throw exception when adding address to non-existent customer")
        void shouldThrowExceptionWhenAddingAddressToNonExistentCustomer() {
            // Given
            String customerId = "CUST-999";
            CreateAddressRequest request = new CreateAddressRequest();
            request.setStreet("忠孝東路一段1號");
            request.setCity("台北市");
            request.setDistrict("中正區");
            request.setPostalCode("100");
            request.setCountry("台灣");
            request.setType(AddressType.HOME);

            when(customerPersistencePort.findById(customerId)).thenReturn(Optional.empty());

            // When & Then
            CustomerNotFoundException exception = assertThrows(CustomerNotFoundException.class, () ->
                addressManagementService.addAddress(customerId, request)
            );
            
            verify(customerPersistencePort).findById(customerId);
            verifyNoMoreInteractions(customerPersistencePort);
        }
    }

    @Nested
    @DisplayName("Update Address Tests")
    class UpdateAddressTests {

        @Test
        @DisplayName("Should update address successfully")
        void shouldUpdateAddressSuccessfully() {
            // Given
            String customerId = "CUST-123";
            Customer customer = Customer.create("Rex", "Chen", "rex.chen@example.com", "0912345678");
            Address address = Address.createTaipeiAddress("忠孝東路一段1號", "中正區", "100", AddressType.HOME);
            customer.addAddress(address);
            String addressId = address.getAddressId();

            CreateAddressRequest updateRequest = new CreateAddressRequest();
            updateRequest.setStreet("信義路五段7號");
            updateRequest.setCity("台北市");
            updateRequest.setDistrict("信義區");
            updateRequest.setPostalCode("110");
            updateRequest.setCountry("台灣");
            updateRequest.setType(AddressType.WORK);

            when(customerPersistencePort.findById(customerId)).thenReturn(Optional.of(customer));
            when(customerPersistencePort.save(any(Customer.class))).thenReturn(customer);

            // When
            addressManagementService.updateAddress(customerId, addressId, updateRequest);

            // Then
            verify(customerPersistencePort).findById(customerId);
            verify(customerPersistencePort).save(customer);
            
            Address updatedAddress = customer.getAddresses().get(0);
            assertEquals("信義路五段7號", updatedAddress.getStreet());
            assertEquals("信義區", updatedAddress.getDistrict());
            assertEquals("110", updatedAddress.getPostalCode());
        }
    }

    @Nested
    @DisplayName("Remove Address Tests")
    class RemoveAddressTests {

        @Test
        @DisplayName("Should remove non-primary address successfully")
        void shouldRemoveNonPrimaryAddressSuccessfully() {
            // Given
            String customerId = "CUST-123";
            Customer customer = Customer.create("Rex", "Chen", "rex.chen@example.com", "0912345678");
            
            Address homeAddress = Address.createTaipeiAddress("忠孝東路一段1號", "中正區", "100", AddressType.HOME);
            Address workAddress = Address.createTaipeiAddress("信義路五段7號", "信義區", "110", AddressType.WORK);
            
            customer.addAddress(homeAddress);
            customer.addAddress(workAddress);
            
            String workAddressId = workAddress.getAddressId();

            when(customerPersistencePort.findById(customerId)).thenReturn(Optional.of(customer));
            when(customerPersistencePort.save(any(Customer.class))).thenReturn(customer);

            // When
            addressManagementService.removeAddress(customerId, workAddressId);

            // Then
            verify(customerPersistencePort).findById(customerId);
            verify(customerPersistencePort).save(customer);
            assertEquals(1, customer.getAddresses().size());
            assertEquals("忠孝東路一段1號", customer.getAddresses().get(0).getStreet());
        }
    }

    @Nested
    @DisplayName("Set Primary Address Tests")
    class SetPrimaryAddressTests {

        @Test
        @DisplayName("Should set primary address successfully")
        void shouldSetPrimaryAddressSuccessfully() {
            // Given
            String customerId = "CUST-123";
            Customer customer = Customer.create("Rex", "Chen", "rex.chen@example.com", "0912345678");
            
            Address homeAddress = Address.createTaipeiAddress("忠孝東路一段1號", "中正區", "100", AddressType.HOME);
            Address workAddress = Address.createTaipeiAddress("信義路五段7號", "信義區", "110", AddressType.WORK);
            
            customer.addAddress(homeAddress);
            customer.addAddress(workAddress);
            
            String workAddressId = workAddress.getAddressId();

            when(customerPersistencePort.findById(customerId)).thenReturn(Optional.of(customer));
            when(customerPersistencePort.save(any(Customer.class))).thenReturn(customer);

            // When
            addressManagementService.setPrimaryAddress(customerId, workAddressId);

            // Then
            verify(customerPersistencePort).findById(customerId);
            verify(customerPersistencePort).save(customer);
            
            assertFalse(homeAddress.isPrimary());
            assertTrue(workAddress.isPrimary());
            assertEquals(workAddress, customer.getPrimaryAddress());
        }
    }
}