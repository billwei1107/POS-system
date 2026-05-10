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

import java.math.BigDecimal;
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
        if (!transactionRepository.findByOrderId(event.getOrderId()).isEmpty()) {
            return;
        }

        PayMethod payMethod = resolveEventPayMethod(event.getStoreId(), event.getPayMethod());
        GatewayConfig.GatewayType gatewayType = resolveGatewayType(payMethod);
        PaymentGateway gateway = gatewayMap().get(gatewayType);
        if (gateway == null) {
            throw new BusinessException("No gateway implementation for type: " + gatewayType);
        }

        BigDecimal amount = event.getPaidAmount() != null ? event.getPaidAmount() : event.getGrandTotal();
        GatewayResponse gatewayResp = gateway.charge(new GatewayRequest(
            event.getOrderId(), event.getOrderNo(), amount, event.getTenderedAmount(),
            "TWD", UUID.randomUUID().toString(), "OrderCompletedEvent"
        ));

        PaymentTransaction txn = new PaymentTransaction();
        txn.setOrderId(event.getOrderId());
        txn.setStoreId(event.getStoreId());
        txn.setPayMethodId(payMethod.getId());
        txn.setMethodType(payMethod.getMethodType().name());
        txn.setAmount(amount);
        txn.setTendered(event.getTenderedAmount());
        txn.setChangeGiven(event.getChangeGiven() != null ? event.getChangeGiven() : BigDecimal.ZERO);
        txn.setStatus(gatewayResp.success() ? PaymentTransaction.TxnStatus.SUCCESS : PaymentTransaction.TxnStatus.FAILED);
        txn.setGatewayRef(gatewayResp.gatewayRef());
        txn.setGatewayResp(gatewayResp.rawResponse());
        txn.setErrorCode(gatewayResp.errorCode());
        txn.setErrorMsg(gatewayResp.errorMessage());
        transactionRepository.save(txn);

        if (!gatewayResp.success()) {
            throw new BusinessException("Payment failed: " + gatewayResp.errorMessage());
        }

        eventPublisher.publishEvent(new PaymentProcessedEvent(
            this, txn.getId(), txn.getOrderId(), txn.getStoreId(),
            event.getTerminalId(), event.getEmployeeId(),
            event.getOrderNo(), txn.getMethodType(), txn.getAmount()
        ));
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

    // ========================================
    // 事件支付方式解析 / Resolve event pay method
    // ========================================
    private PayMethod resolveEventPayMethod(UUID storeId, String payMethodCode) {
        String code = normalizePayMethodCode(payMethodCode);
        return payMethodRepository.findByStoreIdAndCode(storeId, code)
            .orElseGet(() -> createDefaultEventPayMethod(storeId, code));
    }

    private PayMethod createDefaultEventPayMethod(UUID storeId, String code) {
        PayMethod pm = new PayMethod();
        pm.setStoreId(storeId);
        pm.setCode(code);
        pm.setName(defaultPayMethodName(code));
        pm.setMethodType(defaultMethodType(code));
        pm.setChangeBack(pm.getMethodType() == PayMethod.MethodType.CASH);
        pm.setSortOrder(pm.getMethodType() == PayMethod.MethodType.CASH ? 0 : 100);
        return payMethodRepository.save(pm);
    }

    private String normalizePayMethodCode(String payMethodCode) {
        if (payMethodCode == null || payMethodCode.isBlank()) {
            return "CASH";
        }
        return payMethodCode.trim().toUpperCase();
    }

    private PayMethod.MethodType defaultMethodType(String code) {
        return switch (code) {
            case "CASH" -> PayMethod.MethodType.CASH;
            case "CREDIT", "CREDIT_CARD", "CARD" -> PayMethod.MethodType.CARD;
            case "LINE_PAY", "LINEPAY", "JKOPAY", "EASYCARD" -> PayMethod.MethodType.QR_CODE;
            default -> PayMethod.MethodType.MIXED;
        };
    }

    private String defaultPayMethodName(String code) {
        return switch (code) {
            case "CASH" -> "現金";
            case "CREDIT", "CREDIT_CARD", "CARD" -> "信用卡";
            case "LINE_PAY", "LINEPAY" -> "LINE Pay";
            case "JKOPAY" -> "街口支付";
            case "EASYCARD" -> "悠遊卡";
            default -> code;
        };
    }
}
