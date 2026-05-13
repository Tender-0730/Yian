package com.yian.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class HealthRecordVO {
    private Long id;
    private Long residentId;
    private String residentName;
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
    private Long recordedBy;
    private LocalDateTime recordedAt;
    private LocalDateTime createdAt;
}
