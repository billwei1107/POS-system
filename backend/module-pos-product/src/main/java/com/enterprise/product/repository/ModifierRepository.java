/**
 * @file ModifierRepository.java
 * @description 客製化選項資料存取層 / Modifier repository
 */
package com.enterprise.product.repository;

import com.enterprise.product.entity.Modifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ModifierRepository extends JpaRepository<Modifier, UUID> {

    List<Modifier> findByGroupIdAndActiveTrueOrderBySortOrderAsc(UUID groupId);
}
