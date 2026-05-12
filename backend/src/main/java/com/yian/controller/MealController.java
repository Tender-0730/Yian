package com.yian.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "膳食管理")
@RestController
@RequestMapping("/api/meals")
@RequiredArgsConstructor
public class MealController {
    // TODO: 膳食记录 CRUD
}
