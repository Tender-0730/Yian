package com.yian.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DrugRecordSaveRequest {
    @NotNull(message = "处方ID不能为空")
    private Long prescriptionId;

    private String status;
    private String notes;
}
