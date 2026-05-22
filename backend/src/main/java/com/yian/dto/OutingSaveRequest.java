package com.yian.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OutingSaveRequest {
    @NotNull(message = "老人ID不能为空")
    private Long residentId;

    @NotNull(message = "外出时间不能为空")
    private LocalDateTime outTime;

    @NotNull(message = "预计返回时间不能为空")
    private LocalDateTime expectedReturnTime;

    private String destination;
    private String companion;
    private String reason;
    private String notes;
}
