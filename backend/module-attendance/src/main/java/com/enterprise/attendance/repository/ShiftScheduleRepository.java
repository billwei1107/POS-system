package com.enterprise.attendance.repository;

import com.enterprise.attendance.entity.ShiftSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * @file ShiftScheduleRepository.java
 * @description 班表定義資料存取 / Shift schedule repository
 */
@Repository
public interface ShiftScheduleRepository extends JpaRepository<ShiftSchedule, UUID> {
}
