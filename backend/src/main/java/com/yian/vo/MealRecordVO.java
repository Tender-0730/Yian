package com.yian.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class MealRecordVO {
    private Long id;
    private Long residentId;
    private String residentName;
    private LocalDate mealDate;
    private String mealType;
    private String content;
    private String notes;
}
