package com.yian.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentRecordVO {
    private Long id;
    private Long billId;
    private Long residentId;
    private BigDecimal amount;
    private String paymentMethod;
    private LocalDateTime paidAt;
    private Long receivedBy;
    private String remark;
}
