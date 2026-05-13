package com.yian.enums;

import lombok.Getter;

@Getter
public enum CareLevelStatusEnum {
    ACTIVE("ACTIVE", "生效中"),
    INACTIVE("INACTIVE", "已失效");

    private final String code;
    private final String desc;

    CareLevelStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
