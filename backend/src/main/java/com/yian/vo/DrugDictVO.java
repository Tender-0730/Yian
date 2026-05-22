package com.yian.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DrugDictVO {
    private Long id;
    private String drugName;
    private String specification;
    private String manufacturer;
    private String unit;
    private String category;
    private String description;
    private String status;
    private LocalDateTime createdAt;
}
