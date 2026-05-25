package com.yian.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("drug_dict")
public class DrugDict {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String drugName;
    private String specification;
    private String manufacturer;
    private String unit;
    private String category;
    private String description;
    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;
}
