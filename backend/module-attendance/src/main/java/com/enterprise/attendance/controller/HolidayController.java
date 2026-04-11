package com.enterprise.attendance.controller;

import com.enterprise.attendance.entity.Holiday;
import com.enterprise.attendance.repository.HolidayRepository;
import com.enterprise.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * @file HolidayController.java
 * @description 假日設定管理控制器 / Holiday management controller
 */
@RestController
@RequestMapping("/api/v1/attendance/holidays")
@RequiredArgsConstructor
public class HolidayController {

    private final HolidayRepository holidayRepository;

    @GetMapping
    public ApiResponse<List<Holiday>> getHolidays(@RequestParam(defaultValue = "2026") int year) {
        return ApiResponse.success(holidayRepository.findByYear(year));
    }

    @PostMapping
    public ApiResponse<Holiday> createHoliday(@RequestBody Holiday holiday) {
        return ApiResponse.success(holidayRepository.save(holiday));
    }

    @PutMapping("/{id}")
    public ApiResponse<Holiday> updateHoliday(@PathVariable UUID id, @RequestBody Holiday updated) {
        Holiday holiday = holidayRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Holiday not found"));
        holiday.setDate(updated.getDate());
        holiday.setName(updated.getName());
        holiday.setType(updated.getType());
        holiday.setYear(updated.getYear());
        return ApiResponse.success(holidayRepository.save(holiday));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteHoliday(@PathVariable UUID id) {
        holidayRepository.findById(id).ifPresent(h -> {
            h.setDeletedAt(LocalDateTime.now());
            holidayRepository.save(h);
        });
        return ApiResponse.success(null);
    }
}
