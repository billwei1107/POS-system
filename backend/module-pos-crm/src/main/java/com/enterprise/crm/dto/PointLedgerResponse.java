/**
 * @file PointLedgerResponse.java
 * @description 會員點數帳回應 DTO / Member point ledger response DTO
 */
package com.enterprise.crm.dto;

import com.enterprise.crm.entity.PointLedger;

import java.time.LocalDateTime;
import java.util.UUID;

public record PointLedgerResponse(
        UUID id,
        UUID memberId,
        UUID orderId,
        UUID referenceId,
        String referenceType,
        Integer pointsDelta,
        Integer balanceAfter,
        PointLedger.Reason reason,
        String note,
        LocalDateTime occurredAt
) {
    public static PointLedgerResponse from(PointLedger ledger) {
        return new PointLedgerResponse(
                ledger.getId(),
                ledger.getMemberId(),
                ledger.getOrderId(),
                ledger.getReferenceId(),
                ledger.getReferenceType(),
                ledger.getPointsDelta(),
                ledger.getBalanceAfter(),
                ledger.getReason(),
                ledger.getNote(),
                ledger.getOccurredAt()
        );
    }
}
