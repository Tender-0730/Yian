package com.yian.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class ShiftHandoverVO {
    private Long id;
    private Long scheduleId;
    private LocalDate shiftDate;
    private Long fromNurseId;
    private String fromNurseName;
    private Long toNurseId;
    private String toNurseName;
    private LocalDateTime handoverTime;
    private String keyNotes;
    private String residentNotes;
    private String materialCheck;
    private String status;
    private LocalDateTime createdAt;
}
