package com.yian.controller;

import com.yian.common.PageResult;
import com.yian.common.Result;
import com.yian.dto.HealthRecordQuery;
import com.yian.dto.HealthRecordSaveRequest;
import com.yian.service.HealthRecordService;
import com.yian.vo.HealthRecordVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "健康记录")
@RestController
@RequestMapping("/api/health-records")
@RequiredArgsConstructor
public class HealthRecordController {

    private final HealthRecordService healthRecordService;

    @Operation(summary = "分页查询健康记录")
    @GetMapping
    public Result<PageResult<HealthRecordVO>> pageHealthRecords(@Valid HealthRecordQuery query) {
        return Result.success(healthRecordService.pageHealthRecords(query));
    }

    @Operation(summary = "获取健康记录详情")
    @GetMapping("/{id}")
    public Result<HealthRecordVO> getHealthRecordById(@PathVariable Long id) {
        return Result.success(healthRecordService.getHealthRecordById(id));
    }

    @Operation(summary = "新增健康记录")
    @PostMapping
    public Result<Long> createHealthRecord(@Valid @RequestBody HealthRecordSaveRequest request) {
        return Result.success(healthRecordService.createHealthRecord(request));
    }

    @Operation(summary = "编辑健康记录")
    @PutMapping("/{id}")
    public Result<Void> updateHealthRecord(@PathVariable Long id,
                                            @Valid @RequestBody HealthRecordSaveRequest request) {
        healthRecordService.updateHealthRecord(id, request);
        return Result.success();
    }

    @Operation(summary = "删除健康记录")
    @DeleteMapping("/{id}")
    public Result<Void> deleteHealthRecord(@PathVariable Long id) {
        healthRecordService.deleteHealthRecord(id);
        return Result.success();
    }
}
