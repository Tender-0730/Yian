package com.yian.controller;

import com.yian.common.Result;
import com.yian.entity.CareLevel;
import com.yian.service.ResidentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "护理级别")
@RestController
@RequestMapping("/api/care-levels")
@RequiredArgsConstructor
public class CareLevelController {

    private final ResidentService residentService;

    @Operation(summary = "护理级别列表")
    @GetMapping
    public Result<List<CareLevel>> listCareLevels() {
        return Result.success(residentService.listCareLevels());
    }
}
