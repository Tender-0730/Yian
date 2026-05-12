package com.yian.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "老人管理")
@RestController
@RequestMapping("/api/residents")
@RequiredArgsConstructor
public class ResidentController {
    // TODO: 老人 CRUD
}
