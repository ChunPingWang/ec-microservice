package com.ecommerce.customer.application.dto;

import com.ecommerce.customer.domain.model.AddressType;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating an address
 */
public class UpdateAddressRequest {
    
    @Size(max = 200, message = "Street address cannot exceed 200 characters")
    private String street;
    
    @Size(max = 50, message = "City name cannot exceed 50 characters")
    private String city;
    
    @Size(max = 50, message = "District name cannot exceed 50 characters")
    private String district;
    
    private String postalCode;
    
    @Size(max = 50, message = "Country name cannot exceed 50 characters")
    private String country;
    
    private AddressType type;
    
    // Constructors
    public UpdateAddressRequest() {}
    
    public UpdateAddressRequest(String street, String city, String district, 
                              String postalCode, String country, AddressType type) {
        this.street = street;
        this.city = city;
        this.district = district;
        this.postalCode = postalCode;
        this.country = country;
        this.type = type;
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
    
    @Override
    public String toString() {
        return "UpdateAddressRequest{" +
                "street='" + street + '\'' +
                ", city='" + city + '\'' +
                ", district='" + district + '\'' +
                ", postalCode='" + postalCode + '\'' +
                ", country='" + country + '\'' +
                ", type=" + type +
                '}';
    }
}