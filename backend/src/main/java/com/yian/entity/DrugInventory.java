package com.yian.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("drug_inventory")
public class DrugInventory {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long drugId;
    private Integer quantity;
    private Integer alertThreshold;
    private String location;
    private String batchNo;
    private LocalDate expireDate;
    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
