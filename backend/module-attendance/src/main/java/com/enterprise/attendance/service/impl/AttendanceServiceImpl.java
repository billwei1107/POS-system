package com.enterprise.attendance.service.impl;

import com.enterprise.attendance.dto.AttendanceRecordDTO;
import com.enterprise.attendance.dto.ClockInRequest;
import com.enterprise.attendance.dto.CorrectionRequest;
import com.enterprise.attendance.entity.AttendanceCorrection;
import com.enterprise.attendance.entity.AttendanceRecord;
import com.enterprise.attendance.entity.Geofence;
import com.enterprise.attendance.entity.ShiftAssignment;
import com.enterprise.attendance.entity.ShiftSchedule;
import com.enterprise.attendance.repository.*;
import com.enterprise.attendance.service.AttendanceService;
import com.enterprise.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @file AttendanceServiceImpl.java
 * @description 打卡考勤服務實作 / Attendance service implementation
 * @description_zh 處理打卡上下班、GPS 圍欄驗證、遲到判定與出勤記錄查詢
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRecordRepository recordRepository;
    private final AttendanceCorrectionRepository correctionRepository;
    private final ShiftAssignmentRepository shiftAssignmentRepository;
    private final ShiftScheduleRepository shiftScheduleRepository;
    private final GeofenceRepository geofenceRepository;
    private final HolidayRepository holidayRepository;

    // ========================================
    // 打卡上班 / Clock In
    // ========================================
    @Override
    @Transactional
    public AttendanceRecordDTO clockIn(ClockInRequest request) {
        LocalDate today = LocalDate.now();
        String employeeId = request.getEmployeeId();

        // 1. 檢查是否已打卡 / Check if already clocked in
        if (recordRepository.findByEmployeeIdAndDate(employeeId, today).isPresent()) {
            throw new BusinessException(409, "已打卡上班，請勿重複打卡 / Already clocked in today");
        }

        // 2. 驗證地理圍欄 / Validate geofence
        if (request.getLatitude() != null && request.getLongitude() != null) {
            validateGeofence(request.getLatitude(), request.getLongitude());
        }

        // 3. 查找員工班表 → 判斷遲到 / Find shift → determine late status
        String status = "NORMAL";
        ShiftSchedule shift = findActiveShift(employeeId, today);
        if (shift != null) {
            LocalTime now = LocalTime.now();
            LocalTime deadline = shift.getStartTime().plusMinutes(shift.getGraceMinutes());
            if (now.isAfter(deadline)) {
                status = "LATE";
                log.info("⏰ Employee {} is LATE. Shift start: {}, Grace: {}min, Actual: {}",
                        employeeId, shift.getStartTime(), shift.getGraceMinutes(), now);
            }
        }

        // 4. 建立打卡記錄 / Create attendance record
        AttendanceRecord record = new AttendanceRecord();
        record.setEmployeeId(employeeId);
        record.setDate(today);
        record.setClockInTime(LocalDateTime.now());
        record.setClockInLocation(String.format("{\"lat\":%.6f,\"lng\":%.6f}", 
                request.getLatitude() != null ? request.getLatitude() : 0,
                request.getLongitude() != null ? request.getLongitude() : 0));
        record.setClockInMethod(request.getMethod());
        record.setStatus(status);

        recordRepository.save(record);
        log.info("✅ Employee {} clocked in at {} with status {}", employeeId, record.getClockInTime(), status);

        return toDTO(record);
    }

    // ========================================
    // 打卡下班 / Clock Out
    // ========================================
    @Override
    @Transactional
    public AttendanceRecordDTO clockOut(String employeeId) {
        LocalDate today = LocalDate.now();

        AttendanceRecord record = recordRepository.findByEmployeeIdAndDate(employeeId, today)
                .orElseThrow(() -> new BusinessException(404, "今日尚未打卡上班 / No clock-in record found"));

        if (record.getClockOutTime() != null) {
            throw new BusinessException(409, "已打卡下班 / Already clocked out");
        }

        record.setClockOutTime(LocalDateTime.now());

        // 計算加班 / Calculate overtime
        ShiftSchedule shift = findActiveShift(employeeId, today);
        if (shift != null) {
            int overtimeMinutes = calculateOvertime(record.getClockOutTime().toLocalTime(), shift.getEndTime());
            record.setOvertimeMinutes(overtimeMinutes);
            if (overtimeMinutes > 30) {
                log.info("💼 Overtime recorded: Employee {} worked {} extra minutes", employeeId, overtimeMinutes);
            }
        }

        // 若上班正常但提早下班 / Early leave
        if ("NORMAL".equals(record.getStatus()) && shift != null 
                && record.getClockOutTime().toLocalTime().isBefore(shift.getEndTime())) {
            record.setStatus("EARLY_LEAVE");
        }

        recordRepository.save(record);
        return toDTO(record);
    }

    // ========================================
    // 查詢當日記錄 / Get Daily Record
    // ========================================
    @Override
    public AttendanceRecordDTO getDailyRecord(String employeeId, LocalDate date) {
        return recordRepository.findByEmployeeIdAndDate(employeeId, date)
                .map(this::toDTO)
                .orElse(null);
    }

    // ========================================
    // 查詢月度記錄 / Get Monthly Records
    // ========================================
    @Override
    public List<AttendanceRecordDTO> getMonthlyRecords(String employeeId, int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        return recordRepository.findByEmployeeIdAndDateBetweenOrderByDateAsc(employeeId, start, end)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    // ========================================
    // 提交補卡申請 / Submit Correction
    // ========================================
    @Override
    @Transactional
    public void submitCorrection(CorrectionRequest request) {
        AttendanceCorrection correction = new AttendanceCorrection();
        correction.setEmployeeId(request.getEmployeeId());
        if (request.getRecordId() != null) {
            correction.setRecordId(UUID.fromString(request.getRecordId()));
        }
        correction.setReason(request.getReason());
        correction.setCorrectedTime(request.getCorrectedTime());
        correction.setCorrectionType(request.getCorrectionType());
        correction.setStatus("PENDING");

        correctionRepository.save(correction);
        log.info("📝 Correction submitted by employee {}", request.getEmployeeId());
        // TODO: 未來可呼叫 WorkflowEngineService.startWorkflow("CORRECTION", ...) 發起審批
    }

    // ========================================
    // 私有方法 / Private Methods
    // ========================================

    /**
     * 驗證 GPS 座標是否在任一啟用的地理圍欄內 / Validate GPS within any active geofence
     */
    private void validateGeofence(double lat, double lng) {
        List<Geofence> fences = geofenceRepository.findByActiveTrue();
        if (fences.isEmpty()) {
            return; // 無圍欄設定則略過 / Skip if no geofences configured
        }

        boolean withinFence = fences.stream().anyMatch(fence -> {
            double distance = calculateDistance(lat, lng, fence.getLatitude(), fence.getLongitude());
            return distance <= fence.getRadiusMeters();
        });

        if (!withinFence) {
            throw new BusinessException(403, "不在打卡範圍內 / GPS location is outside geofence");
        }
    }

    /**
     * Haversine 公式計算兩點間距離（公尺）/ Calculate distance in meters
     */
    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        final int EARTH_RADIUS = 6371000; // 地球半徑（公尺）
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS * c;
    }

    /**
     * 查找員工當天的有效班表 / Find active shift for employee
     */
    private ShiftSchedule findActiveShift(String employeeId, LocalDate date) {
        return shiftAssignmentRepository.findByEmployeeIdAndDeletedAtIsNull(employeeId).stream()
                .filter(sa -> !sa.getEffectiveDate().isAfter(date) 
                        && (sa.getEndDate() == null || !sa.getEndDate().isBefore(date)))
                .findFirst()
                .map(sa -> shiftScheduleRepository.findById(sa.getShiftScheduleId()).orElse(null))
                .orElse(null);
    }

    /**
     * 計算加班分鐘數 / Calculate overtime minutes
     */
    private int calculateOvertime(LocalTime clockOut, LocalTime shiftEnd) {
        if (clockOut.isAfter(shiftEnd)) {
            return (int) java.time.Duration.between(shiftEnd, clockOut).toMinutes();
        }
        return 0;
    }

    /**
     * Entity → DTO 轉換 / Convert entity to DTO
     */
    private AttendanceRecordDTO toDTO(AttendanceRecord record) {
        return AttendanceRecordDTO.builder()
                .id(record.getId() != null ? record.getId().toString() : null)
                .employeeId(record.getEmployeeId())
                .date(record.getDate())
                .clockInTime(record.getClockInTime())
                .clockOutTime(record.getClockOutTime())
                .clockInMethod(record.getClockInMethod())
                .status(record.getStatus())
                .overtimeMinutes(record.getOvertimeMinutes())
                .build();
    }
}
