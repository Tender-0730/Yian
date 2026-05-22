package com.yian.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("bill_item")
public class BillItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long billId;
    private Long feeConfigId;
    private String feeName;
    private BigDecimal amount;
    private String description;
}
