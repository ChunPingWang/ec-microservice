package com.ecommerce.customer.infrastructure.adapter.external;

import com.ecommerce.common.architecture.Adapter;
import com.ecommerce.customer.domain.model.Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * External adapter for address validation
 * Provides address validation services, particularly for Taiwan/Taipei addresses
 */
@Adapter
@Component
public class AddressValidationAdapter {
    
    private static final Logger logger = LoggerFactory.getLogger(AddressValidationAdapter.class);
    
    // Taiwan postal code pattern (5 digits)
    private static final Pattern TAIWAN_POSTAL_CODE_PATTERN = Pattern.compile("^[0-9]{5}$");
    
    // Taipei districts for validation
    private static final List<String> TAIPEI_DISTRICTS = Arrays.asList(
        "中正區", "大同區", "中山區", "松山區", "大安區", "萬華區",
        "信義區", "士林區", "北投區", "內湖區", "南港區", "文山區"
    );
    
    /**
     * Validate Taiwan address format and postal code
     */
    public boolean validateTaiwanAddress(Address address) {
        try {
            if (!address.isTaiwanAddress()) {
                return false;
            }
            
            // Validate postal code format
            if (!TAIWAN_POSTAL_CODE_PATTERN.matcher(address.getPostalCode()).matches()) {
                logger.warn("Invalid Taiwan postal code format: {}", address.getPostalCode());
                return false;
            }
            
            // Additional validation for Taipei addresses
            if (address.isTaipeiAddress()) {
                return validateTaipeiAddress(address);
            }
            
            logger.info("Taiwan address validation successful: {}", address.getFormattedAddress());
            return true;
            
        } catch (Exception e) {
            logger.error("Error validating Taiwan address: {}", address.getFormattedAddress(), e);
            return false;
        }
    }
    
    /**
     * Validate Taipei address specifically
     */
    public boolean validateTaipeiAddress(Address address) {
        try {
            if (!address.isTaipeiAddress()) {
                logger.warn("Address is not a Taipei address: {}", address.getFormattedAddress());
                return false;
            }
            
            // Validate district
            if (!TAIPEI_DISTRICTS.contains(address.getDistrict())) {
                logger.warn("Invalid Taipei district: {}", address.getDistrict());
                return false;
            }
            
            // Validate postal code ranges for Taipei (100-116)
            try {
                int postalCode = Integer.parseInt(address.getPostalCode());
                if (postalCode < 100 || postalCode > 116) {
                    logger.warn("Invalid Taipei postal code range: {}", address.getPostalCode());
                    return false;
                }
                
                // Validate specific district postal codes
                if (!validateDistrictPostalCode(address.getDistrict(), postalCode)) {
                    logger.warn("Postal code {} does not match district {}", 
                               address.getPostalCode(), address.getDistrict());
                    return false;
                }
                
            } catch (NumberFormatException e) {
                logger.warn("Invalid postal code format: {}", address.getPostalCode());
                return false;
            }
            
            // Validate street address is not empty
            if (address.getStreet() == null || address.getStreet().trim().isEmpty()) {
                logger.warn("Street address is required for Taipei addresses");
                return false;
            }
            
            logger.info("Taipei address validation successful: {}", address.getFormattedAddress());
            return true;
            
        } catch (Exception e) {
            logger.error("Error validating Taipei address: {}", address.getFormattedAddress(), e);
            return false;
        }
    }
    
    /**
     * Check if address is suitable for delivery
     */
    public boolean isDeliverable(Address address) {
        try {
            // Basic deliverability checks
            if (address == null) {
                return false;
            }
            
            // Must have all required fields
            if (isNullOrEmpty(address.getStreet()) ||
                isNullOrEmpty(address.getCity()) ||
                isNullOrEmpty(address.getDistrict()) ||
                isNullOrEmpty(address.getPostalCode()) ||
                isNullOrEmpty(address.getCountry())) {
                
                logger.warn("Address missing required fields for delivery: {}", 
                           address.getFormattedAddress());
                return false;
            }
            
            // For Taiwan addresses, use Taiwan-specific validation
            if (address.isTaiwanAddress()) {
                return validateTaiwanAddress(address);
            }
            
            // For other countries, basic validation
            logger.info("Address is deliverable: {}", address.getFormattedAddress());
            return true;
            
        } catch (Exception e) {
            logger.error("Error checking address deliverability: {}", 
                        address != null ? address.getFormattedAddress() : "null", e);
            return false;
        }
    }
    
    /**
     * Get delivery recommendations for invalid addresses
     */
    public String getDeliveryRecommendations(Address address) {
        if (address == null) {
            return "Address is required for delivery";
        }
        
        StringBuilder recommendations = new StringBuilder();
        
        if (isNullOrEmpty(address.getStreet())) {
            recommendations.append("Street address is required. ");
        }
        
        if (isNullOrEmpty(address.getCity())) {
            recommendations.append("City is required. ");
        }
        
        if (isNullOrEmpty(address.getDistrict())) {
            recommendations.append("District is required. ");
        }
        
        if (isNullOrEmpty(address.getPostalCode())) {
            recommendations.append("Postal code is required. ");
        }
        
        if (address.isTaipeiAddress() && !TAIPEI_DISTRICTS.contains(address.getDistrict())) {
            recommendations.append("Please use a valid Taipei district: ")
                          .append(String.join(", ", TAIPEI_DISTRICTS))
                          .append(". ");
        }
        
        if (address.isTaiwanAddress() && 
            !TAIWAN_POSTAL_CODE_PATTERN.matcher(address.getPostalCode()).matches()) {
            recommendations.append("Taiwan postal code must be 5 digits. ");
        }
        
        return recommendations.length() > 0 ? 
               recommendations.toString().trim() : 
               "Address appears to be valid for delivery";
    }
    
    // Private helper methods
    private boolean validateDistrictPostalCode(String district, int postalCode) {
        // Map districts to their postal codes
        switch (district) {
            case "中正區": return postalCode == 100;
            case "大同區": return postalCode == 103;
            case "中山區": return postalCode == 104;
            case "松山區": return postalCode == 105;
            case "大安區": return postalCode == 106;
            case "萬華區": return postalCode == 108;
            case "信義區": return postalCode == 110;
            case "士林區": return postalCode == 111;
            case "北投區": return postalCode == 112;
            case "內湖區": return postalCode == 114;
            case "南港區": return postalCode == 115;
            case "文山區": return postalCode == 116;
            default: return false;
        }
    }
    
    private boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}