package com.yian.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DrugDictSaveRequest {
    @NotBlank(message = "药品名称不能为空")
    private String drugName;

    private String specification;
    private String manufacturer;
    private String unit;
    private String category;
    private String description;
}
