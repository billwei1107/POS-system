/**
 * @file TaxClassRepository.java
 * @description 稅率類別資料存取層 / Tax class repository
 * @description_en JPA repository for tax class CRUD and store-scoped queries
 * @description_zh 稅率類別 JPA 資料存取，支援門店範圍查詢
 */
package com.enterprise.tax.repository;

import com.enterprise.tax.entity.TaxClass;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaxClassRepository extends JpaRepository<TaxClass, UUID> {

    List<TaxClass> findByStoreIdAndIsActiveTrueOrderByName(UUID storeId);

    Optional<TaxClass> findByStoreIdAndIsDefaultTrue(UUID storeId);
}
