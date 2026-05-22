package com.yian.controller;

import com.yian.common.PageResult;
import com.yian.common.Result;
import com.yian.dto.OutingQuery;
import com.yian.dto.OutingSaveRequest;
import com.yian.service.OutingService;
import com.yian.vo.OutingRecordVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "外出登记")
@RestController
@RequestMapping("/api/outings")
@RequiredArgsConstructor
public class OutingController {

    private final OutingService outingService;

    @Operation(summary = "分页查询外出记录")
    @GetMapping
    public Result<PageResult<OutingRecordVO>> pageOutings(@Valid OutingQuery query) {
        return Result.success(outingService.pageOutings(query));
    }

    @Operation(summary = "获取外出记录详情")
    @GetMapping("/{id}")
    public Result<OutingRecordVO> getOutingById(@PathVariable Long id) {
        return Result.success(outingService.getOutingById(id));
    }

    @Operation(summary = "新增外出登记")
    @PostMapping
    public Result<Long> createOuting(@Valid @RequestBody OutingSaveRequest request) {
        return Result.success(outingService.createOuting(request));
    }

    @Operation(summary = "登记返回")
    @PutMapping("/{id}/return")
    public Result<Void> returnOuting(@PathVariable Long id) {
        outingService.returnOuting(id);
        return Result.success();
    }

    @Operation(summary = "取消外出登记（仅 OUT 状态可取消）")
    @DeleteMapping("/{id}")
    public Result<Void> cancelOuting(@PathVariable Long id) {
        outingService.cancelOuting(id);
        return Result.success();
    }

    @Operation(summary = "超时未归列表")
    @GetMapping("/overdue")
    public Result<List<OutingRecordVO>> listOverdue() {
        return Result.success(outingService.listOverdue());
    }
}
