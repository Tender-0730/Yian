package com.yian.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class BillItemVO {
    private Long id;
    private Long billId;
    private Long feeConfigId;
    private String feeName;
    private BigDecimal amount;
    private String description;
}
