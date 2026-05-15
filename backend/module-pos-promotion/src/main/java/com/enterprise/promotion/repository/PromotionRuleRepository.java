/**
 * @file PromotionRuleRepository.java
 * @description POS 促銷規則資料存取 / POS promotion rule repository
 */
package com.enterprise.promotion.repository;

import com.enterprise.promotion.entity.PromotionRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PromotionRuleRepository extends JpaRepository<PromotionRule, UUID> {

    List<PromotionRule> findByStoreIdAndActiveTrue(UUID storeId);

    List<PromotionRule> findByStoreIdIsNullAndActiveTrue();
}
