package com.yian.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CareLevelVO {
    private Long id;
    private String levelName;
    private String levelCode;
    private String description;
    private BigDecimal dailyFee;
    private Integer sortOrder;
}
