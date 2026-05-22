package com.yian.enums;

import lombok.Getter;

@Getter
public enum DrugRecordStatusEnum {
    TAKEN("TAKEN", "已服用"),
    MISSED("MISSED", "漏服"),
    REFUSED("REFUSED", "拒服");

    private final String code;
    private final String desc;

    DrugRecordStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
