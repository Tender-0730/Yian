package com.yian.enums;

import lombok.Getter;

@Getter
public enum BillStatusEnum {
    UNPAID("UNPAID", "未支付"),
    PARTIAL("PARTIAL", "部分支付"),
    PAID("PAID", "已付清"),
    CANCELLED("CANCELLED", "已作废");

    private final String code;
    private final String desc;

    BillStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
