package com.enterprise.attendance.repository;

import com.enterprise.attendance.entity.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * @file HolidayRepository.java
 * @description 假日設定資料存取 / Holiday repository
 */
@Repository
public interface HolidayRepository extends JpaRepository<Holiday, UUID> {
    List<Holiday> findByYear(Integer year);
    boolean existsByDate(LocalDate date);
}
