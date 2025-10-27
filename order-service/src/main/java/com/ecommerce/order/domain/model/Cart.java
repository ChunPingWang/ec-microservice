package com.ecommerce.order.domain.model;

import com.ecommerce.common.architecture.BaseEntity;
import com.ecommerce.common.exception.ValidationException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 購物車實體
 * 管理客戶的購物車狀態和商品項目
 */
public class Cart extends BaseEntity {
    
    private String cartId;
    private String customerId;
    private List<CartItem> cartItems = new ArrayList<>();
    private BigDecimal totalAmount;
    private LocalDateTime lastUpdated;
    private LocalDateTime expiryDate;
    
    // Private constructor for JPA
    protected Cart() {}
    
    // Factory method for creating carts
    public static Cart create(String customerId) {
        Cart cart = new Cart();
        cart.cartId = generateCartId();
        cart.setCustomerId(customerId);
        cart.totalAmount = BigDecimal.ZERO;
        cart.lastUpdated = LocalDateTime.now();
        cart.expiryDate = LocalDateTime.now().plusDays(7); // Cart expires in 7 days
        return cart;
    }
    
    // Business methods for cart management
    public void addItem(String productId, String productName, BigDecimal unitPrice, 
                       Integer quantity, String productSpecifications) {
        validateNotExpired();
        
        // Check if item already exists in cart
        CartItem existingItem = findCartItemByProductId(productId);
        if (existingItem != null) {
            existingItem.updateQuantity(existingItem.getQuantity() + quantity);
        } else {
            CartItem newItem = CartItem.create(productId, productName, unitPrice, quantity, productSpecifications);
            newItem.setCartId(this.cartId);
            cartItems.add(newItem);
        }
        
        recalculateTotal();
        updateLastModified();
    }
    
    public void updateItemQuantity(String productId, Integer newQuantity) {
        validateNotExpired();
        
        CartItem cartItem = findCartItemByProductId(productId);
        if (cartItem == null) {
            throw new ValidationException("Product not found in cart: " + productId);
        }
        
        if (newQuantity <= 0) {
            removeItem(productId);
        } else {
            cartItem.updateQuantity(newQuantity);
            recalculateTotal();
            updateLastModified();
        }
    }
    
    public void removeItem(String productId) {
        validateNotExpired();
        
        boolean removed = cartItems.removeIf(item -> item.getProductId().equals(productId));
        if (!removed) {
            throw new ValidationException("Product not found in cart: " + productId);
        }
        
        recalculateTotal();
        updateLastModified();
    }
    
    public void clear() {
        cartItems.clear();
        totalAmount = BigDecimal.ZERO;
        updateLastModified();
    }
    
    public Order convertToOrder(String customerName, String customerEmail, 
                               String shippingAddress, String billingAddress) {
        validateNotExpired();
        
        if (isEmpty()) {
            throw new ValidationException("Cannot create order from empty cart");
        }
        
        Order order = Order.create(customerId, customerName, customerEmail, shippingAddress, billingAddress);
        
        // Convert cart items to order items
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = OrderItem.create(
                cartItem.getProductId(),
                cartItem.getProductName(),
                cartItem.getUnitPrice(),
                cartItem.getQuantity(),
                cartItem.getProductSpecifications()
            );
            order.addOrderItem(orderItem);
        }
        
