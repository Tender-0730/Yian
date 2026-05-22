package com.yian.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class NurseScheduleVO {
    private Long id;
    private Long nurseId;
    private String nurseName;
    private LocalDate shiftDate;
    private String shiftType;
    private String status;
    private String notes;
}
