package com.ecommerce.customer.domain.model;

import com.ecommerce.common.architecture.BaseEntity;
import com.ecommerce.common.exception.ValidationException;
import com.ecommerce.common.validation.EmailValidator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Customer domain entity following DDD principles
 * Encapsulates customer business rules and validation logic
 */
public class Customer extends BaseEntity {
    
    private String customerId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private CustomerStatus status;
    private List<Address> addresses;
    private LocalDateTime registrationDate;
    private LocalDateTime lastLoginDate;
    
    // Private constructor for JPA
    protected Customer() {
        this.addresses = new ArrayList<>();
    }
    
    // Factory method for creating new customers
    public static Customer create(String firstName, String lastName, String email, String phoneNumber) {
        Customer customer = new Customer();
        customer.customerId = generateCustomerId();
        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        customer.setEmail(email);
        customer.setPhoneNumber(phoneNumber);
        customer.status = CustomerStatus.ACTIVE;
        customer.registrationDate = LocalDateTime.now();
        customer.addresses = new ArrayList<>();
        return customer;
    }
    
    // Business methods
    public void updatePersonalInfo(String firstName, String lastName, String phoneNumber) {
        setFirstName(firstName);
        setLastName(lastName);
        setPhoneNumber(phoneNumber);
        this.updatedAt = LocalDateTime.now();
    }
    
    public void addAddress(Address address) {
        validateAddress(address);
        
        // If this is the first address or marked as primary, make it primary
        if (addresses.isEmpty() || address.isPrimary()) {
            // Remove primary flag from existing addresses
            addresses.forEach(addr -> addr.setPrimary(false));
            address.setPrimary(true);
        }
        
        addresses.add(address);
        this.updatedAt = LocalDateTime.now();
    }
    
    public void updateAddress(String addressId, Address updatedAddress) {
        Address existingAddress = findAddressById(addressId);
        if (existingAddress == null) {
            throw new ValidationException("Address not found: " + addressId);
        }
        
        validateAddress(updatedAddress);
        
        // Update address details
        existingAddress.updateDetails(
            updatedAddress.getStreet(),
            updatedAddress.getCity(),
            updatedAddress.getDistrict(),
            updatedAddress.getPostalCode(),
            updatedAddress.getCountry()
        );
        
        this.updatedAt = LocalDateTime.now();
    }
    
    public void removeAddress(String addressId) {
        Address addressToRemove = findAddressById(addressId);
        if (addressToRemove == null) {
            throw new ValidationException("Address not found: " + addressId);
        }
        
        if (addressToRemove.isPrimary() && addresses.size() > 1) {
            throw new ValidationException("Cannot remove primary address when other addresses exist");
        }
        
        addresses.remove(addressToRemove);
        this.updatedAt = LocalDateTime.now();
    }
    
    public void setPrimaryAddress(String addressId) {
        Address newPrimaryAddress = findAddressById(addressId);
        if (newPrimaryAddress == null) {
            throw new ValidationException("Address not found: " + addressId);
        }
        
        // Remove primary flag from all addresses
        addresses.forEach(addr -> addr.setPrimary(false));
        
        // Set new primary address
        newPrimaryAddress.setPrimary(true);
        this.updatedAt = LocalDateTime.now();
    }
    
    public Address getPrimaryAddress() {
        return addresses.stream()
            .filter(Address::isPrimary)
            .findFirst()
            .orElse(null);
    }
    
    public void deactivate() {
        this.status = CustomerStatus.INACTIVE;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void activate() {
        this.status = CustomerStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void recordLogin() {
        this.lastLoginDate = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public boolean isActive() {
        return CustomerStatus.ACTIVE.equals(this.status);
    }
    
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    // Private helper methods
    private static String generateCustomerId() {
        return "CUST-" + System.currentTimeMillis();
    }
    
    private void validateAddress(Address address) {
        if (address == null) {
            throw new ValidationException("Address cannot be null");
        }
        address.validate();
    }
    
    private Address findAddressById(String addressId) {
        return addresses.stream()
            .filter(addr -> Objects.equals(addr.getAddressId(), addressId))
            .findFirst()
            .orElse(null);
    }
    
    // Validation methods
    private void setFirstName(String firstName) {
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new ValidationException("First name is required");
        }
        if (firstName.length() > 50) {
            throw new ValidationException("First name cannot exceed 50 characters");
        }
        this.firstName = firstName.trim();
    }
    
    private void setLastName(String lastName) {
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new ValidationException("Last name is required");
        }
        if (lastName.length() > 50) {
            throw new ValidationException("Last name cannot exceed 50 characters");
        }
        this.lastName = lastName.trim();
    }
    
    private void setEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("Email is required");
        }
        if (!EmailValidator.isValid(email)) {
            throw new ValidationException("Invalid email format");
        }
        this.email = email.toLowerCase().trim();
    }
    
    private void setPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new ValidationException("Phone number is required");
        }
        
        // Basic phone number validation (Taiwan format)
        String cleanPhone = phoneNumber.replaceAll("[^0-9+]", "");
        if (!cleanPhone.matches("^(\\+886|0)[0-9]{8,9}$")) {
            throw new ValidationException("Invalid phone number format");
        }
        this.phoneNumber = cleanPhone;
    }
    
    // Getters
    public String getCustomerId() { return customerId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }
    public CustomerStatus getStatus() { return status; }
    public List<Address> getAddresses() { return new ArrayList<>(addresses); }
    public LocalDateTime getRegistrationDate() { return registrationDate; }
    public LocalDateTime getLastLoginDate() { return lastLoginDate; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Customer customer = (Customer) o;
        return Objects.equals(customerId, customer.customerId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(customerId);
    }
    
    @Override
    public String toString() {
        return "Customer{" +
                "customerId='" + customerId + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", status=" + status +
                '}';
    }
}