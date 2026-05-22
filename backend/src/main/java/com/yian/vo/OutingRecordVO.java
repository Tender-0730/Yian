package com.yian.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class OutingRecordVO {
    private Long id;
    private Long residentId;
    private String residentName;
    private LocalDateTime outTime;
    private LocalDateTime expectedReturnTime;
    private LocalDateTime actualReturnTime;
    private String destination;
    private String companion;
    private String reason;
    private String status;
    private String notes;
    private Long registeredBy;
    private LocalDateTime createdAt;

    /** 派生状态：是否超时未归（查询时动态计算，不存库） */
    private Boolean isOverdue;
    /** 超时分钟数 */
    private Long overdueMinutes;
}
