package com.yian.enums;

import lombok.Getter;

@Getter
public enum OutingStatusEnum {
    OUT("OUT", "外出中"),
    RETURNED("RETURNED", "已返回");

    private final String code;
    private final String desc;

    OutingStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
