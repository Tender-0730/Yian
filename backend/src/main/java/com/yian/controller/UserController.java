package com.yian.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "个人中心")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    // TODO: 个人资料、修改密码
}
