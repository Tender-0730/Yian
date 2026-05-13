package com.yian.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class MealSaveRequest {
    @NotNull(message = "老人ID不能为空")
    private Long residentId;

    @NotNull(message = "日期不能为空")
    private LocalDate mealDate;

    @NotBlank(message = "餐别不能为空")
    private String mealType;

    @NotBlank(message = "膳食内容不能为空")
    private String content;

    private String notes;
}
