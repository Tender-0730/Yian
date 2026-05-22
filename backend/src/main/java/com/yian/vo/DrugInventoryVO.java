package com.yian.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class DrugInventoryVO {
    private Long id;
    private Long drugId;
    private String drugName;
    private Integer quantity;
    private Integer alertThreshold;
    private String location;
    private String batchNo;
    private LocalDate expireDate;
    private String status;
    /** 派生状态：是否过期（expire_date < 当天） */
    private Boolean isExpired;
}
