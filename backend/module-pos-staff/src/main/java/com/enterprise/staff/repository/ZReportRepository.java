/**
 * @file ZReportRepository.java
 * @description Z Report Repository / Z report repository
 * @description_en JPA repository for Z Report records with unique date constraint queries
 * @description_zh Z Report JPA 儲存庫，含每日唯一性查詢
 */
package com.enterprise.staff.repository;

import com.enterprise.staff.entity.ZReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ZReportRepository extends JpaRepository<ZReport, UUID> {

    @Query("SELECT z FROM ZReport z WHERE z.storeId = :storeId AND z.reportDate = :date AND z.deletedAt IS NULL")
    Optional<ZReport> findByStoreIdAndReportDate(@Param("storeId") UUID storeId, @Param("date") LocalDate date);

    @Query("SELECT z FROM ZReport z WHERE z.storeId = :storeId AND z.deletedAt IS NULL ORDER BY z.reportDate DESC")
    List<ZReport> findRecentByStoreId(@Param("storeId") UUID storeId);
}
