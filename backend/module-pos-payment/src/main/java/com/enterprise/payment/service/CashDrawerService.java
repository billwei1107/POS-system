/**
 * @file CashDrawerService.java
 * @description 現金抽屜服務 / Cash drawer service
 * @description_en Manages cash drawer open/close lifecycle and logs all drawer events as audit trail
 * @description_zh 管理現金抽屜開關生命週期，每次操作記錄稽核日誌
 */
package com.enterprise.payment.service;

import com.enterprise.common.exception.BusinessException;
import com.enterprise.common.exception.ResourceNotFoundException;
import com.enterprise.payment.dto.request.OpenDrawerRequest;
import com.enterprise.payment.entity.CashDrawer;
import com.enterprise.payment.entity.CashDrawerEvent;
import com.enterprise.payment.event.PaymentProcessedEvent;
import com.enterprise.payment.repository.CashDrawerEventRepository;
import com.enterprise.payment.repository.CashDrawerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CashDrawerService {

    private final CashDrawerRepository cashDrawerRepository;
    private final CashDrawerEventRepository eventRepository;

    // ========================================
    // 開啟現金抽屜 / Open cash drawer
    // ========================================
    @Transactional
    public CashDrawer open(OpenDrawerRequest req) {
        if (cashDrawerRepository.existsByTerminalIdAndStatus(req.terminalId(), CashDrawer.DrawerStatus.OPEN)) {
            throw new BusinessException("Cash drawer already open on terminal: " + req.terminalId());
        }
        CashDrawer drawer = new CashDrawer();
        drawer.setStoreId(req.storeId());
        drawer.setTerminalId(req.terminalId());
        drawer.setOpenedBy(req.openedBy());
        drawer.setOpeningAmount(req.openingAmount() != null ? req.openingAmount() : BigDecimal.ZERO);
        drawer = cashDrawerRepository.save(drawer);

        logEvent(drawer.getId(), CashDrawerEvent.EventType.OPEN, drawer.getOpeningAmount(), req.openedBy(), null, null);
        return drawer;
    }

    // ========================================
    // 關閉現金抽屜 / Close cash drawer
    // ========================================
    @Transactional
    public CashDrawer close(UUID drawerId, UUID closedBy, BigDecimal closingAmount, String note) {
        CashDrawer drawer = cashDrawerRepository.findById(drawerId)
            .orElseThrow(() -> new ResourceNotFoundException("CashDrawer not found: " + drawerId));

        if (drawer.getStatus() != CashDrawer.DrawerStatus.OPEN) {
            throw new BusinessException("Cash drawer is not open");
        }

        BigDecimal expected = computeExpected(drawer);
        BigDecimal variance = closingAmount != null ? closingAmount.subtract(expected) : null;

        drawer.setClosedBy(closedBy);
        drawer.setClosedAt(LocalDateTime.now());
        drawer.setClosingAmount(closingAmount);
        drawer.setExpectedAmount(expected);
        drawer.setVariance(variance);
        drawer.setStatus(CashDrawer.DrawerStatus.CLOSED);
        drawer.setNote(note);
        drawer = cashDrawerRepository.save(drawer);

        logEvent(drawer.getId(), CashDrawerEvent.EventType.CLOSE,
            closingAmount != null ? closingAmount : BigDecimal.ZERO, closedBy, null, note);
        return drawer;
    }

    // ========================================
    // 查詢終端機目前抽屜 / Get open drawer for terminal
    // ========================================
    @Transactional(readOnly = true)
    public CashDrawer getOpen(UUID terminalId) {
        return cashDrawerRepository.findByTerminalIdAndStatus(terminalId, CashDrawer.DrawerStatus.OPEN)
            .orElseThrow(() -> new ResourceNotFoundException("No open drawer on terminal: " + terminalId));
    }

    // ========================================
    // 查詢錢櫃門店 / Find cash drawer store
    // ========================================
    @Transactional(readOnly = true)
    public UUID findStoreId(UUID drawerId) {
        return cashDrawerRepository.findById(drawerId)
            .orElseThrow(() -> new ResourceNotFoundException("CashDrawer not found: " + drawerId))
            .getStoreId();
    }

    // ========================================
    // 現金付款 → 記錄抽屜銷售 / Cash payment → record drawer sale
    // ========================================
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onPaymentProcessed(PaymentProcessedEvent event) {
        if (!"CASH".equalsIgnoreCase(event.getMethodType())) {
            return;
        }
        if (event.getTerminalId() == null || event.getEmployeeId() == null) {
            log.debug("Cash drawer sale skipped because terminal or employee is missing for order {}", event.getOrderId());
            return;
        }

        cashDrawerRepository.findByTerminalIdAndStatus(event.getTerminalId(), CashDrawer.DrawerStatus.OPEN)
            .ifPresentOrElse(drawer -> {
                logEvent(drawer.getId(), CashDrawerEvent.EventType.SALE, event.getAmount(),
                    event.getEmployeeId(), event.getOrderId(), "cash payment " + event.getOrderNo());
                log.info("Recorded cash drawer sale {} for order {}", event.getAmount(), event.getOrderId());
            }, () -> log.debug("No open cash drawer on terminal {}, skipping order {}",
                event.getTerminalId(), event.getOrderId()));
    }

    // ========================================
    // 工具方法：預期金額計算 / Compute expected closing amount
    // ========================================
    private BigDecimal computeExpected(CashDrawer drawer) {
        return eventRepository.findByDrawerIdOrderByOccurredAtAsc(drawer.getId()).stream()
            .map(CashDrawerEvent::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void logEvent(UUID drawerId, CashDrawerEvent.EventType type,
                           BigDecimal amount, UUID employeeId, UUID orderId, String note) {
        CashDrawerEvent event = new CashDrawerEvent();
        event.setDrawerId(drawerId);
        event.setEventType(type);
        event.setAmount(amount != null ? amount : BigDecimal.ZERO);
        event.setEmployeeId(employeeId);
        event.setOrderId(orderId);
        event.setNote(note);
        eventRepository.save(event);
    }
}
