package com.yian.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("outing_record")
public class OutingRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long residentId;
    private LocalDateTime outTime;
    private LocalDateTime expectedReturnTime;
    private LocalDateTime actualReturnTime;
    private String destination;
    private String companion;
    private String reason;
    private String status;
    private String notes;
    private Long registeredBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
