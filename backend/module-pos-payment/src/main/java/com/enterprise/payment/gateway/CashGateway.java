/**
 * @file CashGateway.java
 * @description 現金支付閘道實作 / Cash payment gateway implementation
 * @description_en Always succeeds; change calculation is handled by PricingEngine before this call
 * @description_zh 現金付款永遠成功，找零計算由 PricingEngine 在上游處理完畢
 */
package com.enterprise.payment.gateway;

import com.enterprise.payment.entity.GatewayConfig;
import com.enterprise.payment.gateway.dto.GatewayRequest;
import com.enterprise.payment.gateway.dto.GatewayResponse;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CashGateway implements PaymentGateway {

    @Override
    public GatewayConfig.GatewayType gatewayType() {
        return GatewayConfig.GatewayType.CASH;
    }

    // ========================================
    // 現金付款，本地直接成功 / Cash always succeeds locally
    // ========================================
    @Override
    public GatewayResponse charge(GatewayRequest request) {
        String ref = "CASH-" + request.orderId() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return GatewayResponse.ok(ref, "{\"method\":\"CASH\",\"amount\":" + request.amount() + "}");
    }

    // ========================================
    // 現金退款，本地直接成功 / Cash refund always succeeds locally
    // ========================================
    @Override
    public GatewayResponse refund(GatewayRequest request) {
        String ref = "CASH-REFUND-" + request.orderId() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return GatewayResponse.ok(ref, "{\"method\":\"CASH_REFUND\",\"amount\":" + request.amount() + "}");
    }
}
