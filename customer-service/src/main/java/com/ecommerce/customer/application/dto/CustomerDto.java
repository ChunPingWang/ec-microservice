package com.ecommerce.customer.application.dto;

import com.ecommerce.common.dto.BaseDto;
import com.ecommerce.customer.domain.model.CustomerStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Customer data transfer object
 */
public class CustomerDto extends BaseDto {
    
    private String customerId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private CustomerStatus status;
    private List<AddressDto> addresses;
    private LocalDateTime registrationDate;
    private LocalDateTime lastLoginDate;
    
    // Constructors
    public CustomerDto() {}
    
    public CustomerDto(String customerId, String firstName, String lastName, 
                      String email, String phoneNumber, CustomerStatus status,
                      List<AddressDto> addresses, LocalDateTime registrationDate,
                      LocalDateTime lastLoginDate) {
        this.customerId = customerId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.status = status;
        this.addresses = addresses;
        this.registrationDate = registrationDate;
        this.lastLoginDate = lastLoginDate;
    }
    
    // Business methods
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    public boolean isActive() {
        return CustomerStatus.ACTIVE.equals(status);
    }
    
    public AddressDto getPrimaryAddress() {
        if (addresses == null) {
            return null;
        }
        return addresses.stream()
            .filter(AddressDto::isPrimary)
            .findFirst()
            .orElse(null);
    }
    
    // Getters and Setters
    public String getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public CustomerStatus getStatus() {
        return status;
    }
    
    public void setStatus(CustomerStatus status) {
        this.status = status;
    }
    
    public List<AddressDto> getAddresses() {
        return addresses;
    }
    
    public void setAddresses(List<AddressDto> addresses) {
        this.addresses = addresses;
    }
    
    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }
    
    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }
    
    public LocalDateTime getLastLoginDate() {
        return lastLoginDate;
    }
    
    public void setLastLoginDate(LocalDateTime lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }
}