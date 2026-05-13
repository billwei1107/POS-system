/**
 * @file StaffEventListenerTest.java
 * @description 班次事件監聽器測試 / Staff event listener tests
 * @description_en Verifies sales accumulation uses terminal/employee context
 * @description_zh 驗證銷售統計會依終端機/員工上下文歸屬正確班次
 */
package com.enterprise.staff;

import com.enterprise.core.event.OrderCompletedEvent;
import com.enterprise.staff.entity.StaffShift;
import com.enterprise.staff.repository.StaffShiftRepository;
import com.enterprise.staff.service.StaffEventListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StaffEventListenerTest {

    @Mock private StaffShiftRepository shiftRepository;

    private StaffEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new StaffEventListener(shiftRepository);
    }

    @Test
    void onOrderCompleted_usesTerminalShiftWhenMultipleShiftsAreOpen() {
        UUID storeId = UUID.randomUUID();
        UUID terminalId = UUID.randomUUID();
        UUID employeeId = UUID.randomUUID();
        StaffShift shift = shift(storeId, terminalId, employeeId, "S-001");

        when(shiftRepository.findOpenByStoreIdAndTerminalId(storeId, terminalId))
            .thenReturn(Optional.of(shift));

        listener.onOrderCompleted(new OrderCompletedEvent(
            this,
            UUID.randomUUID(),
            storeId,
            terminalId,
            employeeId,
            "ORD-001",
            null,
            new BigDecimal("126.00"),
            new BigDecimal("6.00"),
            "CASH",
            new BigDecimal("126.00"),
            new BigDecimal("130.00"),
            new BigDecimal("4.00")
        ));

        ArgumentCaptor<StaffShift> shiftCaptor = ArgumentCaptor.forClass(StaffShift.class);
        verify(shiftRepository).save(shiftCaptor.capture());
        assertThat(shiftCaptor.getValue().getTotalSales()).isEqualByComparingTo("126.00");
        assertThat(shiftCaptor.getValue().getTransactionCount()).isEqualTo(1);
    }

    @Test
    void onOrderCompleted_withoutContextAndMultipleOpenShifts_doesNotPickFirstShift() {
        UUID storeId = UUID.randomUUID();
        when(shiftRepository.findOpenByStoreId(storeId)).thenReturn(List.of(
            shift(storeId, UUID.randomUUID(), UUID.randomUUID(), "S-001"),
            shift(storeId, UUID.randomUUID(), UUID.randomUUID(), "S-002")
        ));

        listener.onOrderCompleted(new OrderCompletedEvent(
            this,
            UUID.randomUUID(),
            storeId,
            "ORD-002",
            null,
            new BigDecimal("126.00")
        ));

        verify(shiftRepository, never()).save(any());
    }

    private StaffShift shift(UUID storeId, UUID terminalId, UUID employeeId, String shiftNo) {
        StaffShift shift = new StaffShift();
        shift.setStoreId(storeId);
        shift.setTerminalId(terminalId);
        shift.setEmployeeId(employeeId);
        shift.setShiftNo(shiftNo);
        return shift;
    }
}
