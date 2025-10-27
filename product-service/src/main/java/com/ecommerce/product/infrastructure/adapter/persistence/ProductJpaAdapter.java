package com.ecommerce.product.infrastructure.adapter.persistence;

import com.ecommerce.common.architecture.Adapter;
import com.ecommerce.product.application.port.out.ProductPersistencePort;
import com.ecommerce.product.domain.model.Product;
import com.ecommerce.product.domain.model.ProductStatus;
import com.ecommerce.product.infrastructure.adapter.persistence.entity.ProductJpaEntity;
import com.ecommerce.product.infrastructure.adapter.persistence.mapper.ProductJpaMapper;
import com.ecommerce.product.infrastructure.adapter.persistence.repository.ProductJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Product JPA Adapter
 * Implements ProductPersistencePort using JPA for data persistence
 * Follows DIP principle by implementing the output port interface
 */
@Adapter
@Component
@Transactional
public class ProductJpaAdapter implements ProductPersistencePort {
    
    private final ProductJpaRepository productJpaRepository;
    
    public ProductJpaAdapter(ProductJpaRepository productJpaRepository) {
        this.productJpaRepository = productJpaRepository;
    }
    
    @Override
    public Product save(Product product) {
        ProductJpaEntity jpaEntity = ProductJpaMapper.toJpaEntity(product);
        ProductJpaEntity savedEntity = productJpaRepository.save(jpaEntity);
        return ProductJpaMapper.toDomainEntity(savedEntity);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Product> findById(String productId) {
        return productJpaRepository.findByProductId(productId)
                .map(ProductJpaMapper::toDomainEntity);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Product> searchByKeyword(String keyword) {
        List<ProductJpaEntity> jpaEntities = productJpaRepository.searchByKeyword(keyword);
        return ProductJpaMapper.toDomainEntityList(jpaEntities);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Product> searchByKeyword(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductJpaEntity> jpaEntitiesPage = productJpaRepository.searchByKeyword(keyword, pageable);
        return ProductJpaMapper.toDomainEntityList(jpaEntitiesPage.getContent());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Product> findByCategory(String category) {
        List<ProductJpaEntity> jpaEntities = productJpaRepository.findByCategory(category);
        return ProductJpaMapper.toDomainEntityList(jpaEntities);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Product> findByCategory(String category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductJpaEntity> jpaEntitiesPage = productJpaRepository.findByCategory(category, pageable);
        return ProductJpaMapper.toDomainEntityList(jpaEntitiesPage.getContent());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Product> findByBrand(String brand) {
        List<ProductJpaEntity> jpaEntities = productJpaRepository.findByBrand(brand);
        return ProductJpaMapper.toDomainEntityList(jpaEntities);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Product> findByStatus(ProductStatus status) {
        List<ProductJpaEntity> jpaEntities = productJpaRepository.findByStatus(status);
        return ProductJpaMapper.toDomainEntityList(jpaEntities);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Product> findByStatus(ProductStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductJpaEntity> jpaEntitiesPage = productJpaRepository.findByStatus(status, pageable);
        return ProductJpaMapper.toDomainEntityList(jpaEntitiesPage.getContent());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice) {
        List<ProductJpaEntity> jpaEntities = productJpaRepository.findByPriceBetween(minPrice, maxPrice);
        return ProductJpaMapper.toDomainEntityList(jpaEntities);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Product> findByBrandAndModel(String brand, String model) {
        List<ProductJpaEntity> jpaEntities = productJpaRepository.findByBrandAndModel(brand, model);
        return ProductJpaMapper.toDomainEntityList(jpaEntities);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsByBrandAndModel(String brand, String model) {
        return productJpaRepository.existsByBrandAndModel(brand, model);
    }
    
    @Override
    public void deleteById(String productId) {
        productJpaRepository.deleteByProductId(productId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long countByCategory(String category) {
        return productJpaRepository.countByCategory(category);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long countByStatus(ProductStatus status) {
        return productJpaRepository.countByStatus(status);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long countSearchResults(String keyword) {
        return productJpaRepository.countSearchResults(keyword);
    }
}