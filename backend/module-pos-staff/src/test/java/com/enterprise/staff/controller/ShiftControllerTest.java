/**
 * @file ShiftControllerTest.java
 * @description 班次 Controller 測試 / Shift controller tests
 * @description_en Verifies open shift and list open shift API contracts
 * @description_zh 驗證開班與查詢開放班次 API 回傳契約
 */
package com.enterprise.staff.controller;

import com.enterprise.organization.service.StoreAccessService;
import com.enterprise.staff.entity.StaffShift;
import com.enterprise.staff.service.ClockService;
import com.enterprise.staff.service.StaffShiftService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ShiftControllerTest {

    @Mock private StaffShiftService shiftService;
    @Mock private ClockService clockService;
    @Mock private StoreAccessService storeAccessService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ShiftController(shiftService, clockService, storeAccessService))
                .build();
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Test
    void listOpenShifts_returnsCashierShiftAggregates() throws Exception {
        UUID storeId = UUID.randomUUID();
        StaffShift shift = createOpenShift(storeId);
        shift.addSale(new BigDecimal("126.00"), new BigDecimal("6.00"), BigDecimal.ZERO);
        when(shiftService.listOpenShifts(storeId)).thenReturn(List.of(shift));

        mockMvc.perform(get("/api/v1/staff/shifts").param("storeId", storeId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].status").value("OPEN"))
                .andExpect(jsonPath("$.data[0].openingCash").value(1000.00))
                .andExpect(jsonPath("$.data[0].totalSales").value(126.00))
                .andExpect(jsonPath("$.data[0].totalTax").value(6.00))
                .andExpect(jsonPath("$.data[0].transactionCount").value(1));

        verify(storeAccessService).requireReadableStore(storeId);
    }

    @Test
    void openShift_returnsOpenedShiftForTerminal() throws Exception {
        UUID storeId = UUID.randomUUID();
        UUID employeeId = UUID.randomUUID();
        UUID terminalId = UUID.randomUUID();
        StaffShift shift = createOpenShift(storeId);
        shift.setEmployeeId(employeeId);
        shift.setTerminalId(terminalId);
        when(shiftService.openShift(storeId, employeeId, terminalId, new BigDecimal("1000.00"))).thenReturn(shift);

        mockMvc.perform(post("/api/v1/staff/shifts/open")
                        .param("storeId", storeId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new OpenShiftPayload(
                                employeeId,
                                terminalId,
                                new BigDecimal("1000.00")
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.storeId").value(storeId.toString()))
                .andExpect(jsonPath("$.data.employeeId").value(employeeId.toString()))
                .andExpect(jsonPath("$.data.terminalId").value(terminalId.toString()))
                .andExpect(jsonPath("$.data.status").value("OPEN"))
                .andExpect(jsonPath("$.data.openingCash").value(1000.00));

        verify(storeAccessService).requireOperableStore(storeId);
    }

    private StaffShift createOpenShift(UUID storeId) {
        StaffShift shift = new StaffShift();
        shift.setId(UUID.randomUUID());
        shift.setStoreId(storeId);
        shift.setEmployeeId(UUID.randomUUID());
        shift.setTerminalId(UUID.randomUUID());
        shift.setShiftNo("SH202605110330000001");
        shift.setStatus(StaffShift.ShiftStatus.OPEN);
        shift.setOpenedAt(Instant.parse("2026-05-11T03:30:00Z"));
        shift.setOpeningCash(new BigDecimal("1000.00"));
        return shift;
    }

    private record OpenShiftPayload(UUID employeeId, UUID terminalId, BigDecimal openingCash) {}
}
