package com.yian.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class MealQuery {
    private Long residentId;
    private LocalDate mealDate;
    private String mealType;
    private Integer page = 1;
    private Integer size = 10;
}
