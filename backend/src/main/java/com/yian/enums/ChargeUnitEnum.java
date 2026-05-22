package com.yian.enums;

import lombok.Getter;

@Getter
public enum ChargeUnitEnum {
    DAILY("DAILY", "按天"),
    MONTHLY("MONTHLY", "按月"),
    ONE_TIME("ONE_TIME", "一次性");

    private final String code;
    private final String desc;

    ChargeUnitEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
