package com.yian.dto;

import lombok.Data;

@Data
public class ResidentPageQuery {
    private String name;
    private String status;
    private Long careLevelId;
    private Integer page = 1;
    private Integer size = 10;
}
