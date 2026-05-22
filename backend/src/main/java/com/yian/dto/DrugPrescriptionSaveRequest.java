package com.yian.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DrugPrescriptionSaveRequest {
    @NotNull(message = "老人ID不能为空")
    private Long residentId;

    @NotNull(message = "药品ID不能为空")
    private Long drugId;

    private String dosage;
    private String frequency;
    private Integer dosesPerDay;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long prescribedBy;
    private String status;
    private String notes;
}
