package com.yian.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BedVO {
    private Long id;
    private Long roomId;
    private String bedNumber;
    private String status;
    private Long recordId;
    private String residentName;
}
