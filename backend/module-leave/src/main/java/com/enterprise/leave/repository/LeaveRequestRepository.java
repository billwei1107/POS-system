/**
 * @file LeaveRequestRepository.java
 * @description 請假申請資料存取層 / Leave request repository
 * @description_en JPA repository for leave request queries by employee, status, and date range
 * @description_zh 請假申請依員工、狀態、日期範圍查詢的資料存取層
 */
package com.enterprise.leave.repository;

import com.enterprise.leave.entity.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, UUID> {

    @Query("SELECT r FROM LeaveRequest r WHERE r.employeeId = :employeeId AND r.deletedAt IS NULL ORDER BY r.createdAt DESC")
    List<LeaveRequest> findByEmployeeId(@Param("employeeId") UUID employeeId);

    @Query("SELECT r FROM LeaveRequest r WHERE r.employeeId = :employeeId AND r.status = :status AND r.deletedAt IS NULL ORDER BY r.createdAt DESC")
    List<LeaveRequest> findByEmployeeIdAndStatus(@Param("employeeId") UUID employeeId, @Param("status") LeaveRequest.LeaveStatus status);

    @Query("SELECT r FROM LeaveRequest r WHERE r.workflowInstanceId = :instanceId AND r.deletedAt IS NULL")
    Optional<LeaveRequest> findByWorkflowInstanceId(@Param("instanceId") UUID instanceId);

    // ========================================
    // 部門日曆查詢（依日期區間）/ Calendar query by date range
    // ========================================
    @Query("SELECT r FROM LeaveRequest r WHERE r.startDate <= :endDate AND r.endDate >= :startDate AND r.status = 'APPROVED' AND r.deletedAt IS NULL ORDER BY r.startDate")
    List<LeaveRequest> findApprovedInDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
