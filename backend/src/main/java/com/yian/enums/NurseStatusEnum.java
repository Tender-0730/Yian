package com.yian.enums;

import lombok.Getter;

@Getter
public enum NurseStatusEnum {
    ACTIVE("ACTIVE", "在职"),
    INACTIVE("INACTIVE", "离职");

    private final String code;
    private final String desc;

    NurseStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
