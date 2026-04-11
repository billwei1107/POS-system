package com.enterprise.attendance.dto;

import lombok.Data;

/**
 * @file ClockInRequest.java
 * @description 打卡上班請求 / Clock-in request DTO
 * @description_zh 包含員工ID、GPS經緯度與打卡方式
 */
@Data
public class ClockInRequest {
    private String employeeId;
    private Double latitude;
    private Double longitude;
    private String method = "GPS"; // GPS / WIFI / QR
    private String wifiBssid;
}
