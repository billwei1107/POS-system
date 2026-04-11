package com.enterprise.attendance.repository;

import com.enterprise.attendance.entity.AttendanceCorrection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * @file AttendanceCorrectionRepository.java
 * @description 補卡申請資料存取 / Attendance correction repository
 */
@Repository
public interface AttendanceCorrectionRepository extends JpaRepository<AttendanceCorrection, UUID> {
    List<AttendanceCorrection> findByEmployeeIdOrderByCreatedAtDesc(String employeeId);
}
