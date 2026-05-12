package com.yian.service.impl;

import com.yian.dto.LoginRequest;
import com.yian.dto.RefreshTokenRequest;
import com.yian.dto.RegisterRequest;
import com.yian.service.AuthService;
import com.yian.vo.LoginVO;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    @Override
    public LoginVO login(LoginRequest request) {
        throw new UnsupportedOperationException("TODO: implement login");
    }

    @Override
    public void register(RegisterRequest request) {
        throw new UnsupportedOperationException("TODO: implement register");
    }

    @Override
    public LoginVO refreshToken(RefreshTokenRequest request) {
        throw new UnsupportedOperationException("TODO: implement refreshToken");
    }
}
