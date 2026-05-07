/**
 * @file InvoiceTrackServiceTest.java
 * @description 字軌配發演算法單元測試 / Invoice track allocation algorithm unit tests
 * @description_en Verifies correct sequential number allocation and exhaustion detection
 * @description_zh 驗證字軌號碼循序分配與字軌耗盡偵測邏輯
 */
package com.enterprise.tax;

import com.enterprise.tax.entity.InvoiceTrack;
import com.enterprise.tax.repository.InvoiceTrackRepository;
import com.enterprise.tax.service.InvoiceTrackService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceTrackServiceTest {

    @Mock
    private InvoiceTrackRepository trackRepository;

    @InjectMocks
    private InvoiceTrackService trackService;

    private UUID storeId;
    private InvoiceTrack track;

    @BeforeEach
    void setUp() {
        storeId = UUID.randomUUID();
        track = new InvoiceTrack();
        track.setStoreId(storeId);
        track.setTrackPrefix("AB");
        track.setStartNo("00000001");
        track.setEndNo("00000050");
        track.setCurrentNo("00000000");
    }

    // ========================================
    // 字軌分配測試 / Track allocation tests
    // ========================================

    @Test
    void allocateNextNumber_firstAllocation_returnsAB00000001() {
        when(trackRepository.findAvailableTrackForUpdate(storeId)).thenReturn(Optional.of(track));
        when(trackRepository.save(any())).thenReturn(track);

        String result = trackService.allocateNextNumber(storeId);

        assertEquals("AB-00000001", result);
        assertEquals("00000001", track.getCurrentNo());
    }

    @Test
    void allocateNextNumber_secondAllocation_returnsAB00000002() {
        track.setCurrentNo("00000001");
        when(trackRepository.findAvailableTrackForUpdate(storeId)).thenReturn(Optional.of(track));
        when(trackRepository.save(any())).thenReturn(track);

        String result = trackService.allocateNextNumber(storeId);

        assertEquals("AB-00000002", result);
        assertEquals("00000002", track.getCurrentNo());
    }

    @Test
    void allocateNextNumber_noTrackAvailable_throwsIllegalState() {
        when(trackRepository.findAvailableTrackForUpdate(storeId)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> trackService.allocateNextNumber(storeId));
    }

    @Test
    void allocateNextNumber_trackAtEndNo_throwsIllegalState() {
        track.setCurrentNo("00000050");
        when(trackRepository.findAvailableTrackForUpdate(storeId)).thenReturn(Optional.of(track));

        assertThrows(IllegalStateException.class, () -> trackService.allocateNextNumber(storeId));
    }

    @Test
    void isExhausted_belowEndNo_returnsFalse() {
        track.setCurrentNo("00000049");
        assertFalse(track.isExhausted());
    }

    @Test
    void isExhausted_atEndNo_returnsTrue() {
        track.setCurrentNo("00000050");
        assertTrue(track.isExhausted());
    }

    @Test
    void allocateNextNumber_atSecondToLast_returnsEndNo() {
        track.setCurrentNo("00000049");
        when(trackRepository.findAvailableTrackForUpdate(storeId)).thenReturn(Optional.of(track));
        when(trackRepository.save(any())).thenReturn(track);

        String result = trackService.allocateNextNumber(storeId);

        assertEquals("AB-00000050", result);
        assertEquals("00000050", track.getCurrentNo());
    }

    @Test
    void addTrack_persistsWithDefaultCurrentNo() {
        InvoiceTrack saved = new InvoiceTrack();
        saved.setCurrentNo("00000000");
        when(trackRepository.save(any())).thenReturn(saved);

        var req = new com.enterprise.tax.dto.request.AddInvoiceTrackRequest(
                storeId, "12345678", "CD", "11401", "11401-11402", "00000001", "00000100");

        InvoiceTrack result = trackService.addTrack(req);

        assertEquals("00000000", result.getCurrentNo());
        verify(trackRepository, times(1)).save(any());
    }
}
