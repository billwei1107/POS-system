/**
 * @file TaxRuleRepository.java
 * @description 稅務規則資料存取層 / Tax rule repository
 * @description_en Queries tax rules by store, product, and category with priority ordering
 * @description_zh 依門店、商品、分類查詢稅務規則，並依優先度排序
 */
package com.enterprise.tax.repository;

import com.enterprise.tax.entity.TaxRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TaxRuleRepository extends JpaRepository<TaxRule, UUID> {

    List<TaxRule> findByStoreIdAndIsActiveTrueOrderByPriorityDesc(UUID storeId);

    @Query("SELECT r FROM TaxRule r WHERE r.storeId = :storeId AND r.isActive = true " +
           "AND (r.productId = :productId OR r.categoryId = :categoryId OR (r.productId IS NULL AND r.categoryId IS NULL)) " +
           "ORDER BY r.priority DESC")
    List<TaxRule> findApplicableRules(@Param("storeId") UUID storeId,
                                      @Param("productId") UUID productId,
                                      @Param("categoryId") UUID categoryId);
}
