package com.ecommerce.customer.domain.model;

import com.ecommerce.common.exception.ValidationException;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Address value object with Taiwan/Taipei address format support
 * Immutable value object following DDD principles
 */
public class Address {
    
    private String addressId;
    private String street;
    private String city;
    private String district;
    private String postalCode;
    private String country;
    private AddressType type;
    private boolean isPrimary;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Taiwan postal code pattern (5 digits)
    private static final Pattern TAIWAN_POSTAL_CODE_PATTERN = Pattern.compile("^[0-9]{5}$");
    
    // Taipei districts for validation
    private static final String[] TAIPEI_DISTRICTS = {
        "中正區", "大同區", "中山區", "松山區", "大安區", "萬華區",
        "信義區", "士林區", "北投區", "內湖區", "南港區", "文山區"
    };
    
    // Private constructor for JPA
    protected Address() {}
    
    // Factory method for creating new addresses
    public static Address create(String street, String city, String district, 
                               String postalCode, String country, AddressType type) {
        Address address = new Address();
        address.addressId = generateAddressId();
        address.setStreet(street);
        address.setCity(city);
        address.setDistrict(district);
        address.setPostalCode(postalCode);
        address.setCountry(country);
        address.type = type != null ? type : AddressType.HOME;
        address.isPrimary = false;
        address.createdAt = LocalDateTime.now();
        address.updatedAt = LocalDateTime.now();
        return address;
    }
    
    // Factory method for Taipei addresses
    public static Address createTaipeiAddress(String street, String district, 
                                            String postalCode, AddressType type) {
        return create(street, "台北市", district, postalCode, "台灣", type);
    }
    
    // Business methods
    public void updateDetails(String street, String city, String district, 
                            String postalCode, String country) {
        setStreet(street);
        setCity(city);
        setDistrict(district);
        setPostalCode(postalCode);
        setCountry(country);
        this.updatedAt = LocalDateTime.now();
    }
    
