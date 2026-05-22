package com.yian.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class DrugPrescriptionVO {
    private Long id;
    private Long residentId;
    private String residentName;
    private Long drugId;
    private String drugName;
    private String dosage;
    private String frequency;
    private Integer dosesPerDay;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private Long prescribedBy;
    private String notes;
    private LocalDate createdAt;
}
