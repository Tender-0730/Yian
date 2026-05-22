package com.yian.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("shift_handover")
public class ShiftHandover {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long scheduleId;
    private LocalDate shiftDate;
    private Long fromNurseId;
    private Long toNurseId;
    private LocalDateTime handoverTime;
    private String keyNotes;
    private String residentNotes;
    private String materialCheck;
    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
