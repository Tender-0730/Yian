package com.yian.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class NurseScheduleSaveRequest {
    @NotNull(message = "护工ID不能为空")
    private Long nurseId;

    @NotNull(message = "排班日期不能为空")
    private LocalDate shiftDate;

    @NotNull(message = "班次类型不能为空")
    private String shiftType;

    private String status;
    private String notes;
}
