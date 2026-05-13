package com.yian.enums;

import lombok.Getter;

@Getter
public enum BedStatusEnum {
    AVAILABLE("AVAILABLE", "空闲"),
    OCCUPIED("OCCUPIED", "已占用"),
    MAINTENANCE("MAINTENANCE", "维修中");

    private final String code;
    private final String desc;

    BedStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
