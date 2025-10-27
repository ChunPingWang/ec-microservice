package com.ecommerce.customer.application.usecase;

import com.ecommerce.customer.application.dto.CustomerDto;
import com.ecommerce.customer.application.dto.CreateCustomerRequest;
import com.ecommerce.customer.application.dto.UpdateCustomerRequest;
import com.ecommerce.customer.application.mapper.CustomerMapper;
import com.ecommerce.customer.application.port.out.CustomerEventPort;
import com.ecommerce.customer.application.port.out.CustomerPersistencePort;
import com.ecommerce.customer.domain.event.CustomerRegisteredEvent;
import com.ecommerce.customer.domain.exception.CustomerNotFoundException;
import com.ecommerce.customer.domain.model.Customer;
import com.ecommerce.customer.domain.service.CustomerDomainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CustomerService application layer
 * Tests use case orchestration and business flow
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Customer Service Application Tests")
class CustomerServiceTest {

    @Mock
    private CustomerPersistencePort customerPersistencePort;

    @Mock
    private CustomerEventPort customerEventPort;

    @Mock
    private CustomerDomainService customerDomainService;

    @Mock
    private CustomerMapper customerMapper;

    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        customerService = new CustomerService(
            customerPersistencePort,
            customerEventPort,
            customerDomainService,
            customerMapper
        );
    }

    @Nested
    @DisplayName("Customer Registration Tests")
    class CustomerRegistrationTests {

        @Test
        @DisplayName("Should register customer successfully")
        void shouldRegisterCustomerSuccessfully() {
            // Given
            CreateCustomerRequest request = new CreateCustomerRequest();
            request.setFirstName("Rex");
            request.setLastName("Chen");
            request.setEmail("rex.chen@example.com");
            request.setPhoneNumber("0912345678");

            Customer customer = Customer.create("Rex", "Chen", "rex.chen@example.com", "0912345678");
            CustomerDto expectedDto = new CustomerDto();
            expectedDto.setId(customer.getCustomerId());
            expectedDto.setFirstName("Rex");
            expectedDto.setLastName("Chen");
            expectedDto.setEmail("rex.chen@example.com");

            when(customerDomainService.registerCustomer(
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                request.getPhoneNumber()
            )).thenReturn(customer);
            when(customerMapper.toDto(customer)).thenReturn(expectedDto);

            // When
            CustomerDto result = customerService.registerCustomer(request);

            // Then
            assertNotNull(result);
            assertEquals(expectedDto.getId(), result.getId());
            assertEquals(expectedDto.getFirstName(), result.getFirstName());
            assertEquals(expectedDto.getLastName(), result.getLastName());
            assertEquals(expectedDto.getEmail(), result.getEmail());

            // Verify domain service was called
            verify(customerDomainService).registerCustomer(
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                request.getPhoneNumber()
            );

            // Verify event was published
            ArgumentCaptor<CustomerRegisteredEvent> eventCaptor = ArgumentCaptor.forClass(CustomerRegisteredEvent.class);
            verify(customerEventPort).publishCustomerRegistered(eventCaptor.capture());
            
            CustomerRegisteredEvent publishedEvent = eventCaptor.getValue();
            assertEquals(customer.getCustomerId(), publishedEvent.getCustomerId());
            assertEquals(customer.getEmail(), publishedEvent.getEmail());
            assertEquals(customer.getFirstName(), publishedEvent.getFirstName());
            assertEquals(customer.getLastName(), publishedEvent.getLastName());
        }
    }

    @Nested
    @DisplayName("Customer Retrieval Tests")
    class CustomerRetrievalTests {

        @Test
        @DisplayName("Should get customer by ID successfully")
        void shouldGetCustomerByIdSuccessfully() {
            // Given
            String customerId = "CUST-123";
            Customer customer = Customer.create("Rex", "Chen", "rex.chen@example.com", "0912345678");
            CustomerDto expectedDto = new CustomerDto();
            expectedDto.setId(customerId);

            when(customerPersistencePort.findById(customerId)).thenReturn(Optional.of(customer));
            when(customerMapper.toDto(customer)).thenReturn(expectedDto);

            // When
            CustomerDto result = customerService.getCustomer(customerId);

            // Then
            assertNotNull(result);
            assertEquals(expectedDto.getId(), result.getId());
            verify(customerPersistencePort).findById(customerId);
            verify(customerMapper).toDto(customer);
        }

        @Test
        @DisplayName("Should throw exception when customer not found by ID")
        void shouldThrowExceptionWhenCustomerNotFoundById() {
            // Given
            String customerId = "CUST-999";
            when(customerPersistencePort.findById(customerId)).thenReturn(Optional.empty());

            // When & Then
            CustomerNotFoundException exception = assertThrows(CustomerNotFoundException.class, () ->
                customerService.getCustomer(customerId)
            );
            
            verify(customerPersistencePort).findById(customerId);
            verifyNoInteractions(customerMapper);
        }

        @Test
        @DisplayName("Should get customer by email successfully")
        void shouldGetCustomerByEmailSuccessfully() {
            // Given
            String email = "rex.chen@example.com";
            Customer customer = Customer.create("Rex", "Chen", email, "0912345678");
            CustomerDto expectedDto = new CustomerDto();
            expectedDto.setEmail(email);

            when(customerPersistencePort.findByEmail(email)).thenReturn(Optional.of(customer));
            when(customerMapper.toDto(customer)).thenReturn(expectedDto);

            // When
            CustomerDto result = customerService.getCustomerByEmail(email);

            // Then
            assertNotNull(result);
            assertEquals(expectedDto.getEmail(), result.getEmail());
            verify(customerPersistencePort).findByEmail(email);
            verify(customerMapper).toDto(customer);
        }

        @Test
        @DisplayName("Should throw exception when customer not found by email")
        void shouldThrowExceptionWhenCustomerNotFoundByEmail() {
            // Given
            String email = "notfound@example.com";
            when(customerPersistencePort.findByEmail(email)).thenReturn(Optional.empty());

            // When & Then
            CustomerNotFoundException exception = assertThrows(CustomerNotFoundException.class, () ->
                customerService.getCustomerByEmail(email)
            );
            
            verify(customerPersistencePort).findByEmail(email);
            verifyNoInteractions(customerMapper);
        }
    }

    @Nested
    @DisplayName("Customer Update Tests")
    class CustomerUpdateTests {

        @Test
        @DisplayName("Should update customer personal information successfully")
        void shouldUpdateCustomerPersonalInformationSuccessfully() {
            // Given
            String customerId = "CUST-123";
            UpdateCustomerRequest request = new UpdateCustomerRequest();
            request.setFirstName("Rex Updated");
            request.setLastName("Chen Updated");
            request.setPhoneNumber("0987654321");

            Customer customer = Customer.create("Rex", "Chen", "rex.chen@example.com", "0912345678");
            CustomerDto expectedDto = new CustomerDto();
            expectedDto.setId(customerId);

            when(customerPersistencePort.findById(customerId)).thenReturn(Optional.of(customer));
            when(customerPersistencePort.save(customer)).thenReturn(customer);
            when(customerMapper.toDto(customer)).thenReturn(expectedDto);

            // When
            CustomerDto result = customerService.updateCustomer(customerId, request);

            // Then
            assertNotNull(result);
            verify(customerPersistencePort).findById(customerId);
            verify(customerPersistencePort).save(customer);
            verify(customerMapper).toDto(customer);
        }

        @Test
        @DisplayName("Should update customer email through domain service")
        void shouldUpdateCustomerEmailThroughDomainService() {
            // Given
            String customerId = "CUST-123";
            String newEmail = "rex.new@example.com";
            UpdateCustomerRequest request = new UpdateCustomerRequest();
            request.setEmail(newEmail);

            Customer customer = Customer.create("Rex", "Chen", "rex.chen@example.com", "0912345678");
            CustomerDto expectedDto = new CustomerDto();

            when(customerPersistencePort.findById(customerId)).thenReturn(Optional.of(customer));
            when(customerPersistencePort.save(customer)).thenReturn(customer);
            when(customerMapper.toDto(customer)).thenReturn(expectedDto);

            // When
            CustomerDto result = customerService.updateCustomer(customerId, request);

            // Then
            assertNotNull(result);
            verify(customerDomainService).updateCustomerEmail(customerId, newEmail);
            verify(customerPersistencePort).save(customer);
        }

        @Test
        @DisplayName("Should throw exception when updating non-existent customer")
        void shouldThrowExceptionWhenUpdatingNonExistentCustomer() {
            // Given
            String customerId = "CUST-999";
            UpdateCustomerRequest request = new UpdateCustomerRequest();
            request.setFirstName("Updated Name");

            when(customerPersistencePort.findById(customerId)).thenReturn(Optional.empty());

            // When & Then
            CustomerNotFoundException exception = assertThrows(CustomerNotFoundException.class, () ->
                customerService.updateCustomer(customerId, request)
            );
            
            verify(customerPersistencePort).findById(customerId);
            verifyNoInteractions(customerDomainService);
            verifyNoMoreInteractions(customerPersistencePort);
        }
    }

    @Nested
    @DisplayName("Customer Status Management Tests")
    class CustomerStatusManagementTests {

        @Test
        @DisplayName("Should deactivate customer successfully")
        void shouldDeactivateCustomerSuccessfully() {
            // Given
            String customerId = "CUST-123";
            Customer customer = Customer.create("Rex", "Chen", "rex.chen@example.com", "0912345678");

            when(customerPersistencePort.findById(customerId)).thenReturn(Optional.of(customer));
            when(customerPersistencePort.save(customer)).thenReturn(customer);

            // When
            customerService.deactivateCustomer(customerId);

            // Then
            verify(customerPersistencePort).findById(customerId);
            verify(customerPersistencePort).save(customer);
        }

        @Test
        @DisplayName("Should activate customer successfully")
        void shouldActivateCustomerSuccessfully() {
            // Given
            String customerId = "CUST-123";
            Customer customer = Customer.create("Rex", "Chen", "rex.chen@example.com", "0912345678");

            when(customerPersistencePort.findById(customerId)).thenReturn(Optional.of(customer));
            when(customerPersistencePort.save(customer)).thenReturn(customer);

            // When
            customerService.activateCustomer(customerId);

            // Then
            verify(customerPersistencePort).findById(customerId);
            verify(customerPersistencePort).save(customer);
        }

        @Test
        @DisplayName("Should record login successfully")
        void shouldRecordLoginSuccessfully() {
            // Given
            String customerId = "CUST-123";
            Customer customer = Customer.create("Rex", "Chen", "rex.chen@example.com", "0912345678");

            when(customerPersistencePort.findById(customerId)).thenReturn(Optional.of(customer));
            when(customerPersistencePort.save(customer)).thenReturn(customer);

            // When
            customerService.recordLogin(customerId);

            // Then
            verify(customerPersistencePort).findById(customerId);
            verify(customerPersistencePort).save(customer);
        }
    }

    @Nested
    @DisplayName("Customer Search Tests")
    class CustomerSearchTests {

        @Test
        @DisplayName("Should search customers by name successfully")
        void shouldSearchCustomersByNameSuccessfully() {
            // Given
            String searchTerm = "Rex";
            List<Customer> customers = Arrays.asList(
                Customer.create("Rex", "Chen", "rex.chen@example.com", "0912345678"),
                Customer.create("Rex", "Wang", "rex.wang@example.com", "0987654321")
            );
            List<CustomerDto> expectedDtos = Arrays.asList(new CustomerDto(), new CustomerDto());

            when(customerPersistencePort.findByNameContaining(searchTerm)).thenReturn(customers);
            when(customerMapper.toDtoList(customers)).thenReturn(expectedDtos);

            // When
            List<CustomerDto> result = customerService.searchCustomers(searchTerm);

            // Then
            assertNotNull(result);
            assertEquals(2, result.size());
            verify(customerPersistencePort).findByNameContaining(searchTerm);
            verify(customerMapper).toDtoList(customers);
        }

        @Test
        @DisplayName("Should get customers by city successfully")
        void shouldGetCustomersByCitySuccessfully() {
            // Given
            String city = "台北市";
            List<Customer> customers = Arrays.asList(
                Customer.create("Rex", "Chen", "rex.chen@example.com", "0912345678")
            );
            List<CustomerDto> expectedDtos = Arrays.asList(new CustomerDto());

            when(customerPersistencePort.findByCity(city)).thenReturn(customers);
            when(customerMapper.toDtoList(customers)).thenReturn(expectedDtos);

            // When
            List<CustomerDto> result = customerService.getCustomersByCity(city);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(customerPersistencePort).findByCity(city);
            verify(customerMapper).toDtoList(customers);
        }

        @Test
        @DisplayName("Should check if customer can place orders")
        void shouldCheckIfCustomerCanPlaceOrders() {
            // Given
            String customerId = "CUST-123";
            when(customerDomainService.canPlaceOrders(customerId)).thenReturn(true);

            // When
            boolean result = customerService.canPlaceOrders(customerId);

            // Then
            assertTrue(result);
            verify(customerDomainService).canPlaceOrders(customerId);
        }
    }
}