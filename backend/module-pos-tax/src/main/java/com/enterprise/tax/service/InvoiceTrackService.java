/**
 * @file InvoiceTrackService.java
 * @description 發票字軌服務 / Invoice track service
 * @description_en Manages MoF-allocated character tracks; allocates sequential invoice numbers with pessimistic locking
 * @description_zh 管理財政部配發字軌；以悲觀鎖確保發票號碼無重複分配
 */
package com.enterprise.tax.service;

import com.enterprise.tax.dto.request.AddInvoiceTrackRequest;
import com.enterprise.tax.entity.InvoiceTrack;
import com.enterprise.tax.repository.InvoiceTrackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvoiceTrackService {

    private final InvoiceTrackRepository trackRepository;

    @Transactional(readOnly = true)
    public List<InvoiceTrack> listByStore(UUID storeId) {
        return trackRepository.findByStoreIdAndIsActiveTrueOrderByTrackPrefix(storeId);
    }

    @Transactional
    public InvoiceTrack addTrack(AddInvoiceTrackRequest req) {
        InvoiceTrack track = new InvoiceTrack();
        track.setStoreId(req.storeId());
        track.setSellerId(req.sellerId());
        track.setTrackPrefix(req.trackPrefix());
        track.setYearMonth(req.yearMonth());
        track.setPeriod(req.period());
        track.setStartNo(req.startNo());
        track.setEndNo(req.endNo());
        track.setCurrentNo("00000000");
        return trackRepository.save(track);
    }

    // ========================================
    // 字軌配發演算法（悲觀鎖）/ Track allocation algorithm (pessimistic lock)
    // 取得下一張可用號碼，並更新 current_no
    // ========================================
    @Transactional
    public String allocateNextNumber(UUID storeId) {
        InvoiceTrack track = trackRepository.findAvailableTrackForUpdate(storeId)
                .orElseThrow(() -> new IllegalStateException("No available invoice track for store: " + storeId));

        int next = Integer.parseInt(track.getCurrentNo()) + 1;
        if (next > Integer.parseInt(track.getEndNo())) {
            throw new IllegalStateException("Invoice track exhausted: " + track.getTrackPrefix());
        }

        String nextNo = String.format("%08d", next);
        track.setCurrentNo(nextNo);
        trackRepository.save(track);

        return track.getTrackPrefix() + "-" + nextNo;
    }

    @Transactional
    public InvoiceTrack getTrackForStore(UUID storeId) {
        return trackRepository.findAvailableTrackForUpdate(storeId)
                .orElseThrow(() -> new IllegalStateException("No available invoice track for store: " + storeId));
    }
}
