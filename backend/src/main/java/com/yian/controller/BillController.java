package com.yian.controller;

import com.yian.common.PageResult;
import com.yian.common.Result;
import com.yian.dto.*;
import com.yian.service.BillService;
import com.yian.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "费用管理")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BillController {

    private final BillService billService;

    // ==================== 费用配置 ====================

    @Operation(summary = "分页查询费用配置")
    @GetMapping("/fee-configs")
    public Result<PageResult<FeeConfigVO>> pageFeeConfigs(@Valid FeeQuery query) {
        return Result.success(billService.pageFeeConfigs(query));
    }

    @Operation(summary = "新增费用配置")
    @PostMapping("/fee-configs")
    public Result<Long> createFeeConfig(@Valid @RequestBody FeeConfigSaveRequest request) {
        return Result.success(billService.createFeeConfig(request));
    }

    @Operation(summary = "编辑费用配置")
    @PutMapping("/fee-configs/{id}")
    public Result<Void> updateFeeConfig(@PathVariable Long id,
                                         @Valid @RequestBody FeeConfigSaveRequest request) {
        billService.updateFeeConfig(id, request);
        return Result.success();
    }

    @Operation(summary = "删除费用配置")
    @DeleteMapping("/fee-configs/{id}")
    public Result<Void> deleteFeeConfig(@PathVariable Long id) {
        billService.deleteFeeConfig(id);
        return Result.success();
    }

    // ==================== 账单 ====================

    @Operation(summary = "生成月度账单")
    @PostMapping("/bills/generate")
    public Result<Integer> generateBills(@Valid @RequestBody BillGenerateRequest request) {
        return Result.success(billService.generateBills(request));
    }

    @Operation(summary = "分页查询账单")
    @GetMapping("/bills")
    public Result<PageResult<BillVO>> pageBills(@Valid FeeQuery query) {
        return Result.success(billService.pageBills(query));
    }

    @Operation(summary = "账单详情")
    @GetMapping("/bills/{id}")
    public Result<BillDetailVO> getBillDetail(@PathVariable Long id) {
        return Result.success(billService.getBillDetail(id));
    }

    @Operation(summary = "支付账单")
    @PostMapping("/bills/{id}/pay")
    public Result<Void> payBill(@PathVariable Long id, @Valid @RequestBody PaymentSaveRequest request) {
        billService.payBill(id, request);
        return Result.success();
    }

    @Operation(summary = "作废账单")
    @PutMapping("/bills/{id}/cancel")
    public Result<Void> cancelBill(@PathVariable Long id) {
        billService.cancelBill(id);
        return Result.success();
    }

    @Operation(summary = "欠费统计")
    @GetMapping("/bills/arrears")
    public Result<List<ResidentArrearsVO>> listArrears() {
        return Result.success(billService.listArrears());
    }
}
