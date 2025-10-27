package com.ecommerce.customer.infrastructure.adapter.persistence.mapper;

import com.ecommerce.customer.domain.model.Address;
import com.ecommerce.customer.domain.model.Customer;
import com.ecommerce.customer.infrastructure.adapter.persistence.entity.AddressJpaEntity;
import com.ecommerce.customer.infrastructure.adapter.persistence.entity.CustomerJpaEntity;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper between Customer domain objects and JPA entities
 * Handles conversion between domain and persistence layers
 */
@Component
public class CustomerJpaMapper {
    
    /**
     * Convert Customer domain object to JPA entity
     */
    public CustomerJpaEntity toEntity(Customer customer) {
        if (customer == null) {
            return null;
        }
        
        CustomerJpaEntity entity = new CustomerJpaEntity();
        entity.setCustomerId(customer.getCustomerId());
        entity.setFirstName(customer.getFirstName());
        entity.setLastName(customer.getLastName());
        entity.setEmail(customer.getEmail());
        entity.setPhoneNumber(customer.getPhoneNumber());
        entity.setStatus(customer.getStatus());
        entity.setRegistrationDate(customer.getRegistrationDate());
        entity.setLastLoginDate(customer.getLastLoginDate());
        entity.setCreatedAt(customer.getCreatedAt());
        entity.setUpdatedAt(customer.getUpdatedAt());
        
        // Map addresses
        if (customer.getAddresses() != null) {
            List<AddressJpaEntity> addressEntities = customer.getAddresses().stream()
                .map(address -> toAddressEntity(address, entity))
                .collect(Collectors.toList());
            entity.setAddresses(addressEntities);
        }
        
        return entity;
    }
    
    /**
     * Convert JPA entity to Customer domain object
     */
    public Customer toDomain(CustomerJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        
        // Create customer using factory method
        Customer customer = Customer.create(
            entity.getFirstName(),
            entity.getLastName(),
            entity.getEmail(),
            entity.getPhoneNumber()
        );
        
        // Set additional fields using reflection (since they're private)
        setPrivateField(customer, "customerId", entity.getCustomerId());
        setPrivateField(customer, "status", entity.getStatus());
        setPrivateField(customer, "registrationDate", entity.getRegistrationDate());
        setPrivateField(customer, "lastLoginDate", entity.getLastLoginDate());
        setPrivateField(customer, "createdAt", entity.getCreatedAt());
        setPrivateField(customer, "updatedAt", entity.getUpdatedAt());
        
        // Map addresses
        if (entity.getAddresses() != null) {
            List<Address> addresses = entity.getAddresses().stream()
                .map(this::toAddressDomain)
                .collect(Collectors.toList());
            
            // Add addresses to customer
            for (Address address : addresses) {
                customer.addAddress(address);
            }
        }
        
        return customer;
    }
    
    /**
     * Convert Address domain object to JPA entity
     */
    private AddressJpaEntity toAddressEntity(Address address, CustomerJpaEntity customer) {
        if (address == null) {
            return null;
        }
        
        AddressJpaEntity entity = new AddressJpaEntity();
        entity.setAddressId(address.getAddressId());
        entity.setStreet(address.getStreet());
        entity.setCity(address.getCity());
        entity.setDistrict(address.getDistrict());
        entity.setPostalCode(address.getPostalCode());
        entity.setCountry(address.getCountry());
        entity.setType(address.getType());
        entity.setPrimary(address.isPrimary());
        entity.setCreatedAt(address.getCreatedAt());
        entity.setUpdatedAt(address.getUpdatedAt());
        entity.setCustomer(customer);
        
        return entity;
    }
    
    /**
     * Convert JPA entity to Address domain object
     */
    private Address toAddressDomain(AddressJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        
        // Create address using factory method
        Address address = Address.create(
            entity.getStreet(),
            entity.getCity(),
            entity.getDistrict(),
            entity.getPostalCode(),
            entity.getCountry(),
            entity.getType()
        );
        
        // Set additional fields using reflection
        setPrivateField(address, "addressId", entity.getAddressId());
        setPrivateField(address, "isPrimary", entity.isPrimary());
        setPrivateField(address, "createdAt", entity.getCreatedAt());
        setPrivateField(address, "updatedAt", entity.getUpdatedAt());
        
        return address;
    }
    
    /**
     * Helper method to set private fields using reflection
     */
    private void setPrivateField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // Try parent class if field not found in current class
            try {
                Field field = target.getClass().getSuperclass().getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(target, value);
            } catch (NoSuchFieldException | IllegalAccessException ex) {
                throw new RuntimeException("Failed to set field: " + fieldName, ex);
            }
        }
    }
}