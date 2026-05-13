package com.yian.controller;

import com.yian.common.PageResult;
import com.yian.common.Result;
import com.yian.common.PageResult;
import com.yian.common.Result;
import com.yian.dto.CareLevelChangeRequest;
import com.yian.dto.ResidentPageQuery;
import com.yian.dto.ResidentSaveRequest;
import com.yian.service.ResidentService;
import com.yian.vo.ResidentDetailVO;
import com.yian.vo.ResidentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "老人管理")
@RestController
@RequestMapping("/api/residents")
@RequiredArgsConstructor
public class ResidentController {

    private final ResidentService residentService;

    @Operation(summary = "分页查询老人列表")
    @GetMapping
    public Result<PageResult<ResidentVO>> pageResidents(@Valid ResidentPageQuery query) {
        return Result.success(residentService.pageResidents(query));
    }

    @Operation(summary = "获取老人详情")
    @GetMapping("/{id}")
    public Result<ResidentDetailVO> getResidentById(@PathVariable Long id) {
        return Result.success(residentService.getResidentById(id));
    }

    @Operation(summary = "新增老人")
    @PostMapping
    public Result<Long> createResident(@Valid @RequestBody ResidentSaveRequest request) {
        return Result.success(residentService.createResident(request));
    }

    @Operation(summary = "编辑老人")
    @PutMapping("/{id}")
    public Result<Void> updateResident(@PathVariable Long id,
                                        @Valid @RequestBody ResidentSaveRequest request) {
        residentService.updateResident(id, request);
        return Result.success();
    }

    @Operation(summary = "删除老人")
    @DeleteMapping("/{id}")
    public Result<Void> deleteResident(@PathVariable Long id) {
        residentService.deleteResident(id);
        return Result.success();
    }

    @Operation(summary = "变更护理级别")
    @PutMapping("/{id}/care-level")
    public Result<Void> changeCareLevel(@PathVariable Long id,
                                         @Valid @RequestBody CareLevelChangeRequest request) {
        residentService.changeCareLevel(id, request.getCareLevelId());
        return Result.success();
    }

}
