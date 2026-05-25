package com.yian.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("building")
public class Building {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String buildingName;
    private Integer floorCount;
    private String description;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;
}
