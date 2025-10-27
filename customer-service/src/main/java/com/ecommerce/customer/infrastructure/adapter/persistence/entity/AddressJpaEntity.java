package com.ecommerce.customer.infrastructure.adapter.persistence.entity;

import com.ecommerce.customer.domain.model.AddressType;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity for Address
 * Maps address domain object to database table
 */
@Entity
@Table(name = "addresses")
public class AddressJpaEntity {
    
    @Id
    @Column(name = "address_id", length = 50)
    private String addressId;
    
    @Column(name = "street", nullable = false, length = 200)
    private String street;
    
    @Column(name = "city", nullable = false, length = 50)
    private String city;
    
    @Column(name = "district", nullable = false, length = 50)
    private String district;
    
    @Column(name = "postal_code", nullable = false, length = 10)
    private String postalCode;
    
    @Column(name = "country", nullable = false, length = 50)
    private String country;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private AddressType type;
    
    @Column(name = "is_primary", nullable = false)
    private boolean isPrimary;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerJpaEntity customer;
    
    // Constructors
    public AddressJpaEntity() {}
    
    public AddressJpaEntity(String addressId, String street, String city, String district,
                          String postalCode, String country, AddressType type, boolean isPrimary) {
        this.addressId = addressId;
        this.street = street;
        this.city = city;
        this.district = district;
        this.postalCode = postalCode;
        this.country = country;
        this.type = type;
        this.isPrimary = isPrimary;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Business methods
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
    
    // JPA lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
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
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public CustomerJpaEntity getCustomer() {
        return customer;
    }
    
    public void setCustomer(CustomerJpaEntity customer) {
        this.customer = customer;
    }
}