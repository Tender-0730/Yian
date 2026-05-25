package com.yian.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("resident_care_level")
public class ResidentCareLevel {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long residentId;
    private Long careLevelId;
    private LocalDate effectiveDate;
    private LocalDate expireDate;
    private String status;
    private Long updatedBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;
}
