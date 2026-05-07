/**
 * @file ProductItemRepository.java
 * @description 商品資料存取層 / Product item repository
 */
package com.enterprise.product.repository;

import com.enterprise.product.entity.ProductItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductItemRepository extends JpaRepository<ProductItem, UUID> {

    Optional<ProductItem> findBySku(String sku);

    Optional<ProductItem> findByBarcodePrimary(String barcode);

    Page<ProductItem> findByCategoryIdAndActiveTrue(UUID categoryId, Pageable pageable);

    Page<ProductItem> findByActiveTrue(Pageable pageable);

    // 多欄位關鍵字搜尋 / Keyword search across name, sku, barcode
    @Query("SELECT p FROM ProductItem p WHERE p.active = true AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%',:kw,'%')) OR " +
           " LOWER(p.sku) LIKE LOWER(CONCAT('%',:kw,'%')) OR " +
           " LOWER(p.barcodePrimary) LIKE LOWER(CONCAT('%',:kw,'%')))")
    Page<ProductItem> searchByKeyword(@Param("kw") String keyword, Pageable pageable);

    boolean existsBySku(String sku);
}
