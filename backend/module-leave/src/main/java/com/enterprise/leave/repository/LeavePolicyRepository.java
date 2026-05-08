/**
 * @file LeavePolicyRepository.java
 * @description 假別政策資料存取層 / Leave policy repository
 * @description_en JPA repository for querying quota by leave type and service years
 * @description_zh 依假別與年資查詢額度的資料存取層
 */
package com.enterprise.leave.repository;

import com.enterprise.leave.entity.LeavePolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface LeavePolicyRepository extends JpaRepository<LeavePolicy, UUID> {

    @Query("SELECT p FROM LeavePolicy p WHERE p.leaveTypeId = :leaveTypeId AND p.deletedAt IS NULL ORDER BY p.minServiceYears")
    List<LeavePolicy> findByLeaveTypeId(@Param("leaveTypeId") UUID leaveTypeId);
}
