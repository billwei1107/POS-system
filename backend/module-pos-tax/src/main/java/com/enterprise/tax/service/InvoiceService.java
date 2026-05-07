/**
 * @file InvoiceService.java
 * @description 電子發票服務 / E-invoice service
 * @description_en Core invoice logic: listens to OrderCompletedEvent/RefundCompletedEvent, allocates track numbers,
 *                issues invoices via TurnkeyClient, handles void and allowance
 * @description_zh 核心發票邏輯：消費 OrderCompletedEvent/RefundCompletedEvent，配發字軌號碼，
 *                 透過 TurnkeyClient 上傳財政部，處理作廢與折讓
 */
package com.enterprise.tax.service;

import com.enterprise.core.event.OrderCompletedEvent;
import com.enterprise.core.event.RefundCompletedEvent;
import com.enterprise.tax.dto.request.IssueInvoiceRequest;
import com.enterprise.tax.dto.response.InvoiceResponse;
import com.enterprise.tax.entity.Invoice;
import com.enterprise.tax.entity.InvoiceAllowance;
import com.enterprise.tax.entity.InvoiceTrack;
import com.enterprise.tax.event.InvoiceIssuedEvent;
import com.enterprise.tax.invoice.TurnkeyClient;
import com.enterprise.tax.repository.InvoiceAllowanceRepository;
import com.enterprise.tax.repository.InvoiceRepository;
import com.enterprise.tax.repository.InvoiceTrackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceAllowanceRepository allowanceRepository;
    private final InvoiceTrackRepository trackRepository;
    private final InvoiceTrackService trackService;
    private final TurnkeyClient turnkeyClient;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${pos.invoice.seller-id:12345678}")
    private String defaultSellerId;

    @Value("${pos.invoice.seller-name:企業商店}")
    private String defaultSellerName;

    // 台灣標準稅率 5%
    private static final BigDecimal TAX_RATE = new BigDecimal("0.05");

    // ========================================
    // 消費訂單完成事件，自動開立電子發票 / Auto-issue invoice on order completion
    // ========================================
    @EventListener
    @Transactional
    public void onOrderCompleted(OrderCompletedEvent event) {
        if (invoiceRepository.findByOrderId(event.getOrderId()).isPresent()) {
            log.debug("Invoice already exists for order {}", event.getOrderId());
            return;
        }
        try {
            issueForOrder(event.getOrderId(), event.getStoreId(), event.getGrandTotal());
        } catch (Exception e) {
            log.error("Failed to auto-issue invoice for order {}: {}", event.getOrderId(), e.getMessage());
        }
    }

    // ========================================
    // 消費退款完成事件，自動作廢或折讓 / Auto-void or allowance on refund completion
    // ========================================
    @EventListener
    @Transactional
    public void onRefundCompleted(RefundCompletedEvent event) {
        Optional<Invoice> invoiceOpt = invoiceRepository.findByOrderId(event.getOrderId());
        if (invoiceOpt.isEmpty()) {
            log.warn("No invoice found for order {} on refund {}", event.getOrderId(), event.getRefundId());
            return;
        }
        Invoice invoice = invoiceOpt.get();
        // 全額退款：作廢發票
        if (event.getRefundAmount().compareTo(invoice.getTotalAmount()) >= 0) {
            voidInvoice(invoice.getId(), "全額退款");
        } else {
            // 部分退款：開折讓單
            createAllowance(invoice, event.getRefundId(), event.getRefundAmount());
        }
    }

    // ========================================
    // 手動開立發票 / Manual invoice issuance
    // ========================================
    @Transactional
    public InvoiceResponse issue(IssueInvoiceRequest req) {
        Invoice invoice = buildInvoice(req.orderId(), req.storeId(), null, req);
        uploadAndSave(invoice);
        return InvoiceResponse.from(invoice);
    }

    // ========================================
    // 依訂單查詢發票 / Query invoice by order
    // ========================================
    @Transactional(readOnly = true)
    public Optional<InvoiceResponse> getByOrder(UUID orderId) {
        return invoiceRepository.findByOrderId(orderId).map(InvoiceResponse::from);
    }

    // ========================================
    // 查詢門店發票列表 / Query store invoice list
    // ========================================
    @Transactional(readOnly = true)
    public List<InvoiceResponse> listByStore(UUID storeId, LocalDateTime from, LocalDateTime to) {
        return invoiceRepository.findByStoreAndDateRange(storeId, from, to)
                .stream().map(InvoiceResponse::from).toList();
    }

    // ========================================
    // 作廢發票 / Void invoice
    // ========================================
    @Transactional
    public InvoiceResponse voidInvoice(UUID invoiceId, String reason) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + invoiceId));
        if (invoice.getStatus() != Invoice.InvoiceStatus.ISSUED) {
            throw new IllegalStateException("Invoice is not in ISSUED status: " + invoiceId);
        }
        TurnkeyClient.TurnkeyResult result = turnkeyClient.voidInvoice(invoice);
        invoice.setStatus(Invoice.InvoiceStatus.VOIDED);
        invoice.setVoidAt(LocalDateTime.now());
        invoice.setVoidReason(reason);
        if (result.success()) {
            invoice.setUploadStatus(Invoice.UploadStatus.SUCCESS);
        } else {
            invoice.setUploadStatus(Invoice.UploadStatus.FAILED);
            invoice.setUploadResp(result.errorMessage());
        }
        invoiceRepository.save(invoice);
        return InvoiceResponse.from(invoice);
    }

    // =====================================
    // 私有輔助方法 / Private helpers
    // =====================================

    private void issueForOrder(UUID orderId, UUID storeId, BigDecimal grandTotal) {
        Invoice invoice = buildInvoiceFromTotal(orderId, storeId, grandTotal);
        uploadAndSave(invoice);
    }

    private Invoice buildInvoice(UUID orderId, UUID storeId, BigDecimal grandTotal, IssueInvoiceRequest req) {
        BigDecimal total = grandTotal != null ? grandTotal : BigDecimal.ZERO;
        Invoice invoice = buildInvoiceFromTotal(orderId, storeId, total);
        if (req != null) {
            invoice.setInvoiceType(req.invoiceType() != null ? req.invoiceType() : Invoice.InvoiceType.B2C);
            invoice.setBuyerId(req.buyerId());
            invoice.setBuyerName(req.buyerName());
            invoice.setBuyerEmail(req.buyerEmail());
            invoice.setCarrierType(req.carrierType());
            invoice.setDonateCode(req.donateCode());
        }
        return invoice;
    }

    private Invoice buildInvoiceFromTotal(UUID orderId, UUID storeId, BigDecimal grandTotal) {
        // 含稅總額拆分：稅前 = 總額 / 1.05，稅額 = 總額 - 稅前
        BigDecimal salesAmount = grandTotal.divide(BigDecimal.ONE.add(TAX_RATE), 0, RoundingMode.HALF_UP);
        BigDecimal taxAmount = grandTotal.subtract(salesAmount);

        Invoice invoice = new Invoice();
        invoice.setStoreId(storeId);
        invoice.setOrderId(orderId);
        invoice.setSellerId(defaultSellerId);
        invoice.setSellerName(defaultSellerName);
        invoice.setSalesAmount(salesAmount);
        invoice.setTaxAmount(taxAmount);
        invoice.setTotalAmount(grandTotal);
        invoice.setInvoiceType(Invoice.InvoiceType.B2C);
        return invoice;
    }

    private void uploadAndSave(Invoice invoice) {
        // 分配字軌號碼
        try {
            InvoiceTrack track = trackService.getTrackForStore(invoice.getStoreId());
            String fullNo = trackService.allocateNextNumber(invoice.getStoreId());
            invoice.setTrack(track);
            invoice.setInvoiceNo(fullNo.substring(3)); // 去掉 "XX-" 前綴
            invoice.setFullInvoiceNo(fullNo);
        } catch (Exception e) {
            log.warn("Could not allocate invoice track (no tracks registered?): {}", e.getMessage());
        }

        Invoice saved = invoiceRepository.save(invoice);

        // 上傳財政部 Turnkey
        TurnkeyClient.TurnkeyResult result = turnkeyClient.uploadInvoice(saved);
        if (result.success()) {
            saved.setUploadStatus(Invoice.UploadStatus.SUCCESS);
            saved.setUploadAt(LocalDateTime.now());
            saved.setUploadResp(result.rawResponse());
        } else {
            saved.setUploadStatus(Invoice.UploadStatus.FAILED);
            saved.setUploadResp(result.errorMessage());
            log.error("Turnkey upload failed for invoice {}: {}", saved.getId(), result.errorMessage());
        }
        invoiceRepository.save(saved);

        eventPublisher.publishEvent(new InvoiceIssuedEvent(
                this, saved.getId(), saved.getOrderId(), saved.getStoreId(),
                saved.getFullInvoiceNo(), saved.getTotalAmount()
        ));
    }

    private void createAllowance(Invoice invoice, UUID refundId, BigDecimal refundAmount) {
        BigDecimal salesAmt = refundAmount.divide(BigDecimal.ONE.add(TAX_RATE), 0, RoundingMode.HALF_UP);
        BigDecimal taxAmt = refundAmount.subtract(salesAmt);

        InvoiceAllowance allowance = new InvoiceAllowance();
        allowance.setStoreId(invoice.getStoreId());
        allowance.setInvoice(invoice);
        allowance.setRefundId(refundId);
        allowance.setSalesAmount(salesAmt);
        allowance.setTaxAmount(taxAmt);
        allowance.setTotalAmount(refundAmount);

        InvoiceAllowance saved = allowanceRepository.save(allowance);

        TurnkeyClient.TurnkeyResult result = turnkeyClient.uploadAllowance(saved);
        if (result.success()) {
            saved.setUploadStatus(InvoiceAllowance.UploadStatus.SUCCESS);
            saved.setUploadAt(LocalDateTime.now());
            saved.setUploadResp(result.rawResponse());
            saved.setAllowanceNo(result.referenceNo());
        } else {
            saved.setUploadStatus(InvoiceAllowance.UploadStatus.FAILED);
            saved.setUploadResp(result.errorMessage());
        }
        allowanceRepository.save(saved);
    }
}
