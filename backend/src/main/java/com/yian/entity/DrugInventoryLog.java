package com.yian.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("drug_inventory_log")
public class DrugInventoryLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long inventoryId;
    private Long drugId;
    private String changeType;
    private Integer changeQuantity;
    private Integer beforeQuantity;
    private Integer afterQuantity;
    private Long operatorId;
    private String reason;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;
}
