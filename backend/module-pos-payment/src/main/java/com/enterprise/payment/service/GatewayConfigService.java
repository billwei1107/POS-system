/**
 * @file GatewayConfigService.java
 * @description 支付閘道設定服務 / Payment gateway config service
 * @description_en Manages store-level gateway configuration while preventing secret leakage in responses
 * @description_zh 管理門店層級支付閘道設定，並避免在回應中洩漏密鑰
 */
package com.enterprise.payment.service;

import com.enterprise.common.exception.BusinessException;
import com.enterprise.common.exception.ResourceNotFoundException;
import com.enterprise.payment.dto.request.CreateGatewayConfigRequest;
import com.enterprise.payment.dto.request.UpdateGatewayConfigRequest;
import com.enterprise.payment.dto.response.GatewayConfigResponse;
import com.enterprise.payment.entity.GatewayConfig;
import com.enterprise.payment.repository.GatewayConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GatewayConfigService {

    private final GatewayConfigRepository gatewayConfigRepository;

    // ========================================
    // 查詢門店閘道設定 / List gateway configs by store
    // ========================================
    @Transactional(readOnly = true)
    public List<GatewayConfigResponse> listByStore(UUID storeId) {
        return gatewayConfigRepository.findByStoreId(storeId).stream()
            .sorted(Comparator.comparing(GatewayConfig::getGatewayType))
            .map(GatewayConfigResponse::from)
            .toList();
    }

    // ========================================
    // 查詢閘道設定門店 / Find gateway config store
    // ========================================
    @Transactional(readOnly = true)
    public UUID findStoreId(UUID id) {
        return gatewayConfigRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("GatewayConfig not found: " + id))
            .getStoreId();
    }

    // ========================================
    // 建立閘道設定 / Create gateway config
    // ========================================
    @Transactional
    public GatewayConfigResponse create(CreateGatewayConfigRequest req) {
        if (gatewayConfigRepository.existsByStoreIdAndGatewayType(req.storeId(), req.gatewayType())) {
            throw new BusinessException("Gateway config already exists: " + req.gatewayType());
        }

        GatewayConfig config = new GatewayConfig();
        config.setStoreId(req.storeId());
        config.setGatewayType(req.gatewayType());
        applyCreate(config, req);
        return GatewayConfigResponse.from(gatewayConfigRepository.save(config));
    }

    // ========================================
    // 更新閘道設定 / Update gateway config
    // ========================================
    @Transactional
    public GatewayConfigResponse update(UUID id, UpdateGatewayConfigRequest req) {
        GatewayConfig config = gatewayConfigRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("GatewayConfig not found: " + id));

        config.setDisplayName(req.displayName());
        config.setMerchantId(trimToNull(req.merchantId()));
        config.setEndpointUrl(trimToNull(req.endpointUrl()));
        config.setExtraConfig(trimToNull(req.extraConfig()));
        config.setSandbox(req.isSandbox());
        config.setActive(req.isActive());
        applySecretUpdates(config, req.apiKey(), req.apiSecret());
        return GatewayConfigResponse.from(gatewayConfigRepository.save(config));
    }

    // ========================================
    // 停用閘道設定 / Deactivate gateway config
    // ========================================
    @Transactional
    public void deactivate(UUID id) {
        GatewayConfig config = gatewayConfigRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("GatewayConfig not found: " + id));
        config.setActive(false);
        gatewayConfigRepository.save(config);
    }

    private void applyCreate(GatewayConfig config, CreateGatewayConfigRequest req) {
        config.setDisplayName(req.displayName());
        config.setMerchantId(trimToNull(req.merchantId()));
        config.setApiKey(trimToNull(req.apiKey()));
        config.setApiSecret(trimToNull(req.apiSecret()));
        config.setEndpointUrl(trimToNull(req.endpointUrl()));
        config.setExtraConfig(trimToNull(req.extraConfig()));
        config.setSandbox(req.isSandbox());
        config.setActive(req.isActive());
    }

    private void applySecretUpdates(GatewayConfig config, String apiKey, String apiSecret) {
        if (hasText(apiKey)) {
            config.setApiKey(apiKey.trim());
        }
        if (hasText(apiSecret)) {
            config.setApiSecret(apiSecret.trim());
        }
    }

    private String trimToNull(String value) {
        return hasText(value) ? value.trim() : null;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
