package com.yian.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ShiftHandoverSaveRequest {
    @NotNull(message = "排班ID不能为空")
    private Long scheduleId;

    @NotNull(message = "接班人ID不能为空")
    private Long toNurseId;

    private LocalDate shiftDate;
    private String keyNotes;
    private String residentNotes;
    private String materialCheck;
}
