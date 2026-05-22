package com.yian.enums;

import lombok.Getter;

@Getter
public enum HandoverStatusEnum {
    COMPLETED("COMPLETED", "已完成"),
    PENDING("PENDING", "待确认");

    private final String code;
    private final String desc;

    HandoverStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
