/**
 * @file TransferService.java
 * @description 調撥服務 / Inter-store transfer service
 * @description_en Manages stock transfer lifecycle: request → approve → ship → receive
 * @description_zh 管理調撥全生命週期：申請 → 核准 → 出貨 → 收貨，自動更新雙門店庫存
 */
package com.enterprise.inventory.service;

import com.enterprise.inventory.dto.request.CreateTransferRequest;
import com.enterprise.inventory.entity.StockMovement;
import com.enterprise.inventory.entity.TransferItem;
import com.enterprise.inventory.entity.TransferRequest;
import com.enterprise.inventory.repository.StockMovementRepository;
import com.enterprise.inventory.repository.StoreStockRepository;
import com.enterprise.inventory.repository.TransferRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferService {

    private final TransferRequestRepository transferRepository;
    private final StoreStockRepository stockRepository;
    private final StockMovementRepository movementRepository;
    private final StockDeductionService deductionService;

    // ========================================
    // 建立調撥申請 / Create transfer request
    // ========================================
    @Transactional
    public TransferRequest create(CreateTransferRequest req) {
        TransferRequest transfer = new TransferRequest();
        transfer.setTransferNo(generateTransferNo());
        transfer.setFromStoreId(req.fromStoreId());
        transfer.setToStoreId(req.toStoreId());
        transfer.setNotes(req.notes());
        transfer.setRequestedBy(req.requestedBy());

        AtomicInteger seq = new AtomicInteger(0);
        req.items().forEach(item -> {
            TransferItem ti = new TransferItem();
            ti.setTransferRequest(transfer);
            ti.setItemId(item.itemId());
            ti.setRequestedQty(item.requestedQty());
            transfer.getItems().add(ti);
        });

        return transferRepository.save(transfer);
    }

    // ========================================
    // 核准調撥 / Approve transfer
    // ========================================
    @Transactional
    public TransferRequest approve(UUID transferId, UUID approvedBy) {
        TransferRequest transfer = findOrThrow(transferId);
        if (transfer.getStatus() != TransferRequest.TransferStatus.REQUESTED) {
            throw new IllegalStateException("Transfer is not in REQUESTED status");
        }
        transfer.setStatus(TransferRequest.TransferStatus.APPROVED);
        transfer.setApprovedBy(approvedBy);
        return transferRepository.save(transfer);
    }

    // ========================================
    // 確認出貨（扣來源門店庫存）/ Confirm shipment — deduct from source store
    // ========================================
    @Transactional
    public TransferRequest ship(UUID transferId) {
        TransferRequest transfer = findOrThrow(transferId);
        if (transfer.getStatus() != TransferRequest.TransferStatus.APPROVED) {
            throw new IllegalStateException("Transfer must be APPROVED before shipping");
        }
        transfer.setStatus(TransferRequest.TransferStatus.IN_TRANSIT);
        transfer.setShippedAt(Instant.now());

        transfer.getItems().forEach(item -> {
            BigDecimal qty = item.getRequestedQty();
            item.setShippedQty(qty);
            recordTransferMovement(transfer.getFromStoreId(), item.getItemId(),
                    qty.negate(), StockMovement.MovementType.TRANSFER_OUT, transfer.getId());
        });

        return transferRepository.save(transfer);
    }

    // ========================================
    // 確認收貨（入目的門店庫存）/ Confirm receipt — add to destination store
    // ========================================
    @Transactional
    public TransferRequest receive(UUID transferId) {
        TransferRequest transfer = findOrThrow(transferId);
        if (transfer.getStatus() != TransferRequest.TransferStatus.IN_TRANSIT) {
            throw new IllegalStateException("Transfer must be IN_TRANSIT before receiving");
        }
        transfer.setStatus(TransferRequest.TransferStatus.RECEIVED);
        transfer.setReceivedAt(Instant.now());

        transfer.getItems().forEach(item -> {
            BigDecimal qty = item.getShippedQty();
            item.setReceivedQty(qty);
            deductionService.receive(transfer.getToStoreId(), item.getItemId(), qty,
                    transfer.getId(), "Transfer from store " + transfer.getFromStoreId());
        });

        return transferRepository.save(transfer);
    }

    // ========================================
    // 取消調撥 / Cancel transfer
    // ========================================
    @Transactional
    public TransferRequest cancel(UUID transferId) {
        TransferRequest transfer = findOrThrow(transferId);
        if (transfer.getStatus() == TransferRequest.TransferStatus.RECEIVED) {
            throw new IllegalStateException("Cannot cancel a completed transfer");
        }
        transfer.setStatus(TransferRequest.TransferStatus.CANCELLED);
        return transferRepository.save(transfer);
    }

    public List<TransferRequest> listByStore(UUID storeId) {
        return transferRepository.findAllByFromStoreIdOrderByCreatedAtDesc(storeId);
    }

    public TransferRequest findById(UUID id) {
        return findOrThrow(id);
    }

    private TransferRequest findOrThrow(UUID id) {
        return transferRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transfer not found: " + id));
    }

    private void recordTransferMovement(UUID storeId, UUID itemId, BigDecimal qty,
                                         StockMovement.MovementType type, UUID transferId) {
        StockMovement m = new StockMovement();
        m.setStoreId(storeId);
        m.setItemId(itemId);
        m.setQuantityChange(qty);
        m.setMovementType(type);
        m.setReferenceId(transferId);
        m.setReferenceType("pos_inv_transfer_requests");
        movementRepository.save(m);
    }

    private String generateTransferNo() {
        return "TR" + System.currentTimeMillis();
    }
}
