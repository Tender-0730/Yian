package com.yian.controller;

import com.yian.common.Result;
import com.yian.dto.LoginRequest;
import com.yian.dto.RefreshTokenRequest;
import com.yian.dto.RegisterRequest;
import com.yian.vo.LoginVO;
import com.yian.vo.UserInfoVO;
import com.yian.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "认证模块")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }

    @Operation(summary = "注册")
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return Result.success();
    }

    @Operation(summary = "刷新token")
    @PostMapping("/refresh")
    public Result<LoginVO> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return Result.success(authService.refreshToken(request));
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/me")
    public Result<UserInfoVO> me() {
        return Result.success(authService.me());
    }
}
