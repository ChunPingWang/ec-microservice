package com.ecommerce.order.infrastructure.adapter.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 購物車 JPA 實體
 */
@Entity
@Table(name = "carts", indexes = {
    @Index(name = "idx_cart_customer_id", columnList = "customer_id", unique = true),
    @Index(name = "idx_cart_expiry_date", columnList = "expiry_date"),
    @Index(name = "idx_cart_last_updated", columnList = "last_updated")
})
public class CartJpaEntity {
    
    @Id
    @Column(name = "cart_id", length = 50)
    private String cartId;
    
    @Column(name = "customer_id", nullable = false, length = 50, unique = true)
    private String customerId;
    
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;
    
    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;
    
    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<CartItemJpaEntity> cartItems = new ArrayList<>();
    
    // Constructors
    public CartJpaEntity() {}
    
    // Getters and Setters
    public String getCartId() { return cartId; }
    public void setCartId(String cartId) { this.cartId = cartId; }
    
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
    
    public LocalDateTime getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public List<CartItemJpaEntity> getCartItems() { return cartItems; }
    public void setCartItems(List<CartItemJpaEntity> cartItems) { this.cartItems = cartItems; }
    
    // Helper methods
    public void addCartItem(CartItemJpaEntity cartItem) {
        cartItems.add(cartItem);
        cartItem.setCart(this);
    }
    
    public void removeCartItem(CartItemJpaEntity cartItem) {
        cartItems.remove(cartItem);
        cartItem.setCart(null);
    }
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }
    
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}