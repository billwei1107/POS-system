/**
 * @file XReportRepository.java
 * @description X Report Repository / X report repository
 * @description_en JPA repository for X Report records
 * @description_zh X Report JPA 儲存庫
 */
package com.enterprise.staff.repository;

import com.enterprise.staff.entity.XReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface XReportRepository extends JpaRepository<XReport, UUID> {

    @Query("SELECT r FROM XReport r WHERE r.shiftId = :shiftId AND r.deletedAt IS NULL ORDER BY r.generatedAt")
    List<XReport> findByShiftId(@Param("shiftId") UUID shiftId);
}
