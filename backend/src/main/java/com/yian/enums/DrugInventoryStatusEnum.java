package com.yian.enums;

import lombok.Getter;

@Getter
public enum DrugInventoryStatusEnum {
    ACTIVE("ACTIVE", "正常"),
    LOW_STOCK("LOW_STOCK", "低库存"),
    DEPLETED("DEPLETED", "已用完");

    private final String code;
    private final String desc;

    DrugInventoryStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
