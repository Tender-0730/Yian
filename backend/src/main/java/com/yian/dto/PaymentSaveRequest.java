package com.yian.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentSaveRequest {
    @NotNull(message = "支付金额不能为空")
    private BigDecimal amount;

    private String paymentMethod;
    private String remark;
}
