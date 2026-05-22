package com.yian.enums;

import lombok.Getter;

@Getter
public enum ScheduleStatusEnum {
    SCHEDULED("SCHEDULED", "已排班"),
    COMPLETED("COMPLETED", "已完成"),
    ABSENT("ABSENT", "缺勤");

    private final String code;
    private final String desc;

    ScheduleStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
