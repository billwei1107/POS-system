/**
 * @file GatewayConfigServiceTest.java
 * @description 支付閘道設定服務測試 / Payment gateway config service tests
 * @description_en Verifies gateway config listing, secret hiding, duplicate prevention, and secret update rules
 * @description_zh 驗證閘道設定查詢、密鑰隱藏、重複防護與密鑰更新規則
 */
package com.enterprise.payment.service;

import com.enterprise.common.exception.BusinessException;
import com.enterprise.payment.dto.request.CreateGatewayConfigRequest;
import com.enterprise.payment.dto.request.UpdateGatewayConfigRequest;
import com.enterprise.payment.dto.response.GatewayConfigResponse;
import com.enterprise.payment.entity.GatewayConfig;
import com.enterprise.payment.repository.GatewayConfigRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GatewayConfigServiceTest {

    @Mock private GatewayConfigRepository gatewayConfigRepository;

    @Test
    void listByStore_returnsSecretPresenceFlagsWithoutSecretValues() {
        UUID storeId = UUID.randomUUID();
        GatewayConfig config = gatewayConfig(storeId, GatewayConfig.GatewayType.MOCK_CARD);
        config.setApiKey("secret-key");
        config.setApiSecret("secret-value");
        when(gatewayConfigRepository.findByStoreId(storeId)).thenReturn(List.of(config));

        GatewayConfigService service = new GatewayConfigService(gatewayConfigRepository);
        List<GatewayConfigResponse> responses = service.listByStore(storeId);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).apiKeyConfigured()).isTrue();
        assertThat(responses.get(0).apiSecretConfigured()).isTrue();
    }

    @Test
    void create_existingGatewayType_throwsBusinessException() {
        UUID storeId = UUID.randomUUID();
        CreateGatewayConfigRequest req = createRequest(storeId, GatewayConfig.GatewayType.CASH);
        when(gatewayConfigRepository.existsByStoreIdAndGatewayType(storeId, GatewayConfig.GatewayType.CASH))
                .thenReturn(true);

        GatewayConfigService service = new GatewayConfigService(gatewayConfigRepository);

        assertThatThrownBy(() -> service.create(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Gateway config already exists");
        verify(gatewayConfigRepository, never()).save(any());
    }

    @Test
    void update_blankSecrets_keepExistingStoredSecrets() {
        UUID gatewayId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        GatewayConfig existing = gatewayConfig(storeId, GatewayConfig.GatewayType.MOCK_CARD);
        existing.setId(gatewayId);
        existing.setApiKey("old-key");
        existing.setApiSecret("old-secret");
        when(gatewayConfigRepository.findById(gatewayId)).thenReturn(Optional.of(existing));
        when(gatewayConfigRepository.save(any(GatewayConfig.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GatewayConfigService service = new GatewayConfigService(gatewayConfigRepository);
        service.update(gatewayId, new UpdateGatewayConfigRequest(
                "更新刷卡",
                "MID-002",
                "",
                null,
                "https://gateway.example.local",
                "{}",
                true,
                true
        ));

        ArgumentCaptor<GatewayConfig> captor = ArgumentCaptor.forClass(GatewayConfig.class);
        verify(gatewayConfigRepository).save(captor.capture());
        assertThat(captor.getValue().getApiKey()).isEqualTo("old-key");
        assertThat(captor.getValue().getApiSecret()).isEqualTo("old-secret");
        assertThat(captor.getValue().getDisplayName()).isEqualTo("更新刷卡");
    }

    private CreateGatewayConfigRequest createRequest(UUID storeId, GatewayConfig.GatewayType gatewayType) {
        return new CreateGatewayConfigRequest(
                storeId,
                gatewayType,
                "現金",
                null,
                null,
                null,
                null,
                null,
                false,
                true
        );
    }

    private GatewayConfig gatewayConfig(UUID storeId, GatewayConfig.GatewayType gatewayType) {
        GatewayConfig config = new GatewayConfig();
        config.setId(UUID.randomUUID());
        config.setStoreId(storeId);
        config.setGatewayType(gatewayType);
        config.setDisplayName(gatewayType.name());
        config.setSandbox(true);
        config.setActive(true);
        return config;
    }
}
