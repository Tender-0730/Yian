package com.yian.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("bed")
public class Bed {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long roomId;
    private String bedNumber;
    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;
}