    public void setPrimary(boolean primary) {
        this.isPrimary = primary;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getFormattedAddress() {
        if (isTaiwanAddress()) {
            return String.format("%s%s%s%s %s", 
                country, city, district, postalCode, street);
        } else {
            return String.format("%s, %s, %s %s, %s", 
                street, district, city, postalCode, country);
        }
    }
    
    public boolean isTaiwanAddress() {
        return "台灣".equals(country) || "Taiwan".equalsIgnoreCase(country);
    }
    
    public boolean isTaipeiAddress() {
        return isTaiwanAddress() && 
               ("台北市".equals(city) || "Taipei".equalsIgnoreCase(city));
    }
    
    public void validate() {
        validateStreet();
        validateCity();
        validateDistrict();
        validatePostalCode();
        validateCountry();
        
        // Additional validation for Taiwan addresses
        if (isTaiwanAddress()) {
            validateTaiwanSpecificRules();
        }
        
        // Additional validation for Taipei addresses
        if (isTaipeiAddress()) {
            validateTaipeiSpecificRules();
        }
    }
    
    // Private helper methods
    private static String generateAddressId() {
        return "ADDR-" + System.currentTimeMillis();
    }
    
    private void validateStreet() {
        if (street == null || street.trim().isEmpty()) {
            throw new ValidationException("Street address is required");
        }
        if (street.length() > 200) {
            throw new ValidationException("Street address cannot exceed 200 characters");
        }
    }
    
    private void validateCity() {
        if (city == null || city.trim().isEmpty()) {
            throw new ValidationException("City is required");
        }
        if (city.length() > 50) {
            throw new ValidationException("City name cannot exceed 50 characters");
        }
    }
    
    private void validateDistrict() {
        if (district == null || district.trim().isEmpty()) {
            throw new ValidationException("District is required");
        }
        if (district.length() > 50) {
            throw new ValidationException("District name cannot exceed 50 characters");
        }
    }
    
    private void validatePostalCode() {
        if (postalCode == null || postalCode.trim().isEmpty()) {
            throw new ValidationException("Postal code is required");
        }
    }
    
    private void validateCountry() {
        if (country == null || country.trim().isEmpty()) {
            throw new ValidationException("Country is required");
        }
        if (country.length() > 50) {
            throw new ValidationException("Country name cannot exceed 50 characters");
        }
    }
    
    private void validateTaiwanSpecificRules() {
        // Validate Taiwan postal code format
        if (!TAIWAN_POSTAL_CODE_PATTERN.matcher(postalCode).matches()) {
            throw new ValidationException("Invalid Taiwan postal code format. Must be 5 digits.");
        }
    }
    
    private void validateTaipeiSpecificRules() {
        // Validate Taipei district
        boolean isValidDistrict = false;
        for (String validDistrict : TAIPEI_DISTRICTS) {
            if (validDistrict.equals(district)) {
                isValidDistrict = true;
                break;
            }
        }
        
        if (!isValidDistrict) {
            throw new ValidationException("Invalid Taipei district: " + district);
        }
        
        // Validate Taipei postal code ranges
        validateTaipeiPostalCode();
    }
    
    private void validateTaipeiPostalCode() {
        try {
            int code = Integer.parseInt(postalCode);
            
            // Taipei postal code ranges: 100-116
            if (code < 100 || code > 116) {
                throw new ValidationException("Invalid Taipei postal code: " + postalCode);
            }
            
            // Validate specific district postal codes
            validateDistrictPostalCode(code);
            
        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid postal code format: " + postalCode);
        }
    }
    
    private void validateDistrictPostalCode(int code) {
        // Map districts to their postal codes
        switch (district) {
            case "中正區":
                if (code != 100) throw new ValidationException("中正區 postal code should be 100");
                break;
            case "大同區":
                if (code != 103) throw new ValidationException("大同區 postal code should be 103");
                break;
            case "中山區":
                if (code != 104) throw new ValidationException("中山區 postal code should be 104");
                break;
            case "松山區":
                if (code != 105) throw new ValidationException("松山區 postal code should be 105");
                break;
            case "大安區":
                if (code != 106) throw new ValidationException("大安區 postal code should be 106");
                break;
            case "萬華區":
                if (code != 108) throw new ValidationException("萬華區 postal code should be 108");
                break;
            case "信義區":
                if (code != 110) throw new ValidationException("信義區 postal code should be 110");
                break;
            case "士林區":
                if (code != 111) throw new ValidationException("士林區 postal code should be 111");
                break;
            case "北投區":
                if (code != 112) throw new ValidationException("北投區 postal code should be 112");
                break;
            case "內湖區":
                if (code != 114) throw new ValidationException("內湖區 postal code should be 114");
                break;
            case "南港區":
                if (code != 115) throw new ValidationException("南港區 postal code should be 115");
                break;
            case "文山區":
                if (code != 116) throw new ValidationException("文山區 postal code should be 116");
                break;
        }
    }
    
    // Setters with validation
    private void setStreet(String street) {
        this.street = street != null ? street.trim() : null;
    }
    
    private void setCity(String city) {
        this.city = city != null ? city.trim() : null;
    }
    
    private void setDistrict(String district) {
        this.district = district != null ? district.trim() : null;
    }
    
    private void setPostalCode(String postalCode) {
        this.postalCode = postalCode != null ? postalCode.trim() : null;
    }
    
    private void setCountry(String country) {
        this.country = country != null ? country.trim() : null;
    }
    
    // Getters
    public String getAddressId() { return addressId; }
    public String getStreet() { return street; }
    public String getCity() { return city; }
    public String getDistrict() { return district; }
    public String getPostalCode() { return postalCode; }
    public String getCountry() { return country; }
    public AddressType getType() { return type; }
    public boolean isPrimary() { return isPrimary; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return Objects.equals(addressId, address.addressId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(addressId);
    }
    
    @Override
    public String toString() {
        return "Address{" +
                "addressId='" + addressId + '\'' +
                ", formattedAddress='" + getFormattedAddress() + '\'' +
                ", type=" + type +
                ", isPrimary=" + isPrimary +
                '}';
    }
}