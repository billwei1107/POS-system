/**
 * @file MockCardGateway.java
 * @description 模擬刷卡閘道實作 / Mock card payment gateway implementation
 * @description_en Simulates card gateway for development/testing; real gateway integrated in Phase 3
 * @description_zh 供開發與測試使用的模擬刷卡閘道，Phase 3 替換為真實閘道 API
 */
package com.enterprise.payment.gateway;

import com.enterprise.payment.entity.GatewayConfig;
import com.enterprise.payment.gateway.dto.GatewayRequest;
import com.enterprise.payment.gateway.dto.GatewayResponse;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MockCardGateway implements PaymentGateway {

    @Override
    public GatewayConfig.GatewayType gatewayType() {
        return GatewayConfig.GatewayType.MOCK_CARD;
    }

    // ========================================
    // 模擬刷卡授權 / Simulate card authorization
    // ========================================
    @Override
    public GatewayResponse charge(GatewayRequest request) {
        String approvalCode = "MOCK-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        String rawResp = String.format(
            "{\"approval_code\":\"%s\",\"amount\":%s,\"currency\":\"%s\",\"sandbox\":true}",
            approvalCode, request.amount(), request.currency() != null ? request.currency() : "TWD"
        );
        return GatewayResponse.ok(approvalCode, rawResp);
    }

    // ========================================
    // 模擬刷卡退款 / Simulate card refund
    // ========================================
    @Override
    public GatewayResponse refund(GatewayRequest request) {
        String refundRef = "MOCK-REFUND-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        String rawResp = String.format(
            "{\"refund_ref\":\"%s\",\"amount\":%s,\"sandbox\":true}",
            refundRef, request.amount()
        );
        return GatewayResponse.ok(refundRef, rawResp);
    }
}
