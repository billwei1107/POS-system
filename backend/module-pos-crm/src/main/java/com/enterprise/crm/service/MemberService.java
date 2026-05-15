/**
 * @file MemberService.java
 * @description POS 會員服務 / POS member service
 * @description_en Handles member lookup, quick registration and loyalty point accrual
 * @description_zh 處理會員查詢、快速註冊與消費累點
 */
package com.enterprise.crm.service;

import com.enterprise.common.exception.BusinessException;
import com.enterprise.common.exception.ResourceNotFoundException;
import com.enterprise.core.event.OrderCompletedEvent;
import com.enterprise.core.event.RefundCompletedEvent;
import com.enterprise.crm.dto.MemberRequest;
import com.enterprise.crm.dto.MemberResponse;
import com.enterprise.crm.dto.PointAdjustmentRequest;
import com.enterprise.crm.dto.PointLedgerResponse;
import com.enterprise.crm.dto.PointRedemptionRequest;
import com.enterprise.crm.entity.Member;
import com.enterprise.crm.entity.PointLedger;
import com.enterprise.crm.repository.MemberRepository;
import com.enterprise.crm.repository.PointLedgerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private static final BigDecimal POINT_SPEND_UNIT = new BigDecimal("10.00");

    private final MemberRepository memberRepository;
    private final PointLedgerRepository pointLedgerRepository;

    // ========================================
    // 查詢 / Query
    // ========================================
    @Transactional(readOnly = true)
    public List<MemberResponse> search(String query, int limit) {
        String normalizedQuery = normalizeQuery(query);
        if (normalizedQuery.isBlank()) {
            return List.of();
        }
        int safeLimit = Math.max(1, Math.min(limit, 20));
        return memberRepository.search(normalizedQuery, PageRequest.of(0, safeLimit)).stream()
                .map(MemberResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public MemberResponse findById(UUID id) {
        return MemberResponse.from(getOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<PointLedgerResponse> listPointLedgers(UUID memberId) {
        getOrThrow(memberId);
        return pointLedgerRepository.findByMemberIdOrderByOccurredAtDesc(memberId).stream()
                .map(PointLedgerResponse::from)
                .toList();
    }

    // ========================================
    // 建立 / Create
    // ========================================
    @Transactional
    public MemberResponse create(MemberRequest request) {
        String phone = normalizePhone(request.phone());
        if (memberRepository.existsByPhone(phone)) {
            throw new BusinessException(409, "Member phone already exists: " + phone);
        }
        String memberNo = request.memberNo() != null && !request.memberNo().isBlank()
                ? request.memberNo().trim()
                : nextMemberNo(phone);
        if (memberRepository.existsByMemberNo(memberNo)) {
            throw new BusinessException(409, "Member number already exists: " + memberNo);
        }

        Member member = new Member();
        member.setMemberNo(memberNo);
        member.setName(request.name().trim());
        member.setPhone(phone);
        member.setEmail(trimToNull(request.email()));
        member.setBirthday(request.birthday());
        member.setCardNo(trimToNull(request.cardNo()));
        member.setBarcode(trimToNull(request.barcode()));
        member.setTier(request.tier() != null ? request.tier() : Member.Tier.BRONZE);
        member.setDiscountPercent(request.discountPercent() != null
                ? request.discountPercent().setScale(2, RoundingMode.HALF_UP)
                : defaultDiscount(member.getTier()));
        member.setPointsBalance(0);
        member.setStoredValueBalance(BigDecimal.ZERO);
        member.setAnnualSpend(BigDecimal.ZERO);
        member.setActive(true);
        return MemberResponse.from(memberRepository.save(member));
    }

    // ========================================
    // 點數異動 / Point mutations
    // ========================================
    @Transactional
    public PointLedgerResponse redeemPoints(UUID memberId, PointRedemptionRequest request) {
        Member member = getActiveOrThrow(memberId);
        int points = request.points();
        return PointLedgerResponse.from(applyPointDelta(
                member,
                -points,
                PointLedger.Reason.REDEEM,
                request.orderId(),
                request.orderId(),
                request.orderId() != null ? "ORDER_REDEEM" : null,
                request.note() != null && !request.note().isBlank() ? request.note().trim() : "POS point redemption"
        ));
    }

    @Transactional
    public PointLedgerResponse adjustPoints(UUID memberId, PointAdjustmentRequest request) {
        Member member = getActiveOrThrow(memberId);
        int pointsDelta = request.pointsDelta();
        if (pointsDelta == 0) {
            throw new BusinessException(400, "Point adjustment delta cannot be zero");
        }
        return PointLedgerResponse.from(applyPointDelta(
                member,
                pointsDelta,
                PointLedger.Reason.MANUAL_ADJUST,
                null,
                null,
                null,
                request.note() != null && !request.note().isBlank() ? request.note().trim() : "Manual point adjustment"
        ));
    }

    // ========================================
    // 訂單累點 / Order point accrual
    // ========================================
    @EventListener
    @Transactional
    public void handleOrderCompleted(OrderCompletedEvent event) {
        if (event.getMemberId() == null || event.getGrandTotal() == null) {
            return;
        }
        if (pointLedgerRepository.existsByReferenceIdAndReason(event.getOrderId(), PointLedger.Reason.ORDER_EARN)
                || pointLedgerRepository.existsByOrderIdAndReason(event.getOrderId(), PointLedger.Reason.ORDER_EARN)) {
            log.debug("CRM points already accrued for order={}", event.getOrderId());
            return;
        }
        memberRepository.findById(event.getMemberId())
                .filter(member -> Boolean.TRUE.equals(member.getActive()))
                .ifPresent(member -> accrueOrderPoints(member, event));
    }

    private void accrueOrderPoints(Member member, OrderCompletedEvent event) {
        int points = event.getGrandTotal()
                .max(BigDecimal.ZERO)
                .divide(POINT_SPEND_UNIT, 0, RoundingMode.DOWN)
                .intValue();
        if (points <= 0) {
            return;
        }
        int balanceAfter = member.getPointsBalance() + points;
        member.setPointsBalance(balanceAfter);
        member.setAnnualSpend(member.getAnnualSpend().add(event.getGrandTotal()).setScale(2, RoundingMode.HALF_UP));
        memberRepository.save(member);

        PointLedger ledger = new PointLedger();
        ledger.setMemberId(member.getId());
        ledger.setOrderId(event.getOrderId());
        ledger.setReferenceId(event.getOrderId());
        ledger.setReferenceType("ORDER");
        ledger.setPointsDelta(points);
        ledger.setBalanceAfter(balanceAfter);
        ledger.setReason(PointLedger.Reason.ORDER_EARN);
        ledger.setNote("Order " + event.getOrderNo());
        ledger.setOccurredAt(LocalDateTime.now());
        pointLedgerRepository.save(ledger);
    }

    // ========================================
    // 退款回沖 / Refund reversal
    // ========================================
    @EventListener
    @Transactional
    public void handleRefundCompleted(RefundCompletedEvent event) {
        if (event.getMemberId() == null || event.getRefundId() == null || event.getOrderId() == null) {
            return;
        }
        if (pointLedgerRepository.existsByReferenceIdAndReason(event.getRefundId(), PointLedger.Reason.REFUND_REVERSE)) {
            log.debug("CRM points already reversed for refund={}", event.getRefundId());
            return;
        }

        PointLedger earnedLedger = pointLedgerRepository
                .findFirstByOrderIdAndReason(event.getOrderId(), PointLedger.Reason.ORDER_EARN)
                .orElse(null);
        if (earnedLedger == null || earnedLedger.getPointsDelta() == null || earnedLedger.getPointsDelta() <= 0) {
            return;
        }

        memberRepository.findById(event.getMemberId()).ifPresent(member -> {
            int pointsToReverse = calculateRefundReversePoints(event, earnedLedger.getPointsDelta());
            if (pointsToReverse <= 0) {
                return;
            }
            applyPointDelta(
                    member,
                    -pointsToReverse,
                    PointLedger.Reason.REFUND_REVERSE,
                    event.getOrderId(),
                    event.getRefundId(),
                    "REFUND",
                    "Refund " + event.getRefundId(),
                    true
            );
        });
    }

    private int calculateRefundReversePoints(RefundCompletedEvent event, int earnedPoints) {
        BigDecimal completedRefundTotal = event.getCompletedRefundTotal() != null
                ? event.getCompletedRefundTotal()
                : event.getRefundAmount();
        if (completedRefundTotal == null) {
            return 0;
        }
        int targetReversePoints = completedRefundTotal
                .max(BigDecimal.ZERO)
                .divide(POINT_SPEND_UNIT, 0, RoundingMode.DOWN)
                .intValue();
        targetReversePoints = Math.min(targetReversePoints, earnedPoints);

        int alreadyReversed = pointLedgerRepository
                .findByOrderIdAndReason(event.getOrderId(), PointLedger.Reason.REFUND_REVERSE)
                .stream()
                .map(PointLedger::getPointsDelta)
                .filter(delta -> delta != null && delta < 0)
                .mapToInt(Math::abs)
                .sum();
        return Math.max(0, targetReversePoints - alreadyReversed);
    }

    private PointLedger applyPointDelta(
            Member member,
            int pointsDelta,
            PointLedger.Reason reason,
            UUID orderId,
            UUID referenceId,
            String referenceType,
            String note) {
        return applyPointDelta(member, pointsDelta, reason, orderId, referenceId, referenceType, note, false);
    }

    private PointLedger applyPointDelta(
            Member member,
            int pointsDelta,
            PointLedger.Reason reason,
            UUID orderId,
            UUID referenceId,
            String referenceType,
            String note,
            boolean allowNegativeBalance) {
        int currentBalance = member.getPointsBalance() == null ? 0 : member.getPointsBalance();
        int balanceAfter = currentBalance + pointsDelta;
        if (balanceAfter < 0 && !allowNegativeBalance) {
            throw new BusinessException(400, "Insufficient member points");
        }

        member.setPointsBalance(balanceAfter);
        memberRepository.save(member);

        PointLedger ledger = new PointLedger();
        ledger.setMemberId(member.getId());
        ledger.setOrderId(orderId);
        ledger.setReferenceId(referenceId);
        ledger.setReferenceType(referenceType);
        ledger.setPointsDelta(pointsDelta);
        ledger.setBalanceAfter(balanceAfter);
        ledger.setReason(reason);
        ledger.setNote(note);
        ledger.setOccurredAt(LocalDateTime.now());
        return pointLedgerRepository.save(ledger);
    }

    private Member getOrThrow(UUID id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found: " + id));
    }

    private Member getActiveOrThrow(UUID id) {
        Member member = getOrThrow(id);
        if (!Boolean.TRUE.equals(member.getActive())) {
            throw new BusinessException(400, "Member is inactive: " + id);
        }
        return member;
    }

    private String normalizeQuery(String query) {
        return query == null ? "" : query.trim();
    }

    private String normalizePhone(String phone) {
        return phone == null ? "" : phone.replaceAll("[^0-9+]", "");
    }

    private String nextMemberNo(String phone) {
        String suffix = phone.length() >= 4 ? phone.substring(phone.length() - 4) : String.valueOf(System.nanoTime()).substring(8);
        return "M-" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyMMdd")) + "-" + suffix;
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private BigDecimal defaultDiscount(Member.Tier tier) {
        return switch (tier) {
            case PLATINUM -> new BigDecimal("12.00");
            case GOLD -> new BigDecimal("10.00");
            case SILVER -> new BigDecimal("5.00");
            case BRONZE -> BigDecimal.ZERO;
        };
    }
}
