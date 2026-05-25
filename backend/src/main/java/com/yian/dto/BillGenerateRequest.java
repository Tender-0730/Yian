package com.yian.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class BillGenerateRequest {
    @NotBlank(message = "账单周期不能为空")
    @Pattern(regexp = "\\d{4}-\\d{2}", message = "账单周期格式为yyyy-MM")
    private String billPeriod;

    /** 可选：指定老人ID，不传则生成全部在院老人的账单 */
    private Long residentId;
}
