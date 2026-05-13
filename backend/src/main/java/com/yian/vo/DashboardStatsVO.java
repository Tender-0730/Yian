package com.yian.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DashboardStatsVO {
    private Long totalResidents;
    private Long checkedInCount;
    private Long specialCareCount;
    private Long todayNewCount;
    private String occupancyRate;
    private List<DistributionItem> careLevelDistribution;
    private List<DistributionItem> ageDistribution;

    @Data
    @Builder
    public static class DistributionItem {
        private String name;
        private Long value;
    }
}
