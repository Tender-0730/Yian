package com.yian.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("nurse_schedule")
public class NurseSchedule {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long nurseId;
    private LocalDate shiftDate;
    private String shiftType;
    private String status;
    private String notes;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
