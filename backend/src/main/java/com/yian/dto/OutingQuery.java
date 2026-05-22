package com.yian.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;

@Data
public class OutingQuery {
    private Long residentId;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;

    @Positive(message = "页码必须大于0")
    private Integer page = 1;

    @Min(value = 1, message = "每页条数至少为1")
    private Integer size = 10;
}
