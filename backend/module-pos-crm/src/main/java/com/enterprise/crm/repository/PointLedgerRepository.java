/**
 * @file PointLedgerRepository.java
 * @description 會員點數帳資料存取層 / Member point ledger repository
 */
package com.enterprise.crm.repository;

import com.enterprise.crm.entity.PointLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PointLedgerRepository extends JpaRepository<PointLedger, UUID> {

    boolean existsByOrderIdAndReason(UUID orderId, PointLedger.Reason reason);

    boolean existsByReferenceIdAndReason(UUID referenceId, PointLedger.Reason reason);

    Optional<PointLedger> findFirstByOrderIdAndReason(UUID orderId, PointLedger.Reason reason);

    List<PointLedger> findByOrderIdAndReason(UUID orderId, PointLedger.Reason reason);

    List<PointLedger> findByMemberIdOrderByOccurredAtDesc(UUID memberId);
}
