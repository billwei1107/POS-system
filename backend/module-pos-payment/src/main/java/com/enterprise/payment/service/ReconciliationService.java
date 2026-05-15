/**
 * @file ReconciliationService.java
 * @description 對帳服務 / Reconciliation service
 * @description_en Aggregates daily payment transactions into reconciliation records per store+method
 * @description_zh 將每日支付交易彙總為每門店每支付方式的對帳記錄
 */
package com.enterprise.payment.service;

import com.enterprise.common.exception.ResourceNotFoundException;
import com.enterprise.payment.entity.PayMethod;
import com.enterprise.payment.entity.Reconciliation;
import com.enterprise.payment.repository.PayMethodRepository;
import com.enterprise.payment.repository.PaymentTransactionRepository;
import com.enterprise.payment.repository.ReconciliationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReconciliationService {

    private final ReconciliationRepository reconciliationRepository;
    private final PaymentTransactionRepository transactionRepository;
    private final PayMethodRepository payMethodRepository;

    // ========================================
    // 產生每日對帳 / Generate daily reconciliation
    // ========================================
    @Transactional
    public List<Reconciliation> generateDaily(UUID storeId, LocalDate date) {
        LocalDateTime from = date.atStartOfDay();
        LocalDateTime to = date.plusDays(1).atStartOfDay();

        List<PayMethod> methods = payMethodRepository.findByStoreIdAndIsActiveTrueOrderBySortOrder(storeId);

        return methods.stream().map(pm -> {
            var existingRecon = reconciliationRepository
                .findByStoreIdAndReconDateAndPayMethodId(storeId, date, pm.getId());
            if (existingRecon.isPresent()
                && existingRecon.get().getStatus() != Reconciliation.ReconStatus.PENDING) {
                return existingRecon.get();
            }

            var txns = transactionRepository.findByStoreAndDateRange(storeId, from, to)
                .stream().filter(t -> pm.getId().equals(t.getPayMethodId())).toList();

            var successTxns = txns.stream()
                .filter(t -> t.getStatus() == com.enterprise.payment.entity.PaymentTransaction.TxnStatus.SUCCESS)
                .toList();
            var refundTxns = txns.stream()
                .filter(t -> t.getStatus() == com.enterprise.payment.entity.PaymentTransaction.TxnStatus.REFUNDED)
                .toList();

            BigDecimal totalAmount = successTxns.stream()
                .map(com.enterprise.payment.entity.PaymentTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal refundAmount = refundTxns.stream()
                .map(com.enterprise.payment.entity.PaymentTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            Reconciliation recon = existingRecon.orElse(new Reconciliation());

            recon.setStoreId(storeId);
            recon.setReconDate(date);
            recon.setPayMethodId(pm.getId());
            recon.setMethodType(pm.getMethodType().name());
            recon.setTransactionCount(successTxns.size());
            recon.setTotalAmount(totalAmount);
            recon.setRefundCount(refundTxns.size());
            recon.setRefundAmount(refundAmount);
            recon.setNetAmount(totalAmount.subtract(refundAmount));
            recon.setStatus(Reconciliation.ReconStatus.PENDING);

            return reconciliationRepository.save(recon);
        }).toList();
    }

    // ========================================
    // 查詢對帳記錄 / Query reconciliation by store and date
    // ========================================
    @Transactional(readOnly = true)
    public List<Reconciliation> listByStoreAndDate(UUID storeId, LocalDate date) {
        return reconciliationRepository.findByStoreIdAndReconDateOrderByMethodType(storeId, date);
    }

    // ========================================
    // 查詢對帳門店 / Find reconciliation store
    // ========================================
    @Transactional(readOnly = true)
    public UUID findStoreId(UUID reconId) {
        return reconciliationRepository.findById(reconId)
            .orElseThrow(() -> new ResourceNotFoundException("Reconciliation not found: " + reconId))
            .getStoreId();
    }

    // ========================================
    // 確認對帳 / Mark reconciliation as matched/discrepancy
    // ========================================
    @Transactional
    public Reconciliation confirm(UUID reconId, BigDecimal gatewayAmount, UUID reconciledBy) {
        Reconciliation recon = reconciliationRepository.findById(reconId)
            .orElseThrow(() -> new ResourceNotFoundException("Reconciliation not found: " + reconId));

        recon.setGatewayAmount(gatewayAmount);
        BigDecimal variance = recon.getNetAmount().subtract(gatewayAmount);
        recon.setVariance(variance);
        recon.setStatus(variance.compareTo(BigDecimal.ZERO) == 0
            ? Reconciliation.ReconStatus.MATCHED
            : Reconciliation.ReconStatus.DISCREPANCY);
        recon.setReconciledBy(reconciledBy);
        recon.setReconciledAt(LocalDateTime.now());

        return reconciliationRepository.save(recon);
    }
}
