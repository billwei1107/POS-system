/**
 * @file GatewayConfigRepository.java
 * @description 閘道配置 Repository / Gateway config JPA repository
 * @description_en Data access for pos_gateway_configs; soft-delete filter via @SQLRestriction
 * @description_zh 存取 pos_gateway_configs，@SQLRestriction 過濾已刪除閘道設定
 */
package com.enterprise.payment.repository;

import com.enterprise.payment.entity.GatewayConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GatewayConfigRepository extends JpaRepository<GatewayConfig, UUID> {

    List<GatewayConfig> findByStoreId(UUID storeId);

    List<GatewayConfig> findByStoreIdAndIsActiveTrue(UUID storeId);

    Optional<GatewayConfig> findByStoreIdAndGatewayType(UUID storeId, GatewayConfig.GatewayType gatewayType);

    boolean existsByStoreIdAndGatewayType(UUID storeId, GatewayConfig.GatewayType gatewayType);
}
