package com.ecommerce.customer.infrastructure.adapter.web;

import com.ecommerce.customer.CustomerServiceApplication;
import com.ecommerce.customer.application.dto.CreateCustomerRequest;
import com.ecommerce.customer.application.dto.UpdateCustomerRequest;
import com.ecommerce.customer.domain.model.Customer;
import com.ecommerce.customer.infrastructure.adapter.persistence.CustomerJpaAdapter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for CustomerController REST API
 * Tests complete HTTP request/response flow
 */
@SpringBootTest(classes = CustomerServiceApplication.class)
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Customer Controller API Integration Tests")
class CustomerControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private CustomerJpaAdapter customerJpaAdapter;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Nested
    @DisplayName("Customer Registration API Tests")
    class CustomerRegistrationApiTests {

        @Test
        @DisplayName("Should register customer successfully via API")
        void shouldRegisterCustomerSuccessfullyViaApi() throws Exception {
            // Given
            CreateCustomerRequest request = new CreateCustomerRequest();
            request.setFirstName("Rex");
            request.setLastName("Chen");
            request.setEmail("rex.chen@example.com");
            request.setPhoneNumber("0912345678");

            String requestJson = objectMapper.writeValueAsString(request);

            // When & Then
            mockMvc.perform(post("/api/v1/customers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data.id", notNullValue()))
                    .andExpect(jsonPath("$.data.firstName", is("Rex")))
                    .andExpect(jsonPath("$.data.lastName", is("Chen")))
                    .andExpect(jsonPath("$.data.email", is("rex.chen@example.com")))
                    .andExpect(jsonPath("$.data.phoneNumber", is("0912345678")))
                    .andExpect(jsonPath("$.data.status", is("ACTIVE")));
        }

        @Test
        @DisplayName("Should return validation error for invalid email")
        void shouldReturnValidationErrorForInvalidEmail() throws Exception {
            // Given
            CreateCustomerRequest request = new CreateCustomerRequest();
            request.setFirstName("Rex");
            request.setLastName("Chen");
            request.setEmail("invalid-email");
            request.setPhoneNumber("0912345678");

            String requestJson = objectMapper.writeValueAsString(request);

            // When & Then
            mockMvc.perform(post("/api/v1/customers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.message", containsString("Invalid email format")));
        }

        @Test
        @DisplayName("Should return validation error for missing required fields")
        void shouldReturnValidationErrorForMissingRequiredFields() throws Exception {
            // Given
            CreateCustomerRequest request = new CreateCustomerRequest();
            request.setFirstName("Rex");
            // Missing lastName, email, phoneNumber

            String requestJson = objectMapper.writeValueAsString(request);

            // When & Then
            mockMvc.perform(post("/api/v1/customers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.message", notNullValue()));
        }

        @Test
        @DisplayName("Should return validation error for invalid phone number")
        void shouldReturnValidationErrorForInvalidPhoneNumber() throws Exception {
            // Given
            CreateCustomerRequest request = new CreateCustomerRequest();
            request.setFirstName("Rex");
            request.setLastName("Chen");
            request.setEmail("rex.chen@example.com");
            request.setPhoneNumber("123"); // Invalid format

            String requestJson = objectMapper.writeValueAsString(request);

            // When & Then
            mockMvc.perform(post("/api/v1/customers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.message", containsString("Invalid phone number format")));
        }
    }

    @Nested
    @DisplayName("Customer Retrieval API Tests")
    class CustomerRetrievalApiTests {

        @Test
        @DisplayName("Should get customer by ID successfully")
        void shouldGetCustomerByIdSuccessfully() throws Exception {
            // Given
            Customer customer = Customer.create("Rex", "Chen", "rex.chen@example.com", "0912345678");
            Customer savedCustomer = customerJpaAdapter.save(customer);

            // When & Then
            mockMvc.perform(get("/api/v1/customers/{customerId}", savedCustomer.getCustomerId())
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data.id", is(savedCustomer.getCustomerId())))
                    .andExpect(jsonPath("$.data.firstName", is("Rex")))
                    .andExpect(jsonPath("$.data.lastName", is("Chen")))
                    .andExpect(jsonPath("$.data.email", is("rex.chen@example.com")));
        }

        @Test
        @DisplayName("Should return 404 when customer not found")
        void shouldReturn404WhenCustomerNotFound() throws Exception {
            // Given
            String nonExistentCustomerId = "CUST-999";

            // When & Then
            mockMvc.perform(get("/api/v1/customers/{customerId}", nonExistentCustomerId)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.message", containsString("Customer not found")));
        }

        @Test
        @DisplayName("Should get customer by email successfully")
        void shouldGetCustomerByEmailSuccessfully() throws Exception {
            // Given
            String email = "rex.chen@example.com";
            Customer customer = Customer.create("Rex", "Chen", email, "0912345678");
            customerJpaAdapter.save(customer);

            // When & Then
            mockMvc.perform(get("/api/v1/customers/by-email")
                    .param("email", email)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data.email", is(email)))
                    .andExpect(jsonPath("$.data.firstName", is("Rex")));
        }

        @Test
        @DisplayName("Should search customers by name")
        void shouldSearchCustomersByName() throws Exception {
            // Given
            Customer customer1 = Customer.create("Rex", "Chen", "rex.chen@example.com", "0912345678");
            Customer customer2 = Customer.create("Rex", "Wang", "rex.wang@example.com", "0987654321");
            Customer customer3 = Customer.create("John", "Doe", "john.doe@example.com", "0911111111");
            
            customerJpaAdapter.save(customer1);
            customerJpaAdapter.save(customer2);
            customerJpaAdapter.save(customer3);

            // When & Then
            mockMvc.perform(get("/api/v1/customers/search")
                    .param("q", "Rex")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data", hasSize(2)))
                    .andExpect(jsonPath("$.data[*].firstName", everyItem(is("Rex"))));
        }
    }

    @Nested
    @DisplayName("Customer Update API Tests")
    class CustomerUpdateApiTests {

        @Test
        @DisplayName("Should update customer successfully")
        void shouldUpdateCustomerSuccessfully() throws Exception {
            // Given
            Customer customer = Customer.create("Rex", "Chen", "rex.chen@example.com", "0912345678");
            Customer savedCustomer = customerJpaAdapter.save(customer);

            UpdateCustomerRequest request = new UpdateCustomerRequest();
            request.setFirstName("Rex Updated");
            request.setLastName("Chen Updated");
            request.setPhoneNumber("0987654321");

            String requestJson = objectMapper.writeValueAsString(request);

            // When & Then
            mockMvc.perform(put("/api/v1/customers/{customerId}", savedCustomer.getCustomerId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data.firstName", is("Rex Updated")))
                    .andExpect(jsonPath("$.data.lastName", is("Chen Updated")))
                    .andExpect(jsonPath("$.data.phoneNumber", is("0987654321")));
        }

        @Test
        @DisplayName("Should return 404 when updating non-existent customer")
        void shouldReturn404WhenUpdatingNonExistentCustomer() throws Exception {
            // Given
            String nonExistentCustomerId = "CUST-999";
            UpdateCustomerRequest request = new UpdateCustomerRequest();
            request.setFirstName("Updated Name");

            String requestJson = objectMapper.writeValueAsString(request);

            // When & Then
            mockMvc.perform(put("/api/v1/customers/{customerId}", nonExistentCustomerId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.message", containsString("Customer not found")));
        }

        @Test
        @DisplayName("Should return validation error for invalid update data")
        void shouldReturnValidationErrorForInvalidUpdateData() throws Exception {
            // Given
            Customer customer = Customer.create("Rex", "Chen", "rex.chen@example.com", "0912345678");
            Customer savedCustomer = customerJpaAdapter.save(customer);

            UpdateCustomerRequest request = new UpdateCustomerRequest();
            request.setPhoneNumber("invalid-phone");

            String requestJson = objectMapper.writeValueAsString(request);

            // When & Then
            mockMvc.perform(put("/api/v1/customers/{customerId}", savedCustomer.getCustomerId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.message", containsString("Invalid phone number format")));
        }
    }

    @Nested
    @DisplayName("Customer Status Management API Tests")
    class CustomerStatusManagementApiTests {

        @Test
        @DisplayName("Should deactivate customer successfully")
        void shouldDeactivateCustomerSuccessfully() throws Exception {
            // Given
            Customer customer = Customer.create("Rex", "Chen", "rex.chen@example.com", "0912345678");
            Customer savedCustomer = customerJpaAdapter.save(customer);

            // When & Then
            mockMvc.perform(patch("/api/v1/customers/{customerId}/deactivate", savedCustomer.getCustomerId())
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("Customer deactivated successfully")));
        }

        @Test
        @DisplayName("Should activate customer successfully")
        void shouldActivateCustomerSuccessfully() throws Exception {
            // Given
            Customer customer = Customer.create("Rex", "Chen", "rex.chen@example.com", "0912345678");
            customer.deactivate();
            Customer savedCustomer = customerJpaAdapter.save(customer);

            // When & Then
            mockMvc.perform(patch("/api/v1/customers/{customerId}/activate", savedCustomer.getCustomerId())
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("Customer activated successfully")));
        }

        @Test
        @DisplayName("Should record login successfully")
        void shouldRecordLoginSuccessfully() throws Exception {
            // Given
            Customer customer = Customer.create("Rex", "Chen", "rex.chen@example.com", "0912345678");
            Customer savedCustomer = customerJpaAdapter.save(customer);

            // When & Then
            mockMvc.perform(patch("/api/v1/customers/{customerId}/login", savedCustomer.getCustomerId())
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("Login recorded successfully")));
        }

        @Test
        @DisplayName("Should return 404 for status operations on non-existent customer")
        void shouldReturn404ForStatusOperationsOnNonExistentCustomer() throws Exception {
            // Given
            String nonExistentCustomerId = "CUST-999";

            // When & Then
            mockMvc.perform(patch("/api/v1/customers/{customerId}/deactivate", nonExistentCustomerId)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.message", containsString("Customer not found")));
        }
    }

    @Nested
    @DisplayName("Customer Business Logic API Tests")
    class CustomerBusinessLogicApiTests {

        @Test
        @DisplayName("Should check if customer can place orders")
        void shouldCheckIfCustomerCanPlaceOrders() throws Exception {
            // Given
            Customer customer = Customer.create("Rex", "Chen", "rex.chen@example.com", "0912345678");
            Customer savedCustomer = customerJpaAdapter.save(customer);

            // When & Then
            mockMvc.perform(get("/api/v1/customers/{customerId}/can-place-orders", savedCustomer.getCustomerId())
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data", is(true)));
        }

        @Test
        @DisplayName("Should return false for deactivated customer order capability")
        void shouldReturnFalseForDeactivatedCustomerOrderCapability() throws Exception {
            // Given
            Customer customer = Customer.create("Rex", "Chen", "rex.chen@example.com", "0912345678");
            customer.deactivate();
            Customer savedCustomer = customerJpaAdapter.save(customer);

            // When & Then
            mockMvc.perform(get("/api/v1/customers/{customerId}/can-place-orders", savedCustomer.getCustomerId())
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data", is(false)));
        }
    }

    @Nested
    @DisplayName("Error Handling API Tests")
    class ErrorHandlingApiTests {

        @Test
        @DisplayName("Should handle malformed JSON request")
        void shouldHandleMalformedJsonRequest() throws Exception {
            // Given
            String malformedJson = "{ invalid json }";

            // When & Then
            mockMvc.perform(post("/api/v1/customers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(malformedJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.message", notNullValue()));
        }

        @Test
        @DisplayName("Should handle unsupported HTTP method")
        void shouldHandleUnsupportedHttpMethod() throws Exception {
            // When & Then
            mockMvc.perform(patch("/api/v1/customers")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isMethodNotAllowed());
        }

        @Test
        @DisplayName("Should handle missing content type")
        void shouldHandleMissingContentType() throws Exception {
            // Given
            CreateCustomerRequest request = new CreateCustomerRequest();
            request.setFirstName("Rex");
            request.setLastName("Chen");
            request.setEmail("rex.chen@example.com");
            request.setPhoneNumber("0912345678");

            String requestJson = objectMapper.writeValueAsString(request);

            // When & Then
            mockMvc.perform(post("/api/v1/customers")
                    .content(requestJson)) // No content type specified
                    .andExpect(status().isUnsupportedMediaType());
        }
    }
}