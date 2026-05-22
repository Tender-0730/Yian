package com.yian.enums;

import lombok.Getter;

@Getter
public enum PaymentMethodEnum {
    CASH("CASH", "现金"),
    WECHAT("WECHAT", "微信"),
    ALIPAY("ALIPAY", "支付宝"),
    BANK_TRANSFER("BANK_TRANSFER", "银行转账");

    private final String code;
    private final String desc;

    PaymentMethodEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
