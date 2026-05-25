package com.yian.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("meal_record")
public class MealRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long residentId;
    private LocalDate mealDate;
    private String mealType;
    private String content;
    private String notes;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;
}
