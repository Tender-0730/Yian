package com.yian.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("nurse_info")
public class NurseInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String employeeNo;
    private String phone;
    private String shiftPreference;
    private String status;
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
