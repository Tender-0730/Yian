package com.yian.controller;

import com.yian.common.PageResult;
import com.yian.common.Result;
import com.yian.dto.*;
import com.yian.service.DrugService;
import com.yian.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Tag(name = "用药管理")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DrugController {

    private final DrugService drugService;

    // ==================== 药品字典 ====================

    @Operation(summary = "分页查询药品字典")
    @GetMapping("/drug-dicts")
    public Result<PageResult<DrugDictVO>> pageDrugDicts(@Valid DrugDictQuery query) {
        return Result.success(drugService.pageDrugDicts(query));
    }

    @Operation(summary = "获取药品详情")
    @GetMapping("/drug-dicts/{id}")
    public Result<DrugDictVO> getDrugDictById(@PathVariable Long id) {
        return Result.success(drugService.getDrugDictById(id));
    }

    @Operation(summary = "新增药品")
    @PostMapping("/drug-dicts")
    public Result<Long> createDrugDict(@Valid @RequestBody DrugDictSaveRequest request) {
        return Result.success(drugService.createDrugDict(request));
    }

    @Operation(summary = "编辑药品")
    @PutMapping("/drug-dicts/{id}")
    public Result<Void> updateDrugDict(@PathVariable Long id,
                                        @Valid @RequestBody DrugDictSaveRequest request) {
        drugService.updateDrugDict(id, request);
        return Result.success();
    }

    @Operation(summary = "删除药品")
    @DeleteMapping("/drug-dicts/{id}")
    public Result<Void> deleteDrugDict(@PathVariable Long id) {
        drugService.deleteDrugDict(id);
        return Result.success();
    }

    // ==================== 库存 ====================

    @Operation(summary = "分页查询库存")
    @GetMapping("/drug-inventories")
    public Result<PageResult<DrugInventoryVO>> pageInventories(@Valid DrugDictQuery query) {
        return Result.success(drugService.pageInventories(query));
    }

    @Operation(summary = "入库")
    @PostMapping("/drug-inventories/inbound")
    public Result<Void> inbound(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(body.get("id").toString());
        int quantity = Integer.parseInt(body.get("quantity").toString());
        String reason = body.get("reason") != null ? body.get("reason").toString() : null;
        drugService.inbound(id, quantity, reason);
        return Result.success();
    }

    @Operation(summary = "出库")
    @PostMapping("/drug-inventories/outbound")
    public Result<Void> outbound(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(body.get("id").toString());
        int quantity = Integer.parseInt(body.get("quantity").toString());
        String reason = body.get("reason") != null ? body.get("reason").toString() : null;
        drugService.outbound(id, quantity, reason);
        return Result.success();
    }

    @Operation(summary = "库存预警列表")
    @GetMapping("/drug-inventories/alerts")
    public Result<List<DrugInventoryVO>> listAlerts() {
        return Result.success(drugService.listAlerts());
    }

    // ==================== 处方 ====================

    @Operation(summary = "分页查询处方")
    @GetMapping("/drug-prescriptions")
    public Result<PageResult<DrugPrescriptionVO>> pagePrescriptions(@Valid DrugDictQuery query) {
        return Result.success(drugService.pagePrescriptions(query));
    }

    @Operation(summary = "新增处方")
    @PostMapping("/drug-prescriptions")
    public Result<Long> createPrescription(@Valid @RequestBody DrugPrescriptionSaveRequest request) {
        return Result.success(drugService.createPrescription(request));
    }

    @Operation(summary = "编辑/停用处方")
    @PutMapping("/drug-prescriptions/{id}")
    public Result<Void> updatePrescription(@PathVariable Long id,
                                            @Valid @RequestBody DrugPrescriptionSaveRequest request) {
        drugService.updatePrescription(id, request);
        return Result.success();
    }

    // ==================== 服药记录 ====================

    @Operation(summary = "分页查询服药记录")
    @GetMapping("/drug-records")
    public Result<PageResult<DrugRecordVO>> pageDrugRecords(@Valid DrugDictQuery query) {
        return Result.success(drugService.pageDrugRecords(query));
    }

    @Operation(summary = "今日待服药任务")
    @GetMapping("/drug-records/pending")
    public Result<List<PendingDrugVO>> listPending(
            @RequestParam(required = false) Long residentId,
            @RequestParam(required = false) LocalDate date) {
        return Result.success(drugService.listPending(residentId, date));
    }

    @Operation(summary = "记录用药")
    @PostMapping("/drug-records")
    public Result<Void> createDrugRecord(@Valid @RequestBody DrugRecordSaveRequest request) {
        drugService.createDrugRecord(request);
        return Result.success();
    }
}
