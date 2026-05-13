/**
 * @file PayMethodRepository.java
 * @description 支付方式 Repository / Pay method JPA repository
 * @description_en Data access for pos_pay_methods; soft-delete filter applied via @SQLRestriction
 * @description_zh 存取 pos_pay_methods，@SQLRestriction 自動過濾已刪除資料
 */
package com.enterprise.payment.repository;

import com.enterprise.payment.entity.PayMethod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PayMethodRepository extends JpaRepository<PayMethod, UUID> {

    List<PayMethod> findByStoreIdAndIsActiveTrueOrderBySortOrder(UUID storeId);

    Optional<PayMethod> findByStoreIdAndCode(UUID storeId, String code);

    boolean existsByStoreIdAndCode(UUID storeId, String code);
}
