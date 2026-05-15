/**
 * @file PriceRuleRepository.java
 * @description 價格規則資料存取層 / Price rule repository
 */
package com.enterprise.product.repository;

import com.enterprise.product.entity.PriceRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PriceRuleRepository extends JpaRepository<PriceRule, UUID> {

    List<PriceRule> findByItemIdAndActiveTrue(UUID itemId);

    List<PriceRule> findByItemIdAndStoreIdAndActiveTrue(UUID itemId, UUID storeId);

    List<PriceRule> findByItemIdAndStoreIdIsNullAndActiveTrue(UUID itemId);
}
