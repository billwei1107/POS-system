/**
 * @file LeaveBalanceRepository.java
 * @description 員工餘假資料存取層 / Leave balance repository
 * @description_en JPA repository for querying and updating employee leave balances
 * @description_zh 員工餘假查詢與更新的資料存取層
 */
package com.enterprise.leave.repository;

import com.enterprise.leave.entity.LeaveBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, UUID> {

    @Query("SELECT b FROM LeaveBalance b WHERE b.employeeId = :employeeId AND b.year = :year AND b.deletedAt IS NULL")
    List<LeaveBalance> findByEmployeeIdAndYear(@Param("employeeId") UUID employeeId, @Param("year") int year);

    @Query("SELECT b FROM LeaveBalance b WHERE b.employeeId = :employeeId AND b.leaveTypeId = :leaveTypeId AND b.year = :year AND b.deletedAt IS NULL")
    Optional<LeaveBalance> findByEmployeeIdAndLeaveTypeIdAndYear(
            @Param("employeeId") UUID employeeId,
            @Param("leaveTypeId") UUID leaveTypeId,
            @Param("year") int year);
}
