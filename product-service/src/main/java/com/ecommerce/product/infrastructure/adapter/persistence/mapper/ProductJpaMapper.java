package com.ecommerce.product.infrastructure.adapter.persistence.mapper;

import com.ecommerce.product.domain.model.Product;
import com.ecommerce.product.infrastructure.adapter.persistence.entity.ProductJpaEntity;

import java.util.List;

/**
 * Product JPA Mapper
 * Follows SRP principle by handling only product entity mapping between domain and persistence layers
 */
public class ProductJpaMapper {
    
    /**
     * Convert Product domain entity to ProductJpaEntity
     * @param product the domain product entity
     * @return the JPA product entity
     */
    public static ProductJpaEntity toJpaEntity(Product product) {
        if (product == null) {
            return null;
        }
        
        ProductJpaEntity jpaEntity = new ProductJpaEntity();
        jpaEntity.setProductId(product.getProductId());
        jpaEntity.setName(product.getName());
        jpaEntity.setDescription(product.getDescription());
        jpaEntity.setCategory(product.getCategory());
        jpaEntity.setPrice(product.getPrice());
        jpaEntity.setBrand(product.getBrand());
        jpaEntity.setModel(product.getModel());
        jpaEntity.setStatus(product.getStatus());
        jpaEntity.setSpecifications(product.getSpecifications());
        jpaEntity.setImageUrl(product.getImageUrl());
        jpaEntity.setLaunchDate(product.getLaunchDate());
        jpaEntity.setCreatedAt(product.getCreatedAt());
        jpaEntity.setUpdatedAt(product.getUpdatedAt());
        
        return jpaEntity;
    }
    
    /**
     * Convert ProductJpaEntity to Product domain entity
     * @param jpaEntity the JPA product entity
     * @return the domain product entity
     */
    public static Product toDomainEntity(ProductJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }
        
        Product product = Product.create(
            jpaEntity.getName(),
            jpaEntity.getDescription(),
            jpaEntity.getCategory(),
            jpaEntity.getPrice(),
            jpaEntity.getBrand(),
            jpaEntity.getModel(),
            jpaEntity.getSpecifications()
        );
        
        // Set additional fields that are not set by the factory method
        setDomainEntityFields(product, jpaEntity);
        
        return product;
    }
    
    /**
     * Convert list of ProductJpaEntity to list of Product domain entities
     * @param jpaEntities the list of JPA product entities
     * @return the list of domain product entities
     */
    public static List<Product> toDomainEntityList(List<ProductJpaEntity> jpaEntities) {
        if (jpaEntities == null) {
            return null;
        }
        
        return jpaEntities.stream()
                .map(ProductJpaMapper::toDomainEntity)
                .toList();
    }
    
    /**
     * Convert list of Product domain entities to list of ProductJpaEntity
     * @param products the list of domain product entities
     * @return the list of JPA product entities
     */
    public static List<ProductJpaEntity> toJpaEntityList(List<Product> products) {
        if (products == null) {
            return null;
        }
        
        return products.stream()
                .map(ProductJpaMapper::toJpaEntity)
                .toList();
    }
    
    /**
     * Update existing ProductJpaEntity with data from Product domain entity
     * @param jpaEntity the existing JPA entity to update
     * @param product the domain entity with updated data
     */
    public static void updateJpaEntity(ProductJpaEntity jpaEntity, Product product) {
        if (jpaEntity == null || product == null) {
            return;
        }
        
        jpaEntity.setName(product.getName());
        jpaEntity.setDescription(product.getDescription());
        jpaEntity.setCategory(product.getCategory());
        jpaEntity.setPrice(product.getPrice());
        jpaEntity.setBrand(product.getBrand());
        jpaEntity.setModel(product.getModel());
        jpaEntity.setStatus(product.getStatus());
        jpaEntity.setSpecifications(product.getSpecifications());
        jpaEntity.setImageUrl(product.getImageUrl());
        jpaEntity.setLaunchDate(product.getLaunchDate());
        jpaEntity.setUpdatedAt(product.getUpdatedAt());
    }
    
    /**
     * Update existing Product domain entity with data from ProductJpaEntity
     * @param product the existing domain entity to update
     * @param jpaEntity the JPA entity with updated data
     */
    public static void updateDomainEntity(Product product, ProductJpaEntity jpaEntity) {
        if (product == null || jpaEntity == null) {
            return;
        }
        
        product.updateProductInfo(jpaEntity.getName(), jpaEntity.getDescription(), jpaEntity.getPrice());
        product.updateSpecifications(jpaEntity.getSpecifications());
        
        if (jpaEntity.getImageUrl() != null) {
            product.setImageUrl(jpaEntity.getImageUrl());
        }
        
        // Update status if different
        if (!product.getStatus().equals(jpaEntity.getStatus())) {
            switch (jpaEntity.getStatus()) {
                case AVAILABLE -> product.makeAvailable();
                case OUT_OF_STOCK -> product.markOutOfStock();
                case DISCONTINUED -> product.discontinue();
            }
        }
    }
    
    /**
     * Private helper method to set domain entity fields using reflection-like approach
     * Since domain entities may have private setters, we need to handle this carefully
     */
    private static void setDomainEntityFields(Product product, ProductJpaEntity jpaEntity) {
        // Note: In a real implementation, you might need to use reflection or 
        // provide package-private setters in the domain entity for persistence purposes
        // For now, we'll assume the factory method creates the entity correctly
        // and only the status might need to be updated
        
        if (jpaEntity.getStatus() != null) {
            switch (jpaEntity.getStatus()) {
                case AVAILABLE -> product.makeAvailable();
                case OUT_OF_STOCK -> product.markOutOfStock();
                case DISCONTINUED -> product.discontinue();
            }
        }
        
        if (jpaEntity.getImageUrl() != null) {
            product.setImageUrl(jpaEntity.getImageUrl());
        }
    }
}