package com.ecommerce.customer.application.mapper;

import com.ecommerce.customer.application.dto.AddressDto;
import com.ecommerce.customer.application.dto.CreateAddressRequest;
import com.ecommerce.customer.application.dto.UpdateAddressRequest;
import com.ecommerce.customer.domain.model.Address;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between Address domain objects and DTOs
 */
@Component
public class AddressMapper {
    
    /**
     * Convert Address domain object to DTO
     */
    public AddressDto toDto(Address address) {
        if (address == null) {
            return null;
        }
        
        AddressDto dto = new AddressDto();
        dto.setAddressId(address.getAddressId());
        dto.setStreet(address.getStreet());
        dto.setCity(address.getCity());
        dto.setDistrict(address.getDistrict());
        dto.setPostalCode(address.getPostalCode());
        dto.setCountry(address.getCountry());
        dto.setType(address.getType());
        dto.setPrimary(address.isPrimary());
        dto.setFormattedAddress(address.getFormattedAddress());
        dto.setCreatedAt(address.getCreatedAt());
        dto.setUpdatedAt(address.getUpdatedAt());
        
        return dto;
    }
    
    /**
     * Convert CreateAddressRequest to Address domain object
     */
    public Address fromCreateRequest(CreateAddressRequest request) {
        if (request == null) {
            return null;
        }
        
        Address address = Address.create(
            request.getStreet(),
            request.getCity(),
            request.getDistrict(),
            request.getPostalCode(),
            request.getCountry(),
            request.getType()
        );
        
        address.setPrimary(request.isPrimary());
        
        return address;
    }
    
    /**
     * Convert UpdateAddressRequest to Address domain object
     */
    public Address fromUpdateRequest(UpdateAddressRequest request) {
        if (request == null) {
            return null;
        }
        
        return Address.create(
            request.getStreet(),
            request.getCity(),
            request.getDistrict(),
            request.getPostalCode(),
            request.getCountry(),
            request.getType()
        );
    }
    
    /**
     * Convert list of Address domain objects to DTOs
     */
    public List<AddressDto> toDtoList(List<Address> addresses) {
        if (addresses == null) {
            return null;
        }
        
        return addresses.stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }
}