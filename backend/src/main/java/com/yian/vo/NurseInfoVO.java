package com.yian.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NurseInfoVO {
    private Long id;
    private Long userId;
    private String nurseName;
    private String employeeNo;
    private String phone;
    private String shiftPreference;
    private String status;
    private String remark;
    private LocalDateTime createdAt;
}
