package com.yian.enums;

import lombok.Getter;

@Getter
public enum CheckInStatusEnum {
    CHECKED_IN("CHECKED_IN", "入住中"),
    CHECKED_OUT("CHECKED_OUT", "已退住");

    private final String code;
    private final String desc;

    CheckInStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
