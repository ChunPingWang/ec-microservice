package com.ecommerce.customer.application.dto;

import com.ecommerce.common.dto.BaseDto;
import com.ecommerce.customer.domain.model.AddressType;

import java.time.LocalDateTime;

/**
 * Address data transfer object
 */
public class AddressDto extends BaseDto {
    
    private String addressId;
    private String street;
    private String city;
    private String district;
    private String postalCode;
    private String country;
    private AddressType type;
    private boolean isPrimary;
    private String formattedAddress;
    
    // Constructors
    public AddressDto() {}
    
    public AddressDto(String addressId, String street, String city, String district,
                     String postalCode, String country, AddressType type, 
                     boolean isPrimary, String formattedAddress) {
        this.addressId = addressId;
        this.street = street;
        this.city = city;
        this.district = district;
        this.postalCode = postalCode;
        this.country = country;
        this.type = type;
        this.isPrimary = isPrimary;
        this.formattedAddress = formattedAddress;
    }
    
    // Business methods
    public boolean isTaiwanAddress() {
        return "台灣".equals(country) || "Taiwan".equalsIgnoreCase(country);
    }
    
    public boolean isTaipeiAddress() {
        return isTaiwanAddress() && 
               ("台北市".equals(city) || "Taipei".equalsIgnoreCase(city));
    }
    
    // Getters and Setters
    public String getAddressId() {
        return addressId;
    }
    
    public void setAddressId(String addressId) {
        this.addressId = addressId;
    }
    
    public String getStreet() {
        return street;
    }
    
    public void setStreet(String street) {
        this.street = street;
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public String getDistrict() {
        return district;
    }
    
    public void setDistrict(String district) {
        this.district = district;
    }
    
    public String getPostalCode() {
        return postalCode;
    }
    
    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
    
    public String getCountry() {
        return country;
    }
    
    public void setCountry(String country) {
        this.country = country;
    }
    
    public AddressType getType() {
        return type;
    }
    
    public void setType(AddressType type) {
        this.type = type;
    }
    
    public boolean isPrimary() {
        return isPrimary;
    }
    
    public void setPrimary(boolean primary) {
        isPrimary = primary;
    }
    
    public String getFormattedAddress() {
        return formattedAddress;
    }
    
    public void setFormattedAddress(String formattedAddress) {
        this.formattedAddress = formattedAddress;
    }
}