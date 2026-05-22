package com.yian.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class FeeConfigVO {
    private Long id;
    private String feeName;
    private BigDecimal defaultAmount;
    private String chargeUnit;
    private String description;
    private Integer sortOrder;
    private String status;
    private LocalDateTime createdAt;
}
