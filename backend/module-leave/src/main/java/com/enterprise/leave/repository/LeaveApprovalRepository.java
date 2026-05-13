/**
 * @file LeaveApprovalRepository.java
 * @description 請假審批記錄資料存取層 / Leave approval repository
 * @description_en JPA repository for leave approval audit records
 * @description_zh 請假審批記錄的資料存取層
 */
package com.enterprise.leave.repository;

import com.enterprise.leave.entity.LeaveApproval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface LeaveApprovalRepository extends JpaRepository<LeaveApproval, UUID> {

    @Query("SELECT a FROM LeaveApproval a WHERE a.requestId = :requestId ORDER BY a.operatedAt")
    List<LeaveApproval> findByRequestId(@Param("requestId") UUID requestId);
}
