/**
 * @file StaffScheduleRepository.java
 * @description 排班計劃 Repository / Staff schedule repository
 * @description_en JPA repository for staff schedule plans with date queries
 * @description_zh 排班計劃 JPA 儲存庫
 */
package com.enterprise.staff.repository;

import com.enterprise.staff.entity.StaffSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface StaffScheduleRepository extends JpaRepository<StaffSchedule, UUID> {

    @Query("SELECT s FROM StaffSchedule s WHERE s.storeId = :storeId AND s.workDate = :date AND s.deletedAt IS NULL ORDER BY s.plannedStart")
    List<StaffSchedule> findByStoreIdAndWorkDate(@Param("storeId") UUID storeId, @Param("date") LocalDate date);

    @Query("SELECT s FROM StaffSchedule s WHERE s.storeId = :storeId AND s.workDate BETWEEN :from AND :to AND s.deletedAt IS NULL ORDER BY s.workDate, s.plannedStart")
    List<StaffSchedule> findByStoreIdAndDateRange(@Param("storeId") UUID storeId,
                                                  @Param("from") LocalDate from,
                                                  @Param("to") LocalDate to);
}
