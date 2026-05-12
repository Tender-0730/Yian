package com.yian.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("care_level")
public class CareLevel {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String levelName;
    private String levelCode;
    private String description;
    private BigDecimal dailyFee;
    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
