package com.yian.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("health_record")
public class HealthRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long residentId;
    private BigDecimal temperature;
    private Integer bloodSystolic;
    private Integer bloodDiastolic;
    private Integer heartRate;
    private BigDecimal bloodSugar;
    private BigDecimal weight;
    private Integer oxygen;
    private String status;
    private String abnormal;
    private String notes;
    private Long recordedBy;
    private LocalDateTime recordedAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;
}
