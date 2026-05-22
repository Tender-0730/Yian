package com.yian.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DrugRecordVO {
    private Long id;
    private Long prescriptionId;
    private Long residentId;
    private String residentName;
    private Long drugId;
    private String drugName;
    private LocalDateTime administeredAt;
    private Long administeredBy;
    private String status;
    private String notes;
    private LocalDateTime createdAt;
}
