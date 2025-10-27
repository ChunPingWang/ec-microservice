package com.ecommerce.customer.application.mapper;

import com.ecommerce.customer.application.dto.CustomerDto;
import com.ecommerce.customer.domain.model.Customer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between Customer domain objects and DTOs
 */
@Component
public class CustomerMapper {
    
    private final AddressMapper addressMapper;
    
    public CustomerMapper(AddressMapper addressMapper) {
        this.addressMapper = addressMapper;
    }
    
    /**
     * Convert Customer domain object to DTO
     */
    public CustomerDto toDto(Customer customer) {
        if (customer == null) {
            return null;
        }
        
        CustomerDto dto = new CustomerDto();
        dto.setCustomerId(customer.getCustomerId());
        dto.setFirstName(customer.getFirstName());
        dto.setLastName(customer.getLastName());
        dto.setEmail(customer.getEmail());
        dto.setPhoneNumber(customer.getPhoneNumber());
        dto.setStatus(customer.getStatus());
        dto.setRegistrationDate(customer.getRegistrationDate());
        dto.setLastLoginDate(customer.getLastLoginDate());
        dto.setCreatedAt(customer.getCreatedAt());
        dto.setUpdatedAt(customer.getUpdatedAt());
        
        // Map addresses
        if (customer.getAddresses() != null) {
            dto.setAddresses(customer.getAddresses().stream()
                .map(addressMapper::toDto)
                .collect(Collectors.toList()));
        }
        
        return dto;
    }
    
    /**
     * Convert list of Customer domain objects to DTOs
     */
    public List<CustomerDto> toDtoList(List<Customer> customers) {
        if (customers == null) {
            return null;
        }
        
        return customers.stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }
}