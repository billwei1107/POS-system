/**
 * @file StaffShiftRepository.java
 * @description 班次 Repository / Staff shift repository
 * @description_en JPA repository for staff shift records with store and date range queries
 * @description_zh 班次 JPA 儲存庫，支援門店/員工/時段查詢
 */
package com.enterprise.staff.repository;

import com.enterprise.staff.entity.StaffShift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StaffShiftRepository extends JpaRepository<StaffShift, UUID> {

    // ========================================
    // 查詢門店開放班次 / Find open shifts by store
    // ========================================
    @Query("SELECT s FROM StaffShift s WHERE s.storeId = :storeId AND s.status = 'OPEN' AND s.deletedAt IS NULL")
    List<StaffShift> findOpenByStoreId(@Param("storeId") UUID storeId);

    // ========================================
    // 查詢員工開放班次 / Find employee open shift
    // ========================================
    @Query("SELECT s FROM StaffShift s WHERE s.employeeId = :employeeId AND s.status = 'OPEN' AND s.deletedAt IS NULL")
    Optional<StaffShift> findOpenByEmployeeId(@Param("employeeId") UUID employeeId);

    // ========================================
    // 查詢終端機開放班次 / Find terminal open shift
    // ========================================
    @Query("SELECT s FROM StaffShift s WHERE s.storeId = :storeId AND s.terminalId = :terminalId AND s.status = 'OPEN' AND s.deletedAt IS NULL")
    Optional<StaffShift> findOpenByStoreIdAndTerminalId(@Param("storeId") UUID storeId,
                                                        @Param("terminalId") UUID terminalId);

    // ========================================
    // 依時段查詢班次 / Find shifts in date range
    // ========================================
    @Query("SELECT s FROM StaffShift s WHERE s.storeId = :storeId AND s.openedAt >= :from AND s.openedAt < :to AND s.deletedAt IS NULL ORDER BY s.openedAt")
    List<StaffShift> findByStoreIdAndDateRange(@Param("storeId") UUID storeId,
                                               @Param("from") Instant from,
                                               @Param("to") Instant to);
}
