package com.ecommerce.order.application.usecase;

import com.ecommerce.common.architecture.UseCase;
import com.ecommerce.order.application.dto.AddToCartRequest;
import com.ecommerce.order.application.dto.CartDto;
import com.ecommerce.order.application.dto.UpdateCartItemRequest;
import com.ecommerce.order.application.mapper.CartMapper;
import com.ecommerce.order.application.port.in.CartUseCase;
import com.ecommerce.order.application.port.out.CartPersistencePort;
import com.ecommerce.order.application.port.out.ProductServicePort;
import com.ecommerce.order.domain.exception.CartNotFoundException;
import com.ecommerce.order.domain.model.Cart;
import com.ecommerce.order.domain.service.CartDomainService;

/**
 * 購物車服務
 * 實作購物車相關的業務邏輯
 */
@UseCase
public class CartService implements CartUseCase {
    
    private final CartPersistencePort cartPersistencePort;
    private final ProductServicePort productServicePort;
    private final CartDomainService cartDomainService;
    
    public CartService(CartPersistencePort cartPersistencePort,
                      ProductServicePort productServicePort,
                      CartDomainService cartDomainService) {
        this.cartPersistencePort = cartPersistencePort;
        this.productServicePort = productServicePort;
        this.cartDomainService = cartDomainService;
    }
    
    @Override
    public CartDto getOrCreateCart(String customerId) {
        Cart cart = cartDomainService.getOrCreateCart(customerId);
        return CartMapper.toDto(cart);
    }
    
    @Override
    public CartDto addToCart(String customerId, AddToCartRequest request) {
        // 驗證商品是否可用
        if (!productServicePort.isProductAvailable(request.getProductId())) {
            throw new IllegalArgumentException("Product is not available: " + request.getProductId());
        }
        
        // 驗證庫存是否足夠
        if (!productServicePort.hasAvailableStock(request.getProductId(), request.getQuantity())) {
            throw new IllegalArgumentException("Insufficient stock for product: " + request.getProductId());
        }
        
        // 獲取或創建購物車
        Cart cart = cartDomainService.getOrCreateCart(customerId);
        
        // 加入商品到購物車
        cart.addItem(
            request.getProductId(),
            request.getProductName(),
            request.getUnitPrice(),
            request.getQuantity(),
            request.getProductSpecifications()
        );
        
        // 儲存購物車
        Cart savedCart = cartPersistencePort.save(cart);
        
        return CartMapper.toDto(savedCart);
    }
    
    @Override
    public CartDto updateCartItem(String customerId, String productId, UpdateCartItemRequest request) {
        Cart cart = cartPersistencePort.findByCustomerId(customerId)
            .orElseThrow(() -> CartNotFoundException.byCustomerId(customerId));
        
        // 驗證庫存是否足夠
        if (!productServicePort.hasAvailableStock(productId, request.getQuantity())) {
            throw new IllegalArgumentException("Insufficient stock for product: " + productId);
        }
        
        // 更新購物車項目數量
        cart.updateItemQuantity(productId, request.getQuantity());
        
        // 儲存購物車
        Cart savedCart = cartPersistencePort.save(cart);
        
        return CartMapper.toDto(savedCart);
    }
    
    @Override
    public CartDto removeFromCart(String customerId, String productId) {
        Cart cart = cartPersistencePort.findByCustomerId(customerId)
            .orElseThrow(() -> CartNotFoundException.byCustomerId(customerId));
        
        // 從購物車移除商品
        cart.removeItem(productId);
        
        // 儲存購物車
        Cart savedCart = cartPersistencePort.save(cart);
        
        return CartMapper.toDto(savedCart);
    }
    
    @Override
    public CartDto clearCart(String customerId) {
        Cart cart = cartPersistencePort.findByCustomerId(customerId)
            .orElseThrow(() -> CartNotFoundException.byCustomerId(customerId));
        
        // 清空購物車
        cart.clear();
        
        // 儲存購物車
        Cart savedCart = cartPersistencePort.save(cart);
        
        return CartMapper.toDto(savedCart);
    }
    
    @Override
    public CartDto getCart(String customerId) {
        Cart cart = cartPersistencePort.findByCustomerId(customerId)
            .orElseThrow(() -> CartNotFoundException.byCustomerId(customerId));
        
        return CartMapper.toDto(cart);
    }
    
    @Override
    public boolean cartContainsProduct(String customerId, String productId) {
        return cartDomainService.cartContainsProduct(customerId, productId);
    }
    
    @Override
    public CartDto extendCartExpiry(String customerId, int days) {
        Cart cart = cartPersistencePort.findByCustomerId(customerId)
            .orElseThrow(() -> CartNotFoundException.byCustomerId(customerId));
        
        Cart extendedCart = cartDomainService.extendCartExpiry(cart.getCartId(), days);
        
        return CartMapper.toDto(extendedCart);
    }
    
    @Override
    public CartDto mergeCarts(String targetCustomerId, String sourceCartId) {
        Cart sourceCart = cartPersistencePort.findById(sourceCartId)
            .orElseThrow(() -> CartNotFoundException.byCartId(sourceCartId));
        
        Cart mergedCart = cartDomainService.mergeCarts(targetCustomerId, sourceCart);
        
        return CartMapper.toDto(mergedCart);
    }
    
    @Override
    public void validateCartForOrder(String customerId) {
        Cart cart = cartPersistencePort.findByCustomerId(customerId)
            .orElseThrow(() -> CartNotFoundException.byCustomerId(customerId));
        
        cartDomainService.validateCartForOrder(cart);
        
        // 額外驗證所有商品的可用性和庫存
        for (Cart.CartItem cartItem : cart.getCartItems()) {
            if (!productServicePort.isProductAvailable(cartItem.getProductId())) {
                throw new IllegalStateException("Product is not available: " + cartItem.getProductName());
            }
            
            if (!productServicePort.hasAvailableStock(cartItem.getProductId(), cartItem.getQuantity())) {
                throw new IllegalStateException("Insufficient stock for product: " + cartItem.getProductName());
            }
        }
    }
    
    @Override
    public int cleanupExpiredCarts() {
        return cartDomainService.cleanupExpiredCarts();
    }
    
    @Override
    public int cleanupAbandonedCarts(int daysThreshold) {
        return cartDomainService.cleanupAbandonedCarts(daysThreshold);
    }
}