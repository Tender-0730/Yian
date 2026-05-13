package com.yian.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DietaryRestrictionVO {
    private Long id;
    private Long residentId;
    private String restriction;
    private String description;
}