        return order;
    }
    
    // Query methods
    public boolean isEmpty() {
        return cartItems.isEmpty();
    }
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }
    
    public int getTotalItemCount() {
        return cartItems.stream()
            .mapToInt(CartItem::getQuantity)
            .sum();
    }
    
    public List<CartItem> getCartItems() {
        return Collections.unmodifiableList(cartItems);
    }
    
    public CartItem findCartItemByProductId(String productId) {
        return cartItems.stream()
            .filter(item -> item.getProductId().equals(productId))
            .findFirst()
            .orElse(null);
    }
    
    public boolean containsProduct(String productId) {
        return findCartItemByProductId(productId) != null;
    }
    
    // Private helper methods
    private void recalculateTotal() {
        this.totalAmount = cartItems.stream()
            .map(CartItem::getTotalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    private void updateLastModified() {
        this.lastUpdated = LocalDateTime.now();
        this.setUpdatedAt(LocalDateTime.now());
    }
    
    private void validateNotExpired() {
        if (isExpired()) {
            throw new ValidationException("Cart has expired");
        }
    }
    
    private static String generateCartId() {
        return "CART-" + System.currentTimeMillis();
    }
    
    // Validation methods
    private void setCustomerId(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new ValidationException("Customer ID is required");
        }
        this.customerId = customerId.trim();
    }
    
    // Getters
    public String getCartId() { return cartId; }
    public String getCustomerId() { return customerId; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public LocalDateTime getExpiryDate() { return expiryDate; }
    
    public void extendExpiry(int days) {
        this.expiryDate = LocalDateTime.now().plusDays(days);
        updateLastModified();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cart cart = (Cart) o;
        return Objects.equals(cartId, cart.cartId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(cartId);
    }
    
    @Override
    public String toString() {
        return "Cart{" +
                "cartId='" + cartId + '\'' +
                ", customerId='" + customerId + '\'' +
                ", itemCount=" + cartItems.size() +
                ", totalAmount=" + totalAmount +
                ", isExpired=" + isExpired() +
                '}';
    }
    
    /**
     * 購物車項目內部類別
     */
    public static class CartItem extends BaseEntity {
        
        private String cartItemId;
        private String cartId;
        private String productId;
        private String productName;
        private BigDecimal unitPrice;
        private Integer quantity;
        private BigDecimal totalPrice;
        private String productSpecifications;
        
        // Private constructor
        protected CartItem() {}
        
        // Factory method
        public static CartItem create(String productId, String productName, BigDecimal unitPrice,
                                    Integer quantity, String productSpecifications) {
            CartItem cartItem = new CartItem();
            cartItem.cartItemId = generateCartItemId();
            cartItem.setProductId(productId);
            cartItem.setProductName(productName);
            cartItem.setUnitPrice(unitPrice);
            cartItem.setQuantity(quantity);
            cartItem.setProductSpecifications(productSpecifications);
            cartItem.calculateTotalPrice();
            return cartItem;
        }
        
        // Business methods
        public void updateQuantity(Integer newQuantity) {
            setQuantity(newQuantity);
            calculateTotalPrice();
        }
        
        private void calculateTotalPrice() {
            if (unitPrice != null && quantity != null) {
                this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
            }
        }
        
        // Validation methods
        private void setProductId(String productId) {
            if (productId == null || productId.trim().isEmpty()) {
                throw new ValidationException("Product ID is required");
            }
            this.productId = productId.trim();
        }
        
        private void setProductName(String productName) {
            if (productName == null || productName.trim().isEmpty()) {
                throw new ValidationException("Product name is required");
            }
            this.productName = productName.trim();
        }
        
        private void setUnitPrice(BigDecimal unitPrice) {
            if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ValidationException("Unit price must be greater than zero");
            }
            this.unitPrice = unitPrice;
        }
        
        private void setQuantity(Integer quantity) {
            if (quantity == null || quantity <= 0) {
                throw new ValidationException("Quantity must be positive");
            }
            if (quantity > 999) {
                throw new ValidationException("Quantity cannot exceed 999");
            }
            this.quantity = quantity;
        }
        
        private void setProductSpecifications(String productSpecifications) {
            this.productSpecifications = productSpecifications;
        }
        
        private static String generateCartItemId() {
            return "CARTITEM-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
        }
        
        // Package-private setter
        void setCartId(String cartId) {
            this.cartId = cartId;
        }
        
        // Getters
        public String getCartItemId() { return cartItemId; }
        public String getCartId() { return cartId; }
        public String getProductId() { return productId; }
        public String getProductName() { return productName; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public Integer getQuantity() { return quantity; }
        public BigDecimal getTotalPrice() { return totalPrice; }
        public String getProductSpecifications() { return productSpecifications; }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CartItem cartItem = (CartItem) o;
            return Objects.equals(cartItemId, cartItem.cartItemId);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(cartItemId);
        }
        
        @Override
        public String toString() {
            return "CartItem{" +
                    "cartItemId='" + cartItemId + '\'' +
                    ", productId='" + productId + '\'' +
                    ", productName='" + productName + '\'' +
                    ", quantity=" + quantity +
                    ", unitPrice=" + unitPrice +
                    ", totalPrice=" + totalPrice +
                    '}';
        }
    }
}