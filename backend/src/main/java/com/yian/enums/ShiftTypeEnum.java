package com.yian.enums;

import lombok.Getter;

@Getter
public enum ShiftTypeEnum {
    MORNING("MORNING", "早班"),
    AFTERNOON("AFTERNOON", "中班"),
    NIGHT("NIGHT", "夜班");

    private final String code;
    private final String desc;

    ShiftTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
