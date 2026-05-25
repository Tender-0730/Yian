package com.yian.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("payment_record")
public class PaymentRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long billId;
    private Long residentId;
    private BigDecimal amount;
    private String paymentMethod;
    private LocalDateTime paidAt;
    private Long receivedBy;
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;
}
