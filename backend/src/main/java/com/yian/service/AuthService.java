package com.yian.service;

import com.yian.dto.LoginRequest;
import com.yian.dto.RefreshTokenRequest;
import com.yian.dto.RegisterRequest;
import com.yian.vo.LoginVO;

public interface AuthService {

    LoginVO login(LoginRequest request);

    void register(RegisterRequest request);

    LoginVO refreshToken(RefreshTokenRequest request);
}
