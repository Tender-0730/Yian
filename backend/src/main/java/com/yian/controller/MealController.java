package com.yian.controller;

import com.yian.common.PageResult;
import com.yian.common.Result;
import com.yian.dto.MealQuery;
import com.yian.dto.MealSaveRequest;
import com.yian.entity.DietaryRestriction;
import com.yian.service.MealService;
import com.yian.vo.MealRecordVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "膳食管理")
@RestController
@RequiredArgsConstructor
public class MealController {

    private final MealService mealService;

    @Operation(summary = "分页查询膳食记录")
    @GetMapping("/api/meals")
    public Result<PageResult<MealRecordVO>> pageMeals(@Valid MealQuery query) {
        return Result.success(mealService.pageMeals(query));
    }

    @Operation(summary = "按日期汇总膳食")
    @GetMapping("/api/meals/daily")
    public Result<List<MealRecordVO>> getDailyMeals(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return Result.success(mealService.getDailyMeals(date));
    }

    @Operation(summary = "新增膳食记录")
    @PostMapping("/api/meals")
    public Result<Long> createMeal(@Valid @RequestBody MealSaveRequest request) {
        return Result.success(mealService.createMeal(request));
    }

    @Operation(summary = "编辑膳食记录")
    @PutMapping("/api/meals/{id}")
    public Result<Void> updateMeal(@PathVariable Long id,
                                    @Valid @RequestBody MealSaveRequest request) {
        mealService.updateMeal(id, request);
        return Result.success();
    }

    @Operation(summary = "删除膳食记录")
    @DeleteMapping("/api/meals/{id}")
    public Result<Void> deleteMeal(@PathVariable Long id) {
        mealService.deleteMeal(id);
        return Result.success();
    }

    @Operation(summary = "某老人饮食禁忌")
    @GetMapping("/api/residents/{id}/restrictions")
    public Result<List<DietaryRestriction>> getResidentRestrictions(@PathVariable Long id) {
        return Result.success(mealService.getResidentRestrictions(id));
    }
}
