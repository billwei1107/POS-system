/**
 * @file LeaveRequestDTO.java
 * @description 請假申請 DTO / Leave request data transfer object
 * @description_en Request payload for submitting a leave application
 * @description_zh 提交請假申請的資料傳輸物件
 */
package com.enterprise.leave.dto;

import com.enterprise.leave.entity.LeaveRequest.HalfDay;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class LeaveRequestDTO {

    @NotNull
    private UUID employeeId;

    @NotNull
    private UUID leaveTypeId;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    private HalfDay startHalf = HalfDay.FULL;
    private HalfDay endHalf   = HalfDay.FULL;

    private String reason;
    private String attachmentPath;
    private UUID   delegateId;
}
