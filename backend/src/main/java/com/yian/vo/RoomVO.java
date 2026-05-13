package com.yian.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoomVO {
    private Long id;
    private Long buildingId;
    private String buildingName;
    private Integer floor;
    private String roomNumber;
    private String roomType;
    private Integer capacity;
    private Integer occupied;
    private String status;
    private String description;
}
