package com.yian.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("drug_record")
public class DrugRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long prescriptionId;
    private Long residentId;
    private Long drugId;
    private String drugName;
    private LocalDateTime administeredAt;
    private Long administeredBy;
    private String status;
    private String notes;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
