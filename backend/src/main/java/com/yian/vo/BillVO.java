package com.yian.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class BillVO {
    private Long id;
    private Long residentId;
    private String residentName;
    private String billNo;
    private String billPeriod;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal unpaidAmount;
    private String status;
    private String remark;
    private LocalDateTime generatedAt;
}
