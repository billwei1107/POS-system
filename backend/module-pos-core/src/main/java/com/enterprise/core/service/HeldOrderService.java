/**
 * @file HeldOrderService.java
 * @description 掛單服務 / Held order service
 * @description_en Persists, lists and removes parked POS cart snapshots
 * @description_zh 保存、查詢與移除 POS 暫存掛單快照
 */
package com.enterprise.core.service;

import com.enterprise.common.exception.ResourceNotFoundException;
import com.enterprise.core.dto.request.CreateHeldOrderRequest;
import com.enterprise.core.dto.response.HeldOrderResponse;
import com.enterprise.core.entity.HeldOrder;
import com.enterprise.core.repository.HeldOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HeldOrderService {

    private final HeldOrderRepository heldOrderRepository;

    // ========================================
    // 建立掛單 / Create held order
    // ========================================
    @Transactional
    public HeldOrderResponse create(CreateHeldOrderRequest request) {
        HeldOrder heldOrder = new HeldOrder();
        heldOrder.setStoreId(request.storeId());
        heldOrder.setTerminalId(request.terminalId());
        heldOrder.setLabel(request.label());
        heldOrder.setPayload(request.payload());
        heldOrder.setHeldAt(LocalDateTime.now());
        return HeldOrderResponse.from(heldOrderRepository.save(heldOrder));
    }

    // ========================================
    // 查詢掛單 / List held orders
    // ========================================
    @Transactional(readOnly = true)
    public List<HeldOrderResponse> list(UUID storeId, UUID terminalId) {
        List<HeldOrder> heldOrders = terminalId != null
            ? heldOrderRepository.findByStoreIdAndTerminalIdOrderByHeldAtDesc(storeId, terminalId)
            : heldOrderRepository.findByStoreIdOrderByHeldAtDesc(storeId);
        return heldOrders.stream().map(HeldOrderResponse::from).toList();
    }

    // ========================================
    // 刪除掛單 / Delete held order
    // ========================================
    @Transactional
    public void delete(UUID id) {
        HeldOrder heldOrder = heldOrderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Held order not found: " + id));
        heldOrderRepository.delete(heldOrder);
    }
}
