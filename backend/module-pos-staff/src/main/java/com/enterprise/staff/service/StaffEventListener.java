/**
 * @file StaffEventListener.java
 * @description 排班事件監聽器 / Staff module event listener
 * @description_en Consumes order/payment/void events to accumulate shift statistics
 * @description_zh 消費訂單/支付/作廢事件，累計班次銷售統計資料
 */
package com.enterprise.staff.service;

import com.enterprise.core.event.OrderCompletedEvent;
import com.enterprise.core.event.OrderVoidedEvent;
import com.enterprise.core.event.RefundCompletedEvent;
import com.enterprise.staff.entity.StaffShift;
import com.enterprise.staff.repository.StaffShiftRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StaffEventListener {

    private final StaffShiftRepository shiftRepository;

    // ========================================
    // 訂單完成 → 累計班次銷售 / Order completed → accumulate sales
    // ========================================
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onOrderCompleted(OrderCompletedEvent event) {
        Optional<StaffShift> openShift = findOpenShiftForContext(
                event.getStoreId(),
                event.getTerminalId(),
                event.getEmployeeId());
        if (openShift.isEmpty()) {
            log.debug("No open shift for store {}, skipping sales accumulation for order {}", event.getStoreId(), event.getOrderId());
            return;
        }
        StaffShift shift = openShift.get();
        shift.addSale(event.getGrandTotal(),
                event.getTaxAmount() != null ? event.getTaxAmount() : BigDecimal.ZERO,
                BigDecimal.ZERO);
        shiftRepository.save(shift);
        log.info("Accumulated sale {} to shift {} for order {}", event.getGrandTotal(), shift.getShiftNo(), event.getOrderId());
    }

    // ========================================
    // 退款完成 → 累計班次退款 / Refund completed → accumulate refund
    // ========================================
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onRefundCompleted(RefundCompletedEvent event) {
        Optional<StaffShift> openShift = findOpenShiftForContext(event.getStoreId(), null, null);
        if (openShift.isEmpty()) {
            log.debug("No open shift for store {}, skipping refund accumulation for refund {}", event.getStoreId(), event.getRefundId());
            return;
        }
        StaffShift shift = openShift.get();
        shift.addRefund(event.getRefundAmount());
        shiftRepository.save(shift);
        log.info("Accumulated refund {} to shift {} for refund {}", event.getRefundAmount(), shift.getShiftNo(), event.getRefundId());
    }

    // ========================================
    // 訂單作廢 → 遞減交易計數 / Order voided → decrement transaction count
    // ========================================
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onOrderVoided(OrderVoidedEvent event) {
        Optional<StaffShift> openShift = findOpenShiftForContext(event.getStoreId(), null, null);
        if (openShift.isEmpty()) {
            log.debug("No open shift for store {}, skipping void for order {}", event.getStoreId(), event.getOrderId());
            return;
        }
        StaffShift shift = openShift.get();
        if (shift.getTransactionCount() > 0) {
            shift.setTransactionCount(shift.getTransactionCount() - 1);
            shiftRepository.save(shift);
        }
        log.info("Order voided recorded on shift {} for order {}", shift.getShiftNo(), event.getOrderId());
    }

    // ========================================
    // 找對應班次 / Find matching open shift
    // ========================================
    private Optional<StaffShift> findOpenShiftForContext(UUID storeId, UUID terminalId, UUID employeeId) {
        if (terminalId != null) {
            Optional<StaffShift> byTerminal = shiftRepository.findOpenByStoreIdAndTerminalId(storeId, terminalId);
            if (byTerminal.isPresent()) {
                return byTerminal;
            }
        }

        if (employeeId != null) {
            Optional<StaffShift> byEmployee = shiftRepository.findOpenByEmployeeId(employeeId)
                    .filter(shift -> storeId.equals(shift.getStoreId()));
            if (byEmployee.isPresent()) {
                return byEmployee;
            }
        }

        List<StaffShift> openShifts = shiftRepository.findOpenByStoreId(storeId);
        if (openShifts.size() == 1) {
            return Optional.of(openShifts.get(0));
        }
        if (openShifts.size() > 1) {
            log.warn("Multiple open shifts for store {}, terminal/employee context required", storeId);
        }
        return Optional.empty();
    }
}
