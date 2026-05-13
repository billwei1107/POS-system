/**
 * @file PaymentGateway.java
 * @description 支付閘道抽象介面 / Payment gateway abstraction interface
 * @description_en Strategy interface for all payment gateway implementations; Phase 3 integrates real gateways
 * @description_zh 所有支付閘道的策略介面，Phase 3 再接入真實閘道 API
 */
package com.enterprise.payment.gateway;

import com.enterprise.payment.gateway.dto.GatewayRequest;
import com.enterprise.payment.gateway.dto.GatewayResponse;
import com.enterprise.payment.entity.GatewayConfig;

public interface PaymentGateway {

    // ========================================
    // 閘道類型識別 / Gateway type identifier
    // ========================================
    GatewayConfig.GatewayType gatewayType();

    // ========================================
    // 發起付款 / Process payment
    // ========================================
    GatewayResponse charge(GatewayRequest request);

    // ========================================
    // 作廢/退款 / Void or refund
    // ========================================
    GatewayResponse refund(GatewayRequest request);
}
