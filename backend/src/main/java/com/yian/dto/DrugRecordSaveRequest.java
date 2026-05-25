package com.yian.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DrugRecordSaveRequest {
    @NotNull(message = "处方ID不能为空")
    private Long prescriptionId;

    @NotBlank(message = "服药状态不能为空")
    private String status;
    private String notes;
}
