/**
 * @file ProductVariantRepository.java
 * @description 商品變體資料存取層 / Product variant repository
 */
package com.enterprise.product.repository;

import com.enterprise.product.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, UUID> {

    List<ProductVariant> findByItemIdAndActiveTrueOrderBySku(UUID itemId);

    Optional<ProductVariant> findBySku(String sku);

    boolean existsBySku(String sku);
}
