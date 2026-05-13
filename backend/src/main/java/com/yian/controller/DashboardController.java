package com.yian.controller;

import com.yian.common.Result;
import com.yian.service.DashboardService;
import com.yian.vo.DashboardStatsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "仪表盘")
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "统计数据")
    @GetMapping("/stats")
    public Result<DashboardStatsVO> getStats() {
        return Result.success(dashboardService.getStats());
    }
}
