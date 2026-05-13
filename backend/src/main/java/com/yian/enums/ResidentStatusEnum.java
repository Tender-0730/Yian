package com.yian.enums;

import lombok.Getter;

@Getter
public enum ResidentStatusEnum {
    CHECKED_IN("CHECKED_IN", "已入住"),
    CHECKED_OUT("CHECKED_OUT", "已退住");

    private final String code;
    private final String desc;

    ResidentStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
