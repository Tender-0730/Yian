package com.yian.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BuildingVO {
    private Long id;
    private String buildingName;
    private Integer floorCount;
    private String description;
}
