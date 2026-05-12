package com.yian.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "房间管理")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RoomController {
    // TODO: 楼栋/房间/床位/入住退住
}
