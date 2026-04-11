package com.enterprise.attendance.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @file CorrectionRequest.java
 * @description 補卡申請請求 / Attendance correction request DTO
 */
@Data
public class CorrectionRequest {
    private String employeeId;
    private String recordId;
    private String reason;
    private LocalDateTime correctedTime;
    private String correctionType = "CLOCK_IN"; // CLOCK_IN / CLOCK_OUT
}
