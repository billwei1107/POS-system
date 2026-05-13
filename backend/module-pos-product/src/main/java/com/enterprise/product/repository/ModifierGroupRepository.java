/**
 * @file ModifierGroupRepository.java
 * @description 客製化群組資料存取層 / Modifier group repository
 */
package com.enterprise.product.repository;

import com.enterprise.product.entity.ModifierGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ModifierGroupRepository extends JpaRepository<ModifierGroup, UUID> {
}
