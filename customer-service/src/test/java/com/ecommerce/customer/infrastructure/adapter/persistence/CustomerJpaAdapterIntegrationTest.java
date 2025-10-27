package com.ecommerce.customer.infrastructure.adapter.persistence;

import com.ecommerce.customer.domain.model.Address;
import com.ecommerce.customer.domain.model.AddressType;
import com.ecommerce.customer.domain.model.Customer;
import com.ecommerce.customer.domain.model.CustomerStatus;
import com.ecommerce.customer.infrastructure.adapter.persistence.entity.CustomerJpaEntity;
import com.ecommerce.customer.infrastructure.adapter.persistence.repository.CustomerJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for CustomerJpaAdapter
 * Tests database operations and JPA mappings
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Customer JPA Adapter Integration Tests")
class CustomerJpaAdapterIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CustomerJpaRepository customerJpaRepository;

    private CustomerJpaAdapter customerJpaAdapter;

    @BeforeEach
    void setUp() {
        customerJpaAdapter = new CustomerJpaAdapter(customerJpaRepository, new CustomerJpaMapper());
    }

    @Nested
    @DisplayName("Customer Persistence Tests")
    class CustomerPersistenceTests {

        @Test
        @DisplayName("Should save and retrieve customer successfully")
        void shouldSaveAndRetrieveCustomerSuccessfully() {
            // Given
            Customer customer = Customer.create("Rex", "Chen", "rex.chen@example.com", "0912345678");
            Address address = Address.createTaipeiAddress("忠孝東路一段1號", "中正區", "100", AddressType.HOME);
            customer.addAddress(address);

            // When
            Customer savedCustomer = customerJpaAdapter.save(customer);
            entityManager.flush();
            entityManager.clear();

            Optional<Customer> retrievedCustomer = customerJpaAdapter.findById(savedCustomer.getCustomerId());

            // Then
            assertTrue(retrievedCustomer.isPresent());
            Customer found = retrievedCustomer.get();
            assertEquals(savedCustomer.getCustomerId(), found.getCustomerId());
            assertEquals("Rex", found.getFirstName());
            assertEquals("Chen", found.getLastName());
            assertEquals("rex.chen@example.com", found.getEmail());
            assertEquals("0912345678", found.getPhoneNumber());
            assertEquals(CustomerStatus.ACTIVE, found.getStatus());
            assertEquals(1, found.getAddresses().size());
            
            Address foundAddress = found.getAddresses().get(0);
            assertEquals("忠孝東路一段1號", foundAddress.getStreet());
            assertEquals("中正區", foundAddress.getDistrict());
            assertEquals("100", foundAddress.getPostalCode());
            assertTrue(foundAddress.isPrimary());
        }

        @Test
        @DisplayName("Should update customer successfully")
        void shouldUpdateCustomerSuccessfully() {
            // Given
            Customer customer = Customer.create("Rex", "Chen", "rex.chen@example.com", "0912345678");
            Customer savedCustomer = customerJpaAdapter.save(customer);
            entityManager.flush();

            // When
            savedCustomer.updatePersonalInfo("Rex Updated", "Chen Updated", "0987654321");
            Customer updatedCustomer = customerJpaAdapter.save(savedCustomer);
            entityManager.flush();
            entityManager.clear();

            Optional<Customer> retrievedCustomer = customerJpaAdapter.findById(updatedCustomer.getCustomerId());

            // Then
            assertTrue(retrievedCustomer.isPresent());
            Customer found = retrievedCustomer.get();
            assertEquals("Rex Updated", found.getFirstName());
            assertEquals("Chen Updated", found.getLastName());
            assertEquals("0987654321", found.getPhoneNumber());
            assertNotNull(found.getUpdatedAt());
        }

        @Test
        @DisplayName("Should delete customer successfully")
        void shouldDeleteCustomerSuccessfully() {
            // Given
            Customer customer = Customer.create("Rex", "Chen", "rex.chen@example.com", "0912345678");
            Customer savedCustomer = customerJpaAdapter.save(customer);
            entityManager.flush();

            // When
            customerJpaAdapter.deleteById(savedCustomer.getCustomerId());
            entityManager.flush();

            Optional<Customer> retrievedCustomer = customerJpaAdapter.findById(savedCustomer.getCustomerId());

            // Then
            assertFalse(retrievedCustomer.isPresent());
        }
    }

    @Nested
    @DisplayName("Customer Query Tests")
    class CustomerQueryTests {

        @Test
        @DisplayName("Should find customer by email successfully")
        void shouldFindCustomerByEmailSuccessfully() {
            // Given
            String email = "rex.chen@example.com";
            Customer customer = Customer.create("Rex", "Chen", email, "0912345678");
            customerJpaAdapter.save(customer);
            entityManager.flush();

            // When
            Optional<Customer> foundCustomer = customerJpaAdapter.findByEmail(email);

            // Then
            assertTrue(foundCustomer.isPresent());
            assertEquals(email, foundCustomer.get().getEmail());
            assertEquals("Rex", foundCustomer.get().getFirstName());
        }

        @Test
        @DisplayName("Should return empty when customer not found by email")
        void shouldReturnEmptyWhenCustomerNotFoundByEmail() {
            // Given
            String nonExistentEmail = "notfound@example.com";

            // When
            Optional<Customer> foundCustomer = customerJpaAdapter.findByEmail(nonExistentEmail);

            // Then
            assertFalse(foundCustomer.isPresent());
        }

        @Test
        @DisplayName("Should find customers by name containing search term")
        void shouldFindCustomersByNameContainingSearchTerm() {
            // Given
            Customer customer1 = Customer.create("Rex", "Chen", "rex.chen@example.com", "0912345678");
            Customer customer2 = Customer.create("Rex", "Wang", "rex.wang@example.com", "0987654321");
            Customer customer3 = Customer.create("John", "Doe", "john.doe@example.com", "0911111111");
            
            customerJpaAdapter.save(customer1);
            customerJpaAdapter.save(customer2);
            customerJpaAdapter.save(customer3);
            entityManager.flush();

            // When
            List<Customer> foundCustomers = customerJpaAdapter.findByNameContaining("Rex");

            // Then
            assertEquals(2, foundCustomers.size());
            assertTrue(foundCustomers.stream().allMatch(c -> c.getFirstName().contains("Rex")));
        }

        @Test
        @DisplayName("Should find customers by city")
        void shouldFindCustomersByCity() {
            // Given
            Customer customer1 = Customer.create("Rex", "Chen", "rex.chen@example.com", "0912345678");
            Address taipeiAddress = Address.createTaipeiAddress("忠孝東路一段1號", "中正區", "100", AddressType.HOME);
            customer1.addAddress(taipeiAddress);

            Customer customer2 = Customer.create("John", "Doe", "john.doe@example.com", "0987654321");
            Address kaohsiungAddress = Address.create("中山路1號", "高雄市", "前金區", "80041", "台灣", AddressType.HOME);
            customer2.addAddress(kaohsiungAddress);

            customerJpaAdapter.save(customer1);
            customerJpaAdapter.save(customer2);
            entityManager.flush();

            // When
            List<Customer> taipeiCustomers = customerJpaAdapter.findByCity("台北市");

            // Then
            assertEquals(1, taipeiCustomers.size());
            assertEquals("Rex", taipeiCustomers.get(0).getFirstName());
            assertTrue(taipeiCustomers.get(0).getAddresses().stream()
                .anyMatch(addr -> "台北市".equals(addr.getCity())));
        }

        @Test
        @DisplayName("Should check if customer exists by email")
        void shouldCheckIfCustomerExistsByEmail() {
            // Given
            String email = "rex.chen@example.com";
            Customer customer = Customer.create("Rex", "Chen", email, "0912345678");
            customerJpaAdapter.save(customer);
            entityManager.flush();

            // When
            boolean exists = customerJpaAdapter.existsByEmail(email);
            boolean notExists = customerJpaAdapter.existsByEmail("notfound@example.com");

            // Then
            assertTrue(exists);
            assertFalse(notExists);
        }
    }

    @Nested
    @DisplayName("Address Management Tests")
    class AddressManagementTests {

        @Test
        @DisplayName("Should persist multiple addresses correctly")
        void shouldPersistMultipleAddressesCorrectly() {
            // Given
            Customer customer = Customer.create("Rex", "Chen", "rex.chen@example.com", "0912345678");
            
            Address homeAddress = Address.createTaipeiAddress("忠孝東路一段1號", "中正區", "100", AddressType.HOME);
            Address workAddress = Address.createTaipeiAddress("信義路五段7號", "信義區", "110", AddressType.WORK);
            
            customer.addAddress(homeAddress);
            customer.addAddress(workAddress);

            // When
            Customer savedCustomer = customerJpaAdapter.save(customer);
            entityManager.flush();
            entityManager.clear();

            Optional<Customer> retrievedCustomer = customerJpaAdapter.findById(savedCustomer.getCustomerId());

            // Then
            assertTrue(retrievedCustomer.isPresent());
            Customer found = retrievedCustomer.get();
            assertEquals(2, found.getAddresses().size());
            
            // Check primary address
            Address primaryAddress = found.getPrimaryAddress();
            assertNotNull(primaryAddress);
            assertEquals("忠孝東路一段1號", primaryAddress.getStreet());
            assertTrue(primaryAddress.isPrimary());
            
            // Check non-primary address
            Address nonPrimaryAddress = found.getAddresses().stream()
                .filter(addr -> !addr.isPrimary())
                .findFirst()
                .orElse(null);
            assertNotNull(nonPrimaryAddress);
            assertEquals("信義路五段7號", nonPrimaryAddress.getStreet());
            assertFalse(nonPrimaryAddress.isPrimary());
        }

        @Test
        @DisplayName("Should update address correctly")
        void shouldUpdateAddressCorrectly() {
            // Given
            Customer customer = Customer.create("Rex", "Chen", "rex.chen@example.com", "0912345678");
            Address address = Address.createTaipeiAddress("忠孝東路一段1號", "中正區", "100", AddressType.HOME);
            customer.addAddress(address);
            
            Customer savedCustomer = customerJpaAdapter.save(customer);
            entityManager.flush();

            // When
            Address addressToUpdate = savedCustomer.getAddresses().get(0);
            addressToUpdate.updateDetails("信義路五段7號", "台北市", "信義區", "110", "台灣");
            
            Customer updatedCustomer = customerJpaAdapter.save(savedCustomer);
            entityManager.flush();
            entityManager.clear();

            Optional<Customer> retrievedCustomer = customerJpaAdapter.findById(updatedCustomer.getCustomerId());

            // Then
            assertTrue(retrievedCustomer.isPresent());
            Customer found = retrievedCustomer.get();
            assertEquals(1, found.getAddresses().size());
            
            Address updatedAddress = found.getAddresses().get(0);
            assertEquals("信義路五段7號", updatedAddress.getStreet());
            assertEquals("信義區", updatedAddress.getDistrict());
            assertEquals("110", updatedAddress.getPostalCode());
        }

        @Test
        @DisplayName("Should remove address correctly")
        void shouldRemoveAddressCorrectly() {
            // Given
            Customer customer = Customer.create("Rex", "Chen", "rex.chen@example.com", "0912345678");
            
            Address homeAddress = Address.createTaipeiAddress("忠孝東路一段1號", "中正區", "100", AddressType.HOME);
            Address workAddress = Address.createTaipeiAddress("信義路五段7號", "信義區", "110", AddressType.WORK);
            
            customer.addAddress(homeAddress);
            customer.addAddress(workAddress);
            
            Customer savedCustomer = customerJpaAdapter.save(customer);
            entityManager.flush();

            // When
            String workAddressId = savedCustomer.getAddresses().stream()
                .filter(addr -> !addr.isPrimary())
                .findFirst()
                .map(Address::getAddressId)
                .orElse(null);
            
            assertNotNull(workAddressId);
            savedCustomer.removeAddress(workAddressId);
            
            Customer updatedCustomer = customerJpaAdapter.save(savedCustomer);
            entityManager.flush();
            entityManager.clear();

            Optional<Customer> retrievedCustomer = customerJpaAdapter.findById(updatedCustomer.getCustomerId());

            // Then
            assertTrue(retrievedCustomer.isPresent());
            Customer found = retrievedCustomer.get();
            assertEquals(1, found.getAddresses().size());
            assertEquals("忠孝東路一段1號", found.getAddresses().get(0).getStreet());
        }
    }

    @Nested
    @DisplayName("Customer Status Tests")
    class CustomerStatusTests {

        @Test
        @DisplayName("Should persist customer status changes")
        void shouldPersistCustomerStatusChanges() {
            // Given
            Customer customer = Customer.create("Rex", "Chen", "rex.chen@example.com", "0912345678");
            Customer savedCustomer = customerJpaAdapter.save(customer);
            entityManager.flush();

            // When - Deactivate
            savedCustomer.deactivate();
            customerJpaAdapter.save(savedCustomer);
            entityManager.flush();
            entityManager.clear();

            Optional<Customer> deactivatedCustomer = customerJpaAdapter.findById(savedCustomer.getCustomerId());

            // Then
            assertTrue(deactivatedCustomer.isPresent());
            assertEquals(CustomerStatus.INACTIVE, deactivatedCustomer.get().getStatus());
            assertFalse(deactivatedCustomer.get().isActive());

            // When - Reactivate
            Customer customerToReactivate = deactivatedCustomer.get();
            customerToReactivate.activate();
            customerJpaAdapter.save(customerToReactivate);
            entityManager.flush();
            entityManager.clear();

            Optional<Customer> reactivatedCustomer = customerJpaAdapter.findById(savedCustomer.getCustomerId());

            // Then
            assertTrue(reactivatedCustomer.isPresent());
            assertEquals(CustomerStatus.ACTIVE, reactivatedCustomer.get().getStatus());
            assertTrue(reactivatedCustomer.get().isActive());
        }

        @Test
        @DisplayName("Should persist login timestamp")
        void shouldPersistLoginTimestamp() {
            // Given
            Customer customer = Customer.create("Rex", "Chen", "rex.chen@example.com", "0912345678");
            Customer savedCustomer = customerJpaAdapter.save(customer);
            entityManager.flush();
            assertNull(savedCustomer.getLastLoginDate());

            // When
            savedCustomer.recordLogin();
            customerJpaAdapter.save(savedCustomer);
            entityManager.flush();
            entityManager.clear();

            Optional<Customer> retrievedCustomer = customerJpaAdapter.findById(savedCustomer.getCustomerId());

            // Then
            assertTrue(retrievedCustomer.isPresent());
            assertNotNull(retrievedCustomer.get().getLastLoginDate());
        }
    }
}