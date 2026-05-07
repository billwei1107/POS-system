/**
 * @file ForeignCurrencyRepository.java
 * @description 外幣匯率 Repository / Foreign currency JPA repository
 * @description_en Data access for pos_foreign_currencies; soft-delete filter via @SQLRestriction
 * @description_zh 存取 pos_foreign_currencies，@SQLRestriction 過濾已刪除匯率設定
 */
package com.enterprise.payment.repository;

import com.enterprise.payment.entity.ForeignCurrency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ForeignCurrencyRepository extends JpaRepository<ForeignCurrency, UUID> {

    List<ForeignCurrency> findByStoreIdAndIsActiveTrueOrderByCurrencyCode(UUID storeId);

    Optional<ForeignCurrency> findByStoreIdAndCurrencyCode(UUID storeId, String currencyCode);
}
