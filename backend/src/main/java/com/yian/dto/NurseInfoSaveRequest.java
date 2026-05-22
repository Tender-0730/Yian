package com.yian.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NurseInfoSaveRequest {
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotBlank(message = "工号不能为空")
    private String employeeNo;

    private String phone;
    private String shiftPreference;
    private String remark;
}
