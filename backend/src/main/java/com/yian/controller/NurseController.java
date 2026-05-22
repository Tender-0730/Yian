package com.yian.controller;

import com.yian.common.PageResult;
import com.yian.common.Result;
import com.yian.dto.*;
import com.yian.service.NurseService;
import com.yian.vo.NurseInfoVO;
import com.yian.vo.NurseScheduleVO;
import com.yian.vo.ShiftHandoverVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "护工排班")
@RestController
@RequiredArgsConstructor
public class NurseController {

    private final NurseService nurseService;

    // ==================== 护工信息 ====================

    @Operation(summary = "分页查询护工")
    @GetMapping("/api/nurses")
    public Result<PageResult<NurseInfoVO>> pageNurses(@Valid NurseQuery query) {
        return Result.success(nurseService.pageNurses(query));
    }

    @Operation(summary = "获取护工详情")
    @GetMapping("/api/nurses/{id}")
    public Result<NurseInfoVO> getNurseById(@PathVariable Long id) {
        return Result.success(nurseService.getNurseById(id));
    }

    @Operation(summary = "新增护工")
    @PostMapping("/api/nurses")
    public Result<Long> createNurse(@Valid @RequestBody NurseInfoSaveRequest request) {
        return Result.success(nurseService.createNurse(request));
    }

    @Operation(summary = "编辑护工")
    @PutMapping("/api/nurses/{id}")
    public Result<Void> updateNurse(@PathVariable Long id,
                                     @Valid @RequestBody NurseInfoSaveRequest request) {
        nurseService.updateNurse(id, request);
        return Result.success();
    }

    @Operation(summary = "删除护工")
    @DeleteMapping("/api/nurses/{id}")
    public Result<Void> deleteNurse(@PathVariable Long id) {
        nurseService.deleteNurse(id);
        return Result.success();
    }

    // ==================== 排班 ====================

    @Operation(summary = "分页查询排班")
    @GetMapping("/api/nurse-schedules")
    public Result<PageResult<NurseScheduleVO>> pageSchedules(@Valid NurseQuery query) {
        return Result.success(nurseService.pageSchedules(query));
    }

    @Operation(summary = "新增排班")
    @PostMapping("/api/nurse-schedules")
    public Result<Long> createSchedule(@Valid @RequestBody NurseScheduleSaveRequest request) {
        return Result.success(nurseService.createSchedule(request));
    }

    @Operation(summary = "编辑排班")
    @PutMapping("/api/nurse-schedules/{id}")
    public Result<Void> updateSchedule(@PathVariable Long id,
                                        @Valid @RequestBody NurseScheduleSaveRequest request) {
        nurseService.updateSchedule(id, request);
        return Result.success();
    }

    @Operation(summary = "删除排班")
    @DeleteMapping("/api/nurse-schedules/{id}")
    public Result<Void> deleteSchedule(@PathVariable Long id) {
        nurseService.deleteSchedule(id);
        return Result.success();
    }

    // ==================== 交接班 ====================

    @Operation(summary = "分页查询交接记录")
    @GetMapping("/api/shift-handovers")
    public Result<PageResult<ShiftHandoverVO>> pageHandovers(@Valid NurseQuery query) {
        return Result.success(nurseService.pageHandovers(query));
    }

    @Operation(summary = "创建交接记录")
    @PostMapping("/api/shift-handovers")
    public Result<Long> createHandover(@Valid @RequestBody ShiftHandoverSaveRequest request) {
        return Result.success(nurseService.createHandover(request));
    }
}
