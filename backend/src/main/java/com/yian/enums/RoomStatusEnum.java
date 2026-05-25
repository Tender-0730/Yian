package com.yian.enums;

import lombok.Getter;

@Getter
public enum RoomStatusEnum {
    IN_USE("IN_USE", "使用中"),
    FULL("FULL", "已满"),
    MAINTENANCE("MAINTENANCE", "维修中");

    private final String code;
    private final String desc;

    RoomStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
