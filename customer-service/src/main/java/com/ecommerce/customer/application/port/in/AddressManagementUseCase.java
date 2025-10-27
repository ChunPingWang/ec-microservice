package com.ecommerce.customer.application.port.in;

import com.ecommerce.customer.application.dto.AddressDto;
import com.ecommerce.customer.application.dto.CreateAddressRequest;
import com.ecommerce.customer.application.dto.UpdateAddressRequest;

import java.util.List;

/**
 * Input port for address management use cases
 * Defines the contract for address-related operations
 */
public interface AddressManagementUseCase {
    
    /**
     * Add address to customer
     * @param customerId the customer ID
     * @param request address details
     * @return created address DTO
     */
    AddressDto addAddress(String customerId, CreateAddressRequest request);
    
    /**
     * Update customer address
     * @param customerId the customer ID
     * @param addressId the address ID
     * @param request update details
     * @return updated address DTO
     */
    AddressDto updateAddress(String customerId, String addressId, UpdateAddressRequest request);
    
    /**
     * Remove address from customer
     * @param customerId the customer ID
     * @param addressId the address ID
     */
    void removeAddress(String customerId, String addressId);
    
    /**
     * Set primary address for customer
     * @param customerId the customer ID
     * @param addressId the address ID
     */
    void setPrimaryAddress(String customerId, String addressId);
    
    /**
     * Get all addresses for customer
     * @param customerId the customer ID
     * @return list of customer addresses
     */
    List<AddressDto> getCustomerAddresses(String customerId);
    
    /**
     * Get primary address for customer
     * @param customerId the customer ID
     * @return primary address DTO or null if none
     */
    AddressDto getPrimaryAddress(String customerId);
    
    /**
     * Validate Taipei address
     * @param request address details to validate
     * @return true if valid Taipei address
     */
    boolean validateTaipeiAddress(CreateAddressRequest request);
}