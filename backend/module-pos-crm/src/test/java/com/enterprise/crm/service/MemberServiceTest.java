/**
 * @file MemberServiceTest.java
 * @description 會員服務測試 / Member service tests
 * @description_en Verifies quick registration and order point accrual
 * @description_zh 驗證會員快速註冊與訂單完成累點
 */
package com.enterprise.crm.service;

import com.enterprise.common.exception.BusinessException;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PointLedgerRepository pointLedgerRepository;

    private MemberService memberService;

    @BeforeEach
    void setUp() {
        memberService = new MemberService(memberRepository, pointLedgerRepository);
    }

    // ========================================
    // 快速註冊 / Quick registration
    // ========================================
    @Test
    void create_goldMember_usesTierDefaultDiscount() {
        MemberRequest request = new MemberRequest(
                null, "林小美", "0912-345-678", null, null, null, null,
                Member.Tier.GOLD, null
        );
        when(memberRepository.existsByPhone("0912345678")).thenReturn(false);
        when(memberRepository.existsByMemberNo(org.mockito.ArgumentMatchers.anyString())).thenReturn(false);
        when(memberRepository.save(org.mockito.ArgumentMatchers.any(Member.class))).thenAnswer(invocation -> {
            Member member = invocation.getArgument(0);
            member.setId(UUID.randomUUID());
            return member;
        });

        MemberResponse response = memberService.create(request);

        assertThat(response.phone()).isEqualTo("0912345678");
        assertThat(response.tier()).isEqualTo(Member.Tier.GOLD);
        assertThat(response.discountPercent()).isEqualByComparingTo("10.00");
        assertThat(response.pointsBalance()).isZero();
    }

    @Test
    void create_duplicatePhone_throwsBusinessException() {
        MemberRequest request = new MemberRequest(
                null, "林小美", "0912-345-678", null, null, null, null,
                Member.Tier.BRONZE, null
        );
        when(memberRepository.existsByPhone("0912345678")).thenReturn(true);

        assertThatThrownBy(() -> memberService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("phone already exists");
    }

    // ========================================
    // 訂單累點 / Order point accrual
    // ========================================
    @Test
    void handleOrderCompleted_memberOrder_accruesOnePointPerTenDollars() {
        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        Member member = member(memberId, 5, "100.00");
        OrderCompletedEvent event = new OrderCompletedEvent(
                this, orderId, UUID.randomUUID(), "ORD-001",
                memberId, new BigDecimal("126.00")
        );

        when(pointLedgerRepository.existsByOrderIdAndReason(orderId, PointLedger.Reason.ORDER_EARN))
                .thenReturn(false);
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        memberService.handleOrderCompleted(event);

        assertThat(member.getPointsBalance()).isEqualTo(17);
        assertThat(member.getAnnualSpend()).isEqualByComparingTo("226.00");

        ArgumentCaptor<PointLedger> ledgerCaptor = ArgumentCaptor.forClass(PointLedger.class);
        verify(pointLedgerRepository).save(ledgerCaptor.capture());
        assertThat(ledgerCaptor.getValue().getPointsDelta()).isEqualTo(12);
        assertThat(ledgerCaptor.getValue().getBalanceAfter()).isEqualTo(17);
        assertThat(ledgerCaptor.getValue().getReason()).isEqualTo(PointLedger.Reason.ORDER_EARN);
    }

    @Test
    void handleOrderCompleted_duplicateOrder_skipsAccrual() {
        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        OrderCompletedEvent event = new OrderCompletedEvent(
                this, orderId, UUID.randomUUID(), "ORD-001",
                memberId, new BigDecimal("126.00")
        );

        when(pointLedgerRepository.existsByOrderIdAndReason(orderId, PointLedger.Reason.ORDER_EARN))
                .thenReturn(true);

        memberService.handleOrderCompleted(event);

        verify(memberRepository, never()).findById(memberId);
        verify(pointLedgerRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    // ========================================
    // 點數異動 / Point mutations
    // ========================================
    @Test
    void redeemPoints_enoughBalance_debitsPointsAndWritesLedger() {
        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        Member member = member(memberId, 120, "300.00");
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(pointLedgerRepository.save(org.mockito.ArgumentMatchers.any(PointLedger.class))).thenAnswer(invocation -> {
            PointLedger ledger = invocation.getArgument(0);
            ledger.setId(UUID.randomUUID());
            return ledger;
        });

        PointLedgerResponse response = memberService.redeemPoints(
                memberId,
                new PointRedemptionRequest(50, orderId, "折抵現金")
        );

        assertThat(member.getPointsBalance()).isEqualTo(70);
        assertThat(response.pointsDelta()).isEqualTo(-50);
        assertThat(response.balanceAfter()).isEqualTo(70);
        assertThat(response.reason()).isEqualTo(PointLedger.Reason.REDEEM);
        assertThat(response.orderId()).isEqualTo(orderId);
        verify(memberRepository).save(member);
    }

    @Test
    void redeemPoints_insufficientBalance_throwsBusinessException() {
        UUID memberId = UUID.randomUUID();
        Member member = member(memberId, 20, "300.00");
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        assertThatThrownBy(() -> memberService.redeemPoints(
                memberId,
                new PointRedemptionRequest(50, null, null)
        ))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Insufficient");

        assertThat(member.getPointsBalance()).isEqualTo(20);
        verify(pointLedgerRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void adjustPoints_positiveDelta_increasesBalanceAndWritesLedger() {
        UUID memberId = UUID.randomUUID();
        Member member = member(memberId, 20, "300.00");
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(pointLedgerRepository.save(org.mockito.ArgumentMatchers.any(PointLedger.class))).thenAnswer(invocation -> {
            PointLedger ledger = invocation.getArgument(0);
            ledger.setId(UUID.randomUUID());
            return ledger;
        });

        PointLedgerResponse response = memberService.adjustPoints(
                memberId,
                new PointAdjustmentRequest(30, "活動補點")
        );

        assertThat(member.getPointsBalance()).isEqualTo(50);
        assertThat(response.pointsDelta()).isEqualTo(30);
        assertThat(response.balanceAfter()).isEqualTo(50);
        assertThat(response.reason()).isEqualTo(PointLedger.Reason.MANUAL_ADJUST);
    }

    @Test
    void adjustPoints_zeroDelta_throwsBusinessException() {
        UUID memberId = UUID.randomUUID();
        Member member = member(memberId, 20, "300.00");
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        assertThatThrownBy(() -> memberService.adjustPoints(
                memberId,
                new PointAdjustmentRequest(0, "no-op")
        ))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("cannot be zero");

        verify(pointLedgerRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    // ========================================
    // 退款回沖 / Refund reversal
    // ========================================
    @Test
    void handleRefundCompleted_memberOrder_reversesEarnedPointsByCompletedRefundTotal() {
        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID refundId = UUID.randomUUID();
        Member member = member(memberId, 17, "126.00");
        PointLedger earned = earnedLedger(memberId, orderId, 12);
        RefundCompletedEvent event = new RefundCompletedEvent(
                this, refundId, orderId, UUID.randomUUID(), memberId,
                new BigDecimal("50.00"), new BigDecimal("126.00"), new BigDecimal("50.00"), "CASH"
        );

        when(pointLedgerRepository.existsByReferenceIdAndReason(refundId, PointLedger.Reason.REFUND_REVERSE))
                .thenReturn(false);
        when(pointLedgerRepository.findFirstByOrderIdAndReason(orderId, PointLedger.Reason.ORDER_EARN))
                .thenReturn(Optional.of(earned));
        when(pointLedgerRepository.findByOrderIdAndReason(orderId, PointLedger.Reason.REFUND_REVERSE))
                .thenReturn(List.of());
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(pointLedgerRepository.save(org.mockito.ArgumentMatchers.any(PointLedger.class))).thenAnswer(invocation -> {
            PointLedger ledger = invocation.getArgument(0);
            ledger.setId(UUID.randomUUID());
            return ledger;
        });

        memberService.handleRefundCompleted(event);

        assertThat(member.getPointsBalance()).isEqualTo(12);
        ArgumentCaptor<PointLedger> ledgerCaptor = ArgumentCaptor.forClass(PointLedger.class);
        verify(pointLedgerRepository).save(ledgerCaptor.capture());
        PointLedger ledger = ledgerCaptor.getValue();
        assertThat(ledger.getPointsDelta()).isEqualTo(-5);
        assertThat(ledger.getBalanceAfter()).isEqualTo(12);
        assertThat(ledger.getReason()).isEqualTo(PointLedger.Reason.REFUND_REVERSE);
        assertThat(ledger.getOrderId()).isEqualTo(orderId);
        assertThat(ledger.getReferenceId()).isEqualTo(refundId);
        assertThat(ledger.getReferenceType()).isEqualTo("REFUND");
    }

    @Test
    void handleRefundCompleted_secondPartialRefund_reversesOnlyRemainingTargetDelta() {
        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID refundId = UUID.randomUUID();
        Member member = member(memberId, 12, "126.00");
        PointLedger earned = earnedLedger(memberId, orderId, 12);
        PointLedger previousReverse = refundReverseLedger(memberId, orderId, -5);
        RefundCompletedEvent event = new RefundCompletedEvent(
                this, refundId, orderId, UUID.randomUUID(), memberId,
                new BigDecimal("50.00"), new BigDecimal("126.00"), new BigDecimal("100.00"), "CASH"
        );

        when(pointLedgerRepository.existsByReferenceIdAndReason(refundId, PointLedger.Reason.REFUND_REVERSE))
                .thenReturn(false);
        when(pointLedgerRepository.findFirstByOrderIdAndReason(orderId, PointLedger.Reason.ORDER_EARN))
                .thenReturn(Optional.of(earned));
        when(pointLedgerRepository.findByOrderIdAndReason(orderId, PointLedger.Reason.REFUND_REVERSE))
                .thenReturn(List.of(previousReverse));
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(pointLedgerRepository.save(org.mockito.ArgumentMatchers.any(PointLedger.class))).thenAnswer(invocation -> {
            PointLedger ledger = invocation.getArgument(0);
            ledger.setId(UUID.randomUUID());
            return ledger;
        });

        memberService.handleRefundCompleted(event);

        assertThat(member.getPointsBalance()).isEqualTo(7);
        ArgumentCaptor<PointLedger> ledgerCaptor = ArgumentCaptor.forClass(PointLedger.class);
        verify(pointLedgerRepository).save(ledgerCaptor.capture());
        assertThat(ledgerCaptor.getValue().getPointsDelta()).isEqualTo(-5);
    }

    @Test
    void handleRefundCompleted_duplicateRefund_skipsReversal() {
        UUID refundId = UUID.randomUUID();
        RefundCompletedEvent event = new RefundCompletedEvent(
                this, refundId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                new BigDecimal("50.00"), new BigDecimal("126.00"), new BigDecimal("50.00"), "CASH"
        );
        when(pointLedgerRepository.existsByReferenceIdAndReason(refundId, PointLedger.Reason.REFUND_REVERSE))
                .thenReturn(true);

        memberService.handleRefundCompleted(event);

        verify(memberRepository, never()).findById(org.mockito.ArgumentMatchers.any());
        verify(pointLedgerRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    private Member member(UUID id, int pointsBalance, String annualSpend) {
        Member member = new Member();
        member.setId(id);
        member.setMemberNo("M-001");
        member.setName("Test Member");
        member.setPhone("0912000001");
        member.setTier(Member.Tier.BRONZE);
        member.setDiscountPercent(BigDecimal.ZERO);
        member.setPointsBalance(pointsBalance);
        member.setStoredValueBalance(BigDecimal.ZERO);
        member.setAnnualSpend(new BigDecimal(annualSpend));
        member.setActive(true);
        return member;
    }

    private PointLedger earnedLedger(UUID memberId, UUID orderId, int points) {
        PointLedger ledger = new PointLedger();
        ledger.setId(UUID.randomUUID());
        ledger.setMemberId(memberId);
        ledger.setOrderId(orderId);
        ledger.setReferenceId(orderId);
        ledger.setReferenceType("ORDER");
        ledger.setPointsDelta(points);
        ledger.setBalanceAfter(points);
        ledger.setReason(PointLedger.Reason.ORDER_EARN);
        return ledger;
    }

    private PointLedger refundReverseLedger(UUID memberId, UUID orderId, int pointsDelta) {
        PointLedger ledger = new PointLedger();
        ledger.setId(UUID.randomUUID());
        ledger.setMemberId(memberId);
        ledger.setOrderId(orderId);
        ledger.setReferenceId(UUID.randomUUID());
        ledger.setReferenceType("REFUND");
        ledger.setPointsDelta(pointsDelta);
        ledger.setBalanceAfter(0);
        ledger.setReason(PointLedger.Reason.REFUND_REVERSE);
        return ledger;
    }
}
