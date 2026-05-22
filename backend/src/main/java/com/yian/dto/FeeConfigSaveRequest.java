package com.yian.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FeeConfigSaveRequest {
    @NotBlank(message = "费用名称不能为空")
    private String feeName;

    @NotNull(message = "默认金额不能为空")
    private BigDecimal defaultAmount;

    @NotBlank(message = "计费单位不能为空")
    private String chargeUnit;

    private String description;
    private Integer sortOrder;
}
