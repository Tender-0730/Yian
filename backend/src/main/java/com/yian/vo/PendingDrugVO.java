package com.yian.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PendingDrugVO {
    private Long prescriptionId;
    private Long residentId;
    private String residentName;
    private Long drugId;
    private String drugName;
    private String dosage;
    private String frequency;
    private Integer dosesPerDay;
    private Integer takenCount;
    private Integer pendingCount;
    private LocalDateTime lastTakenTime;
}
