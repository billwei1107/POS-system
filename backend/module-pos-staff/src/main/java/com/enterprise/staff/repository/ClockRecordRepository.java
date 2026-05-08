/**
 * @file ClockRecordRepository.java
 * @description 打卡記錄 Repository / Clock record repository
 * @description_en JPA repository for employee clock records
 * @description_zh 員工打卡記錄 JPA 儲存庫
 */
package com.enterprise.staff.repository;

import com.enterprise.staff.entity.ClockRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ClockRecordRepository extends JpaRepository<ClockRecord, UUID> {

    @Query("SELECT c FROM ClockRecord c WHERE c.shiftId = :shiftId AND c.deletedAt IS NULL ORDER BY c.clockedAt")
    List<ClockRecord> findByShiftId(@Param("shiftId") UUID shiftId);
}
