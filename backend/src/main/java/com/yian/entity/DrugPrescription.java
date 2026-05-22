package com.yian.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("drug_prescription")
public class DrugPrescription {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long residentId;
    private Long drugId;
    private String dosage;
    private String frequency;
    private Integer dosesPerDay;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private Long prescribedBy;
    private String notes;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
