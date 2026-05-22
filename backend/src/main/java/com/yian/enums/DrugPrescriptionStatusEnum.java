package com.yian.enums;

import lombok.Getter;

@Getter
public enum DrugPrescriptionStatusEnum {
    ACTIVE("ACTIVE", "用药中"),
    STOPPED("STOPPED", "已停用");

    private final String code;
    private final String desc;

    DrugPrescriptionStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
