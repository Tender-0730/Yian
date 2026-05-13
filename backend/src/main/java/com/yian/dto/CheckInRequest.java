package com.yian.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CheckInRequest {
    @NotNull(message = "老人ID不能为空")
    private Long residentId;

    @NotNull(message = "床位ID不能为空")
    private Long bedId;

    @NotNull(message = "入住日期不能为空")
    private LocalDate checkInDate;

    private String remark;
}
