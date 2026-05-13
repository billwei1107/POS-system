/**
 * @file ShiftHandoverRepository.java
 * @description 交接班 Repository / Shift handover repository
 * @description_en JPA repository for shift handover records
 * @description_zh 交接班記錄 JPA 儲存庫
 */
package com.enterprise.staff.repository;

import com.enterprise.staff.entity.ShiftHandover;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShiftHandoverRepository extends JpaRepository<ShiftHandover, UUID> {

    @Query("SELECT h FROM ShiftHandover h WHERE h.fromShiftId = :shiftId AND h.deletedAt IS NULL")
    Optional<ShiftHandover> findByFromShiftId(@Param("shiftId") UUID shiftId);

    @Query("SELECT h FROM ShiftHandover h WHERE h.storeId = :storeId AND h.deletedAt IS NULL ORDER BY h.handoverAt DESC")
    List<ShiftHandover> findRecentByStoreId(@Param("storeId") UUID storeId);
}
