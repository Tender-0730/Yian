package com.yian.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ResidentArrearsVO {
    private Long residentId;
    private String residentName;
    private BigDecimal totalArrears;
    private Integer unpaidBillCount;
}
