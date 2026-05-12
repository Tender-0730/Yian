package com.yian.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "健康记录")
@RestController
@RequestMapping("/api/health-records")
@RequiredArgsConstructor
public class HealthRecordController {
    // TODO: 健康记录 CRUD
}
