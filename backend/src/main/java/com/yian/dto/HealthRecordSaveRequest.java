package com.yian.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class HealthRecordSaveRequest {
    @NotNull(message = "老人ID不能为空")
    private Long residentId;

    private BigDecimal temperature;
    private Integer bloodSystolic;
    private Integer bloodDiastolic;
    private Integer heartRate;
    private BigDecimal bloodSugar;
    private BigDecimal weight;
    private Integer oxygen;
    private String status;
    private String abnormal;
    private String notes;
    private LocalDateTime recordedAt;
}
