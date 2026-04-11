package com.enterprise.attendance.repository;

import com.enterprise.attendance.entity.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @file AttendanceRecordRepository.java
 * @description 打卡記錄資料存取 / Attendance record repository
 */
@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, UUID> {

    Optional<AttendanceRecord> findByEmployeeIdAndDate(String employeeId, LocalDate date);

    List<AttendanceRecord> findByEmployeeIdAndDateBetweenOrderByDateAsc(
            String employeeId, LocalDate startDate, LocalDate endDate);

    List<AttendanceRecord> findByDateAndDeletedAtIsNull(LocalDate date);
}
