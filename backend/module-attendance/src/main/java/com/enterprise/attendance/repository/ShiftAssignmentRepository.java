package com.enterprise.attendance.repository;

import com.enterprise.attendance.entity.ShiftAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @file ShiftAssignmentRepository.java
 * @description 班表指派資料存取 / Shift assignment repository
 */
@Repository
public interface ShiftAssignmentRepository extends JpaRepository<ShiftAssignment, UUID> {

    List<ShiftAssignment> findByEmployeeIdAndDeletedAtIsNull(String employeeId);

    Optional<ShiftAssignment> findByEmployeeIdAndEffectiveDateLessThanEqualAndEndDateGreaterThanEqualAndDeletedAtIsNull(
            String employeeId, LocalDate date, LocalDate date2);

    default Optional<ShiftAssignment> findActiveByEmployeeIdAndDate(String employeeId, LocalDate date) {
        return findByEmployeeIdAndEffectiveDateLessThanEqualAndEndDateGreaterThanEqualAndDeletedAtIsNull(
                employeeId, date, date);
    }
}
