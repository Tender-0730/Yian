package com.yian.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class BillDetailVO {
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
    private List<BillItemVO> items;
    private List<PaymentRecordVO> payments;
}
