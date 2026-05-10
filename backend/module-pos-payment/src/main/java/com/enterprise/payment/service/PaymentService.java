/**
 * @file PaymentService.java
 * @description 支付核心服務 / Payment core service
 * @description_en Processes payments via gateway strategy, consumes OrderCompletedEvent, publishes PaymentProcessedEvent
 * @description_zh 透過閘道策略處理付款，消費 OrderCompletedEvent，發布 PaymentProcessedEvent
 */
package com.enterprise.payment.service;

import com.enterprise.common.exception.BusinessException;
import com.enterprise.common.exception.ResourceNotFoundException;
import com.enterprise.core.event.OrderCompletedEvent;
import com.enterprise.payment.dto.request.ProcessPaymentRequest;
import com.enterprise.payment.dto.response.PaymentTransactionResponse;
import com.enterprise.payment.entity.GatewayConfig;
import com.enterprise.payment.entity.PayMethod;
import com.enterprise.payment.entity.PaymentTransaction;
import com.enterprise.payment.event.PaymentProcessedEvent;
import com.enterprise.payment.gateway.PaymentGateway;
import com.enterprise.payment.gateway.dto.GatewayRequest;
import com.enterprise.payment.gateway.dto.GatewayResponse;
import com.enterprise.payment.repository.PayMethodRepository;
import com.enterprise.payment.repository.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PayMethodRepository payMethodRepository;
    private final PaymentTransactionRepository transactionRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final List<PaymentGateway> gateways;

    // ========================================
    // 閘道策略路由 / Gateway strategy router
    // ========================================
    private Map<GatewayConfig.GatewayType, PaymentGateway> gatewayMap() {
        return gateways.stream().collect(
            Collectors.toMap(PaymentGateway::gatewayType, Function.identity())
        );
    }

    // ========================================
    // 消費 OrderCompletedEvent / Consume OrderCompletedEvent
    // ========================================
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onOrderCompleted(OrderCompletedEvent event) {
        log.info("PaymentService received OrderCompletedEvent for order={}", event.getOrderNo());
        // OrderService already recorded the payment record in pos_order_payments.
        // Here we create the canonical PaymentTransaction record with gateway tracking.
        PaymentTransaction txn = new PaymentTransaction();
        txn.setOrderId(event.getOrderId());
        txn.setStoreId(event.getStoreId());
        txn.setMethodType("UNKNOWN"); // method resolved later via explicit processPayment call
        txn.setAmount(event.getGrandTotal());
        txn.setStatus(PaymentTransaction.TxnStatus.SUCCESS);

        // Only create a stub transaction if no explicit call was made
        if (!transactionRepository.findByOrderId(event.getOrderId()).isEmpty()) {
            return;
        }
        transactionRepository.save(txn);
    }

    // ========================================
    // 手動發起支付 / Manual payment processing
    // ========================================
    @Transactional
    public PaymentTransactionResponse processPayment(ProcessPaymentRequest req) {
        PayMethod payMethod = payMethodRepository.findById(req.payMethodId())
            .orElseThrow(() -> new ResourceNotFoundException("PayMethod not found: " + req.payMethodId()));

        GatewayConfig.GatewayType gatewayType = resolveGatewayType(payMethod);
        PaymentGateway gateway = gatewayMap().get(gatewayType);
        if (gateway == null) {
            throw new BusinessException("No gateway implementation for type: " + gatewayType);
        }

        GatewayRequest gatewayReq = new GatewayRequest(
            req.orderId(), req.orderNo(), req.amount(), req.tendered(),
            req.currency() != null ? req.currency() : "TWD",
            UUID.randomUUID().toString(), req.note()
        );

        GatewayResponse gatewayResp = gateway.charge(gatewayReq);

        PaymentTransaction txn = new PaymentTransaction();
        txn.setOrderId(req.orderId());
        txn.setStoreId(req.storeId());
        txn.setPayMethodId(req.payMethodId());
        txn.setMethodType(payMethod.getMethodType().name());
        txn.setAmount(req.amount());
        txn.setTendered(req.tendered());
        txn.setChangeGiven(req.tendered() != null
            ? req.tendered().subtract(req.amount()).max(java.math.BigDecimal.ZERO)
            : java.math.BigDecimal.ZERO);
        txn.setStatus(gatewayResp.success() ? PaymentTransaction.TxnStatus.SUCCESS : PaymentTransaction.TxnStatus.FAILED);
        txn.setGatewayRef(gatewayResp.gatewayRef());
        txn.setGatewayResp(gatewayResp.rawResponse());
        txn.setErrorCode(gatewayResp.errorCode());
        txn.setErrorMsg(gatewayResp.errorMessage());
        txn = transactionRepository.save(txn);

        if (!gatewayResp.success()) {
            throw new BusinessException("Payment failed: " + gatewayResp.errorMessage());
        }

        eventPublisher.publishEvent(new PaymentProcessedEvent(
            this, txn.getId(), txn.getOrderId(), txn.getStoreId(),
            req.orderNo(), txn.getMethodType(), txn.getAmount()
        ));

        return PaymentTransactionResponse.from(txn);
    }

    // ========================================
    // 查詢訂單付款記錄 / Get transactions by order
    // ========================================
    @Transactional(readOnly = true)
    public List<PaymentTransactionResponse> getByOrder(UUID orderId) {
        return transactionRepository.findByOrderId(orderId).stream()
            .map(PaymentTransactionResponse::from).toList();
    }

    // ========================================
    // 閘道類型解析 / Resolve gateway type from pay method
    // ========================================
    private GatewayConfig.GatewayType resolveGatewayType(PayMethod payMethod) {
        return switch (payMethod.getMethodType()) {
            case CASH -> GatewayConfig.GatewayType.CASH;
            case CARD -> GatewayConfig.GatewayType.MOCK_CARD;
            default -> GatewayConfig.GatewayType.MOCK_CARD;
        };
    }
}
