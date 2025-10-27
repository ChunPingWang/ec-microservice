package com.ecommerce.customer.application.dto;

import com.ecommerce.customer.domain.model.AddressType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a new address
 */
public class CreateAddressRequest {
    
    @NotBlank(message = "Street address is required")
    @Size(max = 200, message = "Street address cannot exceed 200 characters")
    private String street;
    
    @NotBlank(message = "City is required")
    @Size(max = 50, message = "City name cannot exceed 50 characters")
    private String city;
    
    @NotBlank(message = "District is required")
    @Size(max = 50, message = "District name cannot exceed 50 characters")
    private String district;
    
    @NotBlank(message = "Postal code is required")
    private String postalCode;
    
    @NotBlank(message = "Country is required")
    @Size(max = 50, message = "Country name cannot exceed 50 characters")
    private String country;
    
    private AddressType type = AddressType.HOME;
    
    private boolean isPrimary = false;
    
    // Constructors
    public CreateAddressRequest() {}
    
    public CreateAddressRequest(String street, String city, String district, 
                              String postalCode, String country, AddressType type) {
        this.street = street;
        this.city = city;
        this.district = district;
        this.postalCode = postalCode;
        this.country = country;
        this.type = type;
    }
    
    // Factory method for Taipei addresses
    public static CreateAddressRequest forTaipei(String street, String district, 
                                               String postalCode, AddressType type) {
        return new CreateAddressRequest(street, "台北市", district, postalCode, "台灣", type);
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
    
    @Override
    public String toString() {
        return "CreateAddressRequest{" +
                "street='" + street + '\'' +
                ", city='" + city + '\'' +
                ", district='" + district + '\'' +
                ", postalCode='" + postalCode + '\'' +
                ", country='" + country + '\'' +
                ", type=" + type +
                ", isPrimary=" + isPrimary +
                '}';
    }
}